package rubt.peer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rubt.BitField;
import rubt.BitFieldArray;
import rubt.MLog;
import rubt.Tools;
import rubt.commander.Commander;
import rubt.commander.Commander.MCommand;
import rubt.message.MCreates;
import rubt.message.MParse;
import rubt.message.Message;
//import rubt.peer.PeerKeepAlive.KeepAliveTask;

import rubt.peer.keepalive.PeerKeepAlive;
import rubt.peer.keepalive.PeerKeepAliveListener;

import rubt.peer.network.PeerNetwork;
import rubt.peer.network.PeerNetworkListener;
import rubt.piece.Block;
import rubt.piece.BlockInfo;
import rubt.piece.FinishedPiece;
import rubt.piece.PieceBuilder;






class PeerEvent{
	ArrayList<PeerEventListener> listeners=new ArrayList<PeerEventListener>();
	void register(PeerEventListener l){
		listeners.add(l);
	}
	void fireChoke(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onChoke(p);			
		}
	}
	void fireUnChoke(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onUnChoke(p);
		}
	}
	void fireInterested(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onInterested(p);
		}
	}
	void fireUninterested(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onUninterested(p);
		}
	}
	void fireHave(PeerInt p,int i){
		for(PeerEventListener l:listeners){
			l.onHave(p,i);
		}
	}
	void fireBitfield(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onBitfield(p);
		}
	}
	void fireRequest(PeerInt p,BlockInfo i){
		for(PeerEventListener l:listeners){
			l.onRequest(p,i);
		}
	}
	void fireRequestDestroy(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onRequestDestroy(p);
		}
	}
	void fireRequestWorkingPiece(PeerInt p){
		for(PeerEventListener l:listeners){
			l.onRequestAWorkingPiece(p);			
		}
	}
	void fireFinishedPiece(PeerInt p,FinishedPiece fp){
		for(PeerEventListener l:listeners){
			l.onFinishedPiece(p,fp);
		}
	}
	void fireFinishedPieceFail(PeerInt p,int i){
		for(PeerEventListener l:listeners){
			l.onFinishedPieceFail(p,i);
		}
	}
//	void fireNewEvent(MCommand p){
//		for(PeerEventListener l:listeners){			
//			l.onNewEvent(p);
//		}
//	}
	
}

//in charge of piece related code
/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * The PeerPiece class holds the info for live requests, the piece it's downloading and request timeout.
 * THIS IS AN IMPLEMENTATION OF PEER INT. THIS IS PACKAGE LEVEL VISIBILITY. rest of program only uses interface  
 * 
 */
//
class Peer implements PeerInt,PeerNetworkListener,PeerKeepAliveListener{
	public static final int maxNumberOfLiveRequests = 10;
	
	// peer state
	private boolean isChokingUs = false;
	private boolean areChoking = false;
	private boolean isInterested = false;
	private boolean areInterested = false;

	public boolean AreInterested(){
		return areInterested;
	}
	public void register(PeerEventListener l){
		event.register(l);
	}
	
	
	PeerInfo info;
	public ArrayList<BlockInfo> liveRequests = new ArrayList<BlockInfo>();	
	public BitFieldArray avaiablePieces;
	private PieceBuilder workingPiece=null;
	private PeerEvent event=new PeerEvent();
	ScheduledThreadPoolExecutor requestTimeout;
	private ScheduledFuture<?> futureTimeout;
	PeerKeepAlive keepalive;
	PeerNetwork network;	
	Commander commander;
	public Peer(PeerEmbryo pe,int numpiece,Commander commander) {
		this.commander=commander;
		this.info =pe.peerinfo;// info;
		requestTimeout = new ScheduledThreadPoolExecutor(1);
		avaiablePieces=new BitFieldArray(numpiece,false);
		network=new PeerNetwork(pe);
		network.registerListener(this);
		
		keepalive=new PeerKeepAlive();
		network.registerListener(keepalive);		
		keepalive.register(this);
	}

	public void run(){		
		keepalive.run();
		network.run();
		
	}
//	/**
//	 * changes state of isChocking us and moves all blocks requested by this peer to no started
//	 * @param pieceChooser
//	 */
//	public void chockme(PieceChooser pieceChooser) {
//		isChokingUs = true;		
//		this.liveRequests.clear();
//		pieceChooser.removeWorkingPiece(this);
//		
//	}
//	
	
	public BitFieldArray getAvaiablePieces(){
		return avaiablePieces;//TODO maybe pass a copy instead
	}
	
	public void startWorkingOnPiece(PieceBuilder p){
		if(workingPiece!=null){
			MLog.error("already working on piece");
			return;
		}
		futureTimeout = requestTimeout.schedule(new PieceFail(),60, TimeUnit.SECONDS);	//TODO don't hard core timeout of 1min
		workingPiece=p;		
		fillUp();		
	}
	private void fillUp(){
		for(int i=liveRequests.size();i<maxNumberOfLiveRequests;i++){
			if(workingPiece.hasNotStartedBlocks()){
				BlockInfo bi=workingPiece.popBlockContToWorkOn();				
				Message m = MCreates.request(bi);
				network.sendMessage(m);
				liveRequests.add(bi);
			}else{
				return;
			}
		}
	}
	
	
	
