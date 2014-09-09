package rubt.torrentjob;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import edu.rutgers.cs.cs352.bt.exceptions.BencodingException;


import rubt.BitField;
import rubt.MLog;
import rubt.PieceChooser;
import rubt.Tools;
import rubt.announcer.AnnounceResponse;
import rubt.announcer.AnnounceTimerListener;
import rubt.announcer.AnnounceType;
import rubt.announcer.AnnounceTimer;
import rubt.commander.Commander;
import rubt.commander.Commander.MCommand;
import rubt.peer.NewPeerConnectListener;
import rubt.peer.PeerEmbryo;
import rubt.peer.PeerEventListener;
import rubt.peer.PeerInfo;
import rubt.peer.PeerInt;
import rubt.peer.PeerListener;
import rubt.peer.PeerManager;
import rubt.piece.Block;
import rubt.piece.BlockInfo;
import rubt.piece.FinishedPiece;
import rubt.piece.PieceBuilder;
import rubt.piece.PieceInfo;



/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * The TorrentJobControl class contains all the different components of a TorrentJob.    
 *
 */
class TorrentJob implements TorrentJobInt,PeerEventListener,NewPeerConnectListener,AnnounceTimerListener{
	final ByteBuffer peer_id;		
	long downloaded;
	long uploaded;
	public TorrentInfoPlus torrentInfo;
	
	
	private Commander commander;
	//public TorrentJob torrentjob;
	public PeerManager manager;
	private AnnounceResponse lastResponse;		
	private AnnounceTimer announcer;			
	private PieceChooser pieceChooser;	
	private PeerListener peerlistener;	
	private TorrentFile torrentfile;


	public String toString(){
		String s="/----TorrentJob------\\\n";
		s+="peer_id="+new String(peer_id.array())+"\n";
		s+="downloaded="+downloaded+" uploaded="+uploaded;
		s+="\n\\------------/";
		return s;
	}
	
	public TorrentJob(TorrentInfoPlus torrentinfo,String targetFile){
		torrentInfo=torrentinfo;						
		peer_id=Tools.generatePeerId();		
		downloaded=0;
		uploaded=0;	
		
		commander=new Commander();
		
		peerlistener= new PeerListener(commander);
		announcer=new AnnounceTimer();
		manager=new PeerManager(torrentInfo.getNumPieces());
		pieceChooser=new PieceChooser(torrentInfo.getNumPieces());
		torrentfile=new TorrentFile(torrentinfo);
		
		peerlistener.register(this);		
		announcer.register(this);				
		manager.registerPieceChooser(pieceChooser);
		
				
		try {
			torrentfile.init(targetFile);
		} catch (IOException e) {
			MLog.error("problem with torrentfile"+e);
		}
				
		for(int i=0;i<torrentInfo.getNumPieces();i++){
			FinishedPiece fp=torrentfile.readIthPiece(torrentInfo,i);
			if(fp!=null){
				pieceChooser.notstarted.set(fp.index(),false);
				pieceChooser.completed.set(fp.index(),true);
			}						
		}
		if(pieceChooser.completed()){
			MLog.log("we already have the piece");
		}
				
		
	}
//	
//	public class DestroyPeerCommand implements Commander.MCommand{
//		Peer p;
//		public DestroyPeerCommand(Peer p){
//			this.p=p;
//		}
//		@Override
//		public void doCommand() {
//			manager.destroy(p);						
//		}		
//	}
	private class PrintStatusCommand implements Commander.MCommand{
		@Override
		public void doCommand() {			
			System.out.println(pieceChooser);
		}
		
	}
	private class PrintPeersCommand implements Commander.MCommand{
		@Override
		public void doCommand() {			
			System.out.println(manager);
		}
		
	}	
	public void printStatus(){
		commander.pushCommand(new PrintStatusCommand());
	}
	public void printPeers(){
		commander.pushCommand(new PrintPeersCommand());
		
	}

	public void shutdownStart(){
		commander.pushASync(new AnnounceTask(announcer,new AnnounceType.Stop())); //TODO WE MUST WAIT UNTIL THIS FINISHES BEFORE SHUTDOWN
		commander.pushCommand(commander.new ShutDownCommand());
	}
	
