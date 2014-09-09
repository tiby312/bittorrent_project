package rubt;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;



//import rubt.torrentjob.TorrentJob;
import rubt.BitField;
import rubt.peer.PeerInt;


/**
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * PieceChooser selects the next piece we should request from the peer. It stores data to keep track of what blocks we have/haven't started, completed, and what 
 * pieces we have started and finished.
 * 
 */
public class PieceChooser {		
	public final BitFieldArray notstarted;
	public final BitFieldArray completed;
	//private TorrentJob tj;
	/**
	 * Initializes the piece chooser
	 * @param completed
	 * @param notstarted
	 * @param torrentJob
	 * @param info
	 */
	public PieceChooser(int numpieces){		
		this.completed=new BitFieldArray(numpieces,false);
		notstarted=new BitFieldArray(numpieces,true);
	}
	
//	
//	public void setNotStarted(int i,boolean val){		
//		notstarted.set(i,val);
//	}
	public BitField getCompleted(){
		return completed.toBitField();
	}
	public boolean havePiece(int index){
		return completed.get(index);
	}



	public String toString(){
		String tmp = "-------------------\nStartedPieces";
//		for(Piece p: started){
//			tmp += "("+p.index()+")";
//		}
		tmp += "\nnotStarted\t\t";
		tmp+=notstarted;
				
		tmp += "\ncompletePieces\t\t";

		tmp+=completed;
		
		tmp+="\n-------------------";
		return tmp;
	}
	
//	public void onPeerDestroy(Peer p){
//		if(p.HasWorkingPiece()){
//			removeWorkingPiece(p);
//		}	
//	}
//	public void removeWorkingPiece(Peer p){
//		//started.remove(p.getWorking());
//		this.notstarted.set(p.getWorking().index(),true);
//		p.setWorking(null);
//	}

	
	public int findBestPieceToWork(PeerInt peer){
		//if we didnt find, start a new piece
		//Piece targetPiece=null;
		
		for(int i=0;i<peer.getAvaiablePieces().length;i++){			
			if(peer.getAvaiablePieces().get(i)&&needToStartPiece(i)){
				return i;
			}			
		}
		return -1;
		
		
//		//for(int i:peer.avaiablePieces){
//			//if(this.needToStartPiece(i)){
//				if(!peer.isInterested){
//					Message m=MCreates.interested();
//					peer.sendMessage(m);
//					//return; //TODO not sure if return here
//				}
//				if(!peer.isChokingUs){
//					return i;
//					//targetPiece= this.startPiece(i,this.torrentinfo);
//					//MLog.log("creating new piece");
//				}
//				//break;
//			}
//		}			
//		return -1;
		
	}
	
	
//	
//
//	public void onfinishedPieceFromFile(FinishedPiece fp){
//		if(!validatePiece(tj.torrentInfo,fp)){
//			//MLog.log("invalid piece in file");
//		}else{
//			//MLog.log("j"+torrentinfo.getNumPieces());
//			//MLog.log("found file piece index="+fp);
//			
//			
//			notstarted.set(fp.index(),false);
//			completed.set(fp.index(),true);
//			//completed.add(fp.index());
//			//checkIfFinishedAndSignal();
////			
////			
////			for(int i=0;i<notstarted.size();i++){
////				if(notstarted.get(i)==fp.index()){
////					notstarted.remove(i);
////					completed.add(fp.index());					
////					checkIfFinishedAndSignal();
////					return;
////				}
////			}
//			//MLog.log("SHOULD NEVER HAPPEN");
//
//		}
//
//	}
	public boolean completed(){
		for(int i=0;i<completed.length;i++){
			if(!completed.get(i)){
				return false;
			}
		}
		return true;
	}
//	private boolean checkIfFinishedAndSignal(){
//		//MLog.log("FINISHED");
//		
//		if(completed()){
//			tj.onComplete();
//			return true;
//		}
//		//torrent
//		//torrentJob.o
//		return false;
//	}
	
