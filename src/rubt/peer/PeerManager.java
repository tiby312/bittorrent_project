package rubt.peer;


import java.nio.ByteBuffer;
import java.util.ArrayList;

import rubt.MLog;
import rubt.PieceChooser;
import rubt.commander.Commander;


/**
 * 
 * @author Ken Reed
 * @author Scott Xu
 * @author Scott Xu
 * 
 * The PeerManager class is responsible for track of all Peers and destroying them when needed.  
 *
 */
public class PeerManager{
	//TorrentJobControl j;
	PieceChooser pc;
	int maxpeers=100;
	private ArrayList<PeerInt> peers;
	//this is used to disconnect a socket if we have pending handshakes at shutdown
	int numpieces;
	
	
	public PeerInt createPeer(PeerEmbryo pe,Commander commander){
		return new Peer(pe,numpieces,commander);		
	}
	
	public String toString(){
		String s="";
		for(PeerInt p:peers){
			s+=p+"\n";
		}
		return s;
	}
	
	
	public PeerManager(int numpieces){
		this.numpieces=numpieces;
		peers=new ArrayList<PeerInt>();
		
		//this.pc=pc;
	}
	
	/**
	 * sets the piece chooser
	 * @param pc
	 */
	public void registerPieceChooser(PieceChooser pc){
		this.pc=pc;
	}
	
	/**
	 * adds a peer to the peer list
	 * @param p
	 */
	public  void add(PeerInt p){
		MLog.log("adding peer");
		peers.add(p);
	}
	
	/** 
	 * handles destorying the peer. It notifies the piece chooser, calls the peers destroy and removes the peer from the list
	 * @param p
	 */
	public  void destroy(PeerInt p){
		MLog.log("destroying peer");		
		//pc.onPeerDestroy(p);
		
		peers.remove(p);
		p.destroy();
	}
	
	/**
	 * we impose limits the max number of peers
	 * this returns true if we go past the limits 
	 * @return
	 */
	public boolean maxPeersReached(){
		if(peers.size()>=maxpeers){
			return true;
		}
		return false;
	}
	
	/**
	 * checks if the peers is already connect 
	 * @param peerid
	 * @return
	 */
	public boolean peerAlreadyConnected(ByteBuffer peerid){
		for(PeerInt p:peers){
			if (p.hasSameID(peerid)){
				return true;
			}
		}
		return false;
	}
	

	
	/**
	 * this destroys all peers in the peer list and pending handshakers
	 */
	public void destroy(){
		MLog.log("destryoing peers");
		while(peers.size()>0){
			PeerInt p=peers.get(peers.size()-1);
			destroy(p);
		}
		
	}	
//	
//	/**
//	 * passes the destroy command to the main queue
//	 * @param me
//	 */
//	public void onRequestDestroy(Peer me) {
//		j.pushCommand(new RemovePeerCommand(me));
//			
//	}

	/**
	 * this sends a have message to all connected peers
	 * @param index
	 */
	public void broadcastHave(int index) {
		for(PeerInt p:peers){
			p.sendHave(index);			
		}
	}
	
//	/**
//	 * sends a remove peer command to the main queue
//	 * @author eric
//	 *
//	 */
//	public class RemovePeerCommand implements Commander.MCommand{		
//		Peer m;
//		public RemovePeerCommand(Peer m){			
//			this.m=m;
//		}
//		@Override
//		public void doCommand() {
//			//MLog.log("remove peer command");
//			destroy(m);						
//		}
//		
//	}
	
}