	/**
	 * call this thread to wait for the torrentjob to close
	 */
	public void join(){		
		
		announcer.shutdown();
		System.out.println("shutting down");
		peerlistener.stop();
		torrentfile.close();				
		MLog.log("announcer stopped.");
		manager.destroy();
		MLog.log("peers stopped.");
		System.out.println("shutted down");
		commander.join();
	}
	
//	/**
//	 * anybody who wants to issue a command calls this function. this function can happen asynchronously by any thread.
//	 * the command is then put on the queue to be read by the main thread.
//	 * @param c
//	 */
//	public void pushCommand(Commander.MCommand c){
//		commander.pushCommand(c);
//	}
//	
//	
//	public void pushASync(Commander.ASyncInt c){
//		commander.pushASync(c);
//	}
//	
	
	
	/**
	 * THE function that starts the command queue, the peer listener and sents the first announce
	 */
	public void start(){
		peerlistener.start();		
		commander.start();
		//announcer.start();
		commander.pushASync(new AnnounceTask(announcer,new AnnounceType.Start()));
	}


	

	
	
	
	
	

	private class HandShakeAnnounceParent{
		/**
		 * checks if the peer is official, already connected, and have space.
		 * this is a function since when we want to handshake with peers on the tracker,
		 * we can already infer some things without even opening a connection
		 * those things that can be inferred from what the tracker gives us is checked
		 * in this function.
		 * @param peeri
		 * @return
		 */
		protected boolean okToConnect(PeerInfo peeri){
			return peeri.official&&!manager.peerAlreadyConnected(peeri.peer_id)&&!manager.maxPeersReached();			
		}		
	}
/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 * The HandShakeTask class is responsible for the initial handshaking procedure.  
 *
 */
	public class HandShakeTask extends HandShakeAnnounceParent implements Commander.ASyncInt{
		private PeerEmbryo pe;
		private Socket connection;
		
		private boolean failed=false;
		public HandShakeTask(Socket socket){		
			connection=socket;
		}
		
		/**
		 * starts a hand shake task
		 */
		@Override
		public void start() {
			// TODO Auto-generated method stub
			MLog.log("handshake about to start");		
	
		}
	
		/**
		 * runs a handshake asynchronously
		 */
		@Override
		public void async() {		
			try{		
				DataInputStream reader = new DataInputStream(connection.getInputStream());			
				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
	
				MLog.log("made reader and writer ="+connection.getInetAddress().getHostAddress()+":"+connection.getPort());
							
				//write handshake
				writer.write(19);
				writer.writeBytes("BitTorrent protocol");
				writer.write(new byte[8]);
				writer.write(torrentInfo.info_hash.array());
				writer.write(peer_id.array());
	
	
				byte[] protocolByteString = new byte[1+"BitTorrent protocol".length()];
				reader.readFully(protocolByteString);
	
				byte[] reserved = new byte[8];
				reader.readFully(reserved);
	
				byte[] infoHash = new byte[20];
				reader.readFully(infoHash);
	
				byte[] peerId = new byte[20];
				reader.readFully(peerId);
	
				MLog.log("read response");
				
				PeerInfo peerinfo=new PeerInfo(ByteBuffer.wrap(peerId),connection.getInetAddress().getHostAddress(),connection.getPort());		
				
				pe=new PeerEmbryo(connection,reader,writer,peerinfo,infoHash);
				MLog.log("got handshake reply");
			}catch(IOException e){
				MLog.log("problem connecting to peer="+pe);
				failed=true;
			}		
			
		}
	