	/*
	 * returns false if validation failed
	 */



//
//	public void removeWorkingPiece(Peer peer){
//		notstarted.set(peer.getWorking().index(),true);
//		//this.notstarted.add(peer.getWorking().index());
//		started.remove(peer.getWorking());		
//		peer.setWorking(null);
//	}

//	/**
//	 * This updates the piece object when a block is acquired. If the block is completed we move the piece to completed and write the piece.
//	 * @param peer Peer which acquired a block
//	 * @param block The block the peer acquired
//	 */
//	public void onfinishedBlock(Peer peer,Block block){					
//		//find piece this block belongs too
//		//TODO EB This will be easy with workingPiece
//
//		if(!peer.HasWorkingPiece()){
//			MLog.log("should neve rhappen");
//		}
//		Piece piece=peer.getWorking();
//
//		piece.addBlock(block);
//		if(piece.finished()){//if the block we added was the last one
//			onfinishedPiece(peer,piece);			
//		}
//
//		/*
//		for(Piece piece:started){			
//			if(block.index()==piece.index()){
//				FinishedPiece finishedPiece=piece.addBlock(block);
//				torrentJob.onBlockFinished(block);
//				if(finishedPiece!=null){
//					MLog.log("FINISHED PIECE");
//					started.remove(piece);
//					completed.add(piece.index());
//					torrentJob.onPieceFinished(finishedPiece);
//				}
//				return;
//			}
//		}*/
//		//MLog.log("ERROR! got block fail!?");
//
//	}


	//
	//	/**
	//	 * moves the moves all blocks from started to not started
	//	 * @param blockInfo
	//	 */
	//	void switchFromStartedToNotStarted(BlockInfo blockInfo){
	//		for(int i=0;i<started.size();i++){
	//			Piece piece=started.get(i);
	//			if(piece.index()==blockInfo.index){							
	//				piece.switchFromStartedToNotStarted(blockInfo);
	//				return;				
	//			}
	//		}
	//		MLog.log("error couldnt find the piece the block belonged to in started");
	//		//for(BlockI)
	//	}


//
//
//	/**
//	 * createPiece create a Piece object given the index and TorrentInfo file
//	 * @param i  the index of the Piece
//	 * @param ti the TorrentInfo object
//	 * @return Piece object made from the index and TorrentInfo
//	 */
//	private Piece startPiece(int i,TorrentInfoPlus ti){
//		notstarted.set(i,false);
//
//		Piece p=new Piece(new PieceInfo(i,ti));
//		//started.add(p);
//		return p;
//	}

	//	/**
	//	 * Given a peer, returns what block it should work on next, if it didnt start a piece this will find a new one to start.  If the peer is not interested and we find a piece
	//	 * we will send an interested message and if not chocked followed by a request.
	//	 * @param peer
	//	 * @param torrentInfo
	//	 * @return
	//	 */
	//	BlockInfo findBestBlockToWorkOn(Peer peer,TorrentInfoPlus torrentInfo){
	//		Piece targetPiece=findPieceThatsStarted(peer);
	//		//if we didnt find, start a new piece
	//		if(targetPiece==null){			
	//			for(int i:peer.avaiablePieces){
	//				if(this.needToStartPiece(i)){
	//					if(!peer.isInterested){
	//						Message m=MCreates.interested();
	//						peer.sendMessage(m);
	//						//return; //TODO not sure if return here
	//					}
	//					if(!peer.isChokingUs){
	//						targetPiece= this.createPiece(i,torrentInfo);
	//						MLog.log("creating new piece");
	//					}
	//					break;
	//				}
	//			}			
	//		}		
	//		if(targetPiece==null){
	//			MLog.log("peer does not have anything we want or is choking us");
	//			return null;
	//		}	
	//		BlockInfo bc=targetPiece.popBlockContToWorkOn();				
	//		return bc;
	//	}



	/**
	 * checks if we need to start the piece at the index given
	 * @param index
	 * @return true if we need to start the piece
	 */
	public boolean needToStartPiece(int index){
		return notstarted.get(index);
		
	}


//	public void setCompleted(int index, boolean b) {
//		completed.set(index, b);
//		
//	}

}