	private class PieceFail implements Runnable {
		public void run() {
			Peer.this.event.fireFinishedPieceFail(Peer.this,workingPiece.index());
			workingPiece=null;
		}
	}
	public void destroy(){
		this.network.destroy();
		this.keepalive.destroy();
		requestTimeout.shutdownNow();
	}
	
	public boolean hasSameID(ByteBuffer a){
		return Tools.equals(info.peer_id.array(),a.array());
	}

	public String toString() {
		String s=info.toString();
		if(workingPiece!=null){
			s+="working on piece= "+workingPiece.index();
		}
		return  s;
	}

	public void sendAndSetAreInterested(){
		Message m=MCreates.interested();
		sendMessage(m);
		this.areInterested=true;
	}
	public void sendHave(int index){
		sendMessage(MCreates.have(index));	
	}
	public void sendBitField(BitField a){
		sendMessage(MCreates.bitField(a));
	}
	public void sendPiece(Block b){
		sendMessage(MCreates.piece(b));
	}
	
	private void sendMessage(Message m){
		network.sendMessage(m);
	}

	@Override
	public void onRequestSendKeepAlive() {
		Message m=MCreates.keepAlive();
		network.sendMessage(m);				
	}


	private class NewMessage implements MCommand{
		Message m;
		NewMessage(Message m){
			this.m=m;
		}
		@Override
		public void doCommand() {
			//TODO if we are chocking don't do anything
			MLog.log("got message="+m);
			
			
			switch (m.id){		
			case Message.KEEP_ALIVE:
				//TODO EB UPDATEING SCHEDULER
				return;
			case Message.CHOKE:
				if(!isChokingUs){
					isChokingUs=true;
					event.fireChoke(Peer.this);
				}			
				return;
			case Message.UNCHOKE:
				if(isChokingUs){
					isChokingUs=false;
					event.fireUnChoke(Peer.this);
				}				
				return;
			case Message.INTERESTED:
				if(!isInterested){
					isInterested = true;
					event.fireInterested(Peer.this);					
				}											
				return ;
			case Message.UNINTERESTED:
				if(isInterested){
					isInterested=false;
					event.fireUninterested(Peer.this);
				}				
				return ;
			case Message.HAVE:				
				int hav=MParse.have(m.payload);
				if(hav<0||hav>=avaiablePieces.length){
					MLog.log("invalid piece index="+hav+"quit" +
							" ignoring have message");
					return;
				}
				if(!avaiablePieces.contains(hav)){					
					avaiablePieces.set(hav,true);
					event.fireHave(Peer.this,hav);
				}
				if(workingPiece==null){
					event.fireRequestWorkingPiece(Peer.this);
				}
				return ;
			case Message.BITFIELD: 
				BitField bf=MParse.bitField(m.payload);
				avaiablePieces.setFrom(bf);				
				event.fireBitfield(Peer.this);	
				if(workingPiece==null){
					event.fireRequestWorkingPiece(Peer.this);
				}
				return;
			case Message.REQUEST:				
				BlockInfo bi=MParse.request(m.payload);
				event.fireRequest(Peer.this,bi);						
				return ;
			case Message.PIECE:
								
				Block block=MParse.piece(m.payload);
				
					/*
					this.pieceChooser.onfinishedBlock(p,block);
					p.finishedARequest(block);
					fillPeerWithRequests(p);
					*/
				if(workingPiece==null){
					MLog.error("receive a block despite not working on a piece");//TODO don't make error
					return;
				}
				
				
				if(!removeFromLiveRequests(block)){
					MLog.error("received a block we didn't request for. Discard!?");//TODO don't make error
					return;
				}
				
				//Piece piece=getWorking();
				workingPiece.addBlock(block);
				if(workingPiece.finished()){//if the block we added was the last one
					FinishedPiece fp=workingPiece.createFinished();					
					if(futureTimeout!=null){//TODO potential synch problems here i think.
						futureTimeout.cancel(false);
					}
					event.fireFinishedPiece(Peer.this,fp);
					workingPiece=null;
					event.fireRequestWorkingPiece(Peer.this);
					//event.fireRequest(this,fp);
				}else{
					fillUp();									
				}
				return;
			default:
				MLog.error("could not parse message"+m);//TODO don't make error				
			}						
		}
		
	}
	/**
	 * pushes message pair onto queue
	 */
	@Override
	public void onNewMessage(Message m) {
		if(!areChoking){ //TODO dunno if this is oks
			commander.pushCommand(new NewMessage(m));
		}
	}

	private boolean removeFromLiveRequests(Block block){
		for (int i = 0; i < liveRequests.size(); i++) {
			BlockInfo o = liveRequests.get(i);
			if (block.index() == o.index) {
				liveRequests.remove(i);
				return true;
			}
		}		
		return false;
	}

	@Override
	public void onRequestDestroy() {
		event.fireRequestDestroy(this);
		//tj.manager.onRequestDestroy(this);
	}


	@Override
	public void onSendMessage(Message m) {
		MLog.log("Sending\t\t " + m.toString());		
	}
	@Override
	public void choke() {
		Message m=MCreates.choke();
		sendMessage(m);
		this.areChoking=true;				
	}
	@Override
	public void unchoke() {
		Message m=MCreates.unchoke();
		sendMessage(m);
		this.areChoking=false;				
	}
	@Override
	public void sendAndSetUnInterested() {
		Message m=MCreates.notinterested();
		sendMessage(m);
		this.areInterested=false;		
		
	}
}