		/**
		 * finishes a handshake by removing the socket from pending handshaker, and verifing peer
		 */
		@Override
		public void finish() {
			if(failed){
				return;
			}
			MLog.log("finalize handshake");
			if(pe!=null){
				MLog.log("got handshake response. validating...");
				if(!Tools.equals(pe.infoHash, torrentInfo.info_hash.array())    ){
					MLog.log("hashes not equal");
					pe.close();
					return;
				}
				
				//verify peer id		
				if(!lastResponse.contains(pe.peerinfo)){
					MLog.log("not a valid peer id");
					pe.close();
					return;
				}
				
				
				if(manager.peerAlreadyConnected(pe.peerinfo.peer_id)){
					MLog.log("peer already ocnnected");
					pe.close();
					return;
				}
				
				if(!pe.peerinfo.official){
					MLog.log("not official peer");
					pe.close();
					return;
				}
				
				if(!okToConnect(pe.peerinfo)){
					MLog.log("fail to connect");
					pe.close();
				}
				
				PeerInt newPeer=manager.createPeer(pe,commander);
				
				
					
				
				
				
				MLog.log("adding peer="+newPeer.toString());
				
				manager.add(newPeer);
				newPeer.register(TorrentJob.this);
				newPeer.run();
				
				newPeer.sendBitField(pieceChooser.getCompleted());				
	
			}else{
				MLog.log("bad handshake");
			}				
		}
	
		@Override
		public void cancel() {
			try {
				connection.close();
			} catch (IOException e) {
				//TODO bla
			}
			
		}
		
	}
	
	public class AnnounceTask extends HandShakeAnnounceParent implements Commander.ASyncInt{
		private AnnounceTimer announcer;
		private AnnounceType ta;
		private AnnounceResponse response;
		private InputStream is;
		public AnnounceTask(AnnounceTimer announce,AnnounceType ta){
			this.announcer=announce;
			this.ta=ta;
		}
		@Override
		public void start() {			
			MLog.log("ANNOUNCING!");
		}

		@Override
		public void async() {						
			try {			
				URL url = createQuery(ta);
				MLog.log(""+url);
				response=get(url);				
			} catch (AnnounceTimer.Fail e) {			
				MLog.log("failed to contact tracker");
				response=null;
			}										
		}

		@Override
		public void finish() {
			if(ta instanceof AnnounceType.Stop){
				return;//this means we're closing the program so no point updating stuff in memory since we're just going to close
			}
			if(response!=null){
				announcer.resetTimer(response.announceInterval);
				lastResponse=response;
				for(PeerInfo peeri:response.peers){
					if(okToConnect(peeri)){			
						MLog.log("trying to handshake with "+peeri);
						Socket socket;
						try {
							socket = new Socket(peeri.ip,peeri.port);
							commander.pushASync(new HandShakeTask(socket));		
							MLog.log("pushed command");
						} catch (UnknownHostException e) {
							MLog.log("unknown host"+e);
						} catch (IOException e) {
							MLog.log("Problem"+e);
						}
						
					}		
				}		
					
			}else{
				announcer.resetTimer(announcer.defaultAnnounceInterval);
			}						
		}

		@Override
		public void cancel() {
			//We never want to be interupted when announcing.
			//for example when we shutdown, we still want announce to finish or timeout
			//before we shutdown
//			if(is!=null){
//				try {
//					is.close();
//				} catch (IOException e) {					
//				}
//			}
		}
		/**
		 * Sends a and gets response to tracker.  throws exception if failed
		 * @param url
		 * @return
		 * @throws AnnounceFail
		 */
		private AnnounceResponse get(URL url) throws AnnounceTimer.Fail{
			try {
				URLConnection con = url.openConnection();
				con.setConnectTimeout(announcer.timeout);
				con.setReadTimeout(announcer.timeout);
				is=con.getInputStream(); //response
				
				ByteArrayOutputStream buffer=new ByteArrayOutputStream();

				while(is.available()>0){							
					buffer.write(is.read());								
				}
				buffer.flush();

				byte[] response=buffer.toByteArray();		
				return new AnnounceResponse(response);
			} catch (IOException e) {			
				throw new AnnounceTimer.Fail("io prob");
			} catch (BencodingException e) {
				throw new AnnounceTimer.Fail("bencoding prob");
			}
		}
		/**
		 * creates the url for the tracker. we pass the announce type (start, stopped, completed etc)
		 * @param job
		 * @param announce
		 * @return
		 * @throws AnnounceFail
		 */
		private URL createQuery(AnnounceType announce) throws AnnounceTimer.Fail{		
			try {
				String announceParameters="";
				announceParameters+="?info_hash="+Tools.convertToEscapedHex(torrentInfo.info_hash.array());
				announceParameters+="&peer_id="+new String(peer_id.array());
				announceParameters+="&port="+peerlistener.getPort();
				announceParameters+="&uploaded="+uploaded;
				announceParameters+="&downloaded="+downloaded;
				announceParameters+="&left="+(torrentInfo.file_length-downloaded);
				announceParameters+=announce.getArg();			

				URL url = new URL(torrentInfo.announce_url.toString()+announceParameters);			
				return url;
			} catch (MalformedURLException e) {
				throw new AnnounceTimer.Fail("prob with url");
			}		
		}		
	}
	
	



	@Override
	 public void onChoke(PeerInt p) {
		// TODO Auto-generated method stub
		
	}


	@Override
	 public void onUnChoke(PeerInt p) {
		// TODO Auto-generated method stub
		
	}


	@Override
	 public void onInterested(PeerInt p) {
		// TODO Auto-generated method stub
		
	}


	@Override
	 public void onUninterested(PeerInt p) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onHave(PeerInt p, int i) {		
		if(pieceChooser.findBestPieceToWork(p)!=-1){
			if(!p.AreInterested()){
				p.sendAndSetAreInterested();		
			}
		}
			
	}


	@Override
	public void onBitfield(PeerInt p) {		
		if(pieceChooser.findBestPieceToWork(p)!=-1){
			if(!p.AreInterested()){
				p.sendAndSetAreInterested();		
			}
		}
	}


	@Override
	public void onRequest(PeerInt p, BlockInfo bi) {
		if(pieceChooser.havePiece(bi.index)){				
			Block block=torrentfile.retreiveBlock(bi);
			if(block!=null){
				p.sendPiece(block);							
				uploaded+=block.length();
			}
		}				
	}


	@Override
	public void onFinishedPiece(PeerInt p, FinishedPiece fp) {						
		if(!TorrentFile.validatePiece(torrentInfo,fp)){
			MLog.log("peer send us an invalid piece"); //TODO what to do here.
			manager.destroy(p);
		}
		MLog.log("FINISHED PIECE="+fp.index()+"prog="+this);		
		this.pieceChooser.completed.set(fp.index(),true);
		
		MLog.log("finished piece" + fp.toString());
		
		if(!torrentfile.writePiece(torrentInfo, fp)){
			MLog.log("failed to write piece");
		}
		manager.broadcastHave(fp.index());
		downloaded+=fp.length; //TODO we updated downloaded as the granularity of pieces is this ok
		if(pieceChooser.completed()){
			MLog.log("TORRENT FILE IS FINISH.");
			commander.pushASync(new AnnounceTask(announcer,new AnnounceType.Complete()));				
		}
	
	}


	@Override
	public void onRequestDestroy(PeerInt p) {
		// TODO Auto-generated method stub
		manager.destroy(p);
		
	}

	@Override
	public void onFinishedPieceFail(PeerInt p,int i) {
		MLog.log("failed to get piece "+i+"from peer="+p);
		pieceChooser.notstarted.set(i, true);
		//pieceChooser.setNotStarted(i,true);
		// TODO this peer is probably bad maybe disconnect.
		
	}

	@Override
	public void onRequestAWorkingPiece(PeerInt p) {		
		
		int pnum=pieceChooser.findBestPieceToWork(p);
		if(pnum==-1){
			MLog.log("this peer does not have anything we want---\np-"+p.getAvaiablePieces()+"\ni-"+pieceChooser);
			//TODO what do we do here. I guess return
			return;
		}
		
		
		if(!pieceChooser.needToStartPiece(pnum)){
			MLog.error("trying to start piece that has already been started or completed");
		}
		pieceChooser.notstarted.set(pnum,false);
		//pieceChooser.setNotStarted(pnum,false);//since we started it
		
		PieceBuilder piece=new PieceBuilder(new PieceInfo(pnum,torrentInfo));
		//System.out.println("S")
		p.startWorkingOnPiece(piece);		
					
	}


	@Override
	public void newEvent(Socket c) {
		commander.pushASync(new HandShakeTask(c));
		
	}

	@Override
	public void announcePlease(AnnounceTimer a) {
		commander.pushASync(new AnnounceTask(a,new AnnounceType.Nothing()));
		// TODO Auto-generated method stub
		
	}


}
