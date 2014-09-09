package rubt.piece;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import rubt.MLog;

/**
 * The Piece class contains the blocks of a piece.  Here, we store what blocks are started, not started, and completed.
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 */
public class PieceBuilder{
	private ArrayList<Block> completed;
	private ArrayList<BlockInfo> notstarted;
	private ArrayList<BlockInfo> started;
	PieceInfo info;
	public PieceBuilder(PieceInfo info){
		this.info=info;		
				
		completed=new ArrayList<Block>();
		started=new ArrayList<BlockInfo>();
		notstarted=new ArrayList<BlockInfo>();
		int runningoffset=0;
		for(int i=0;i<info.numblocks;i++){
			int zz=Math.min(BlockInfo.typicalsize,info.length-runningoffset);
			BlockInfo bc=new BlockInfo(info.index,runningoffset,zz);			
			this.notstarted.add(bc);
			runningoffset+=bc.length;
		}		
		
	}
	
	/**
	 * returns the index of the piece
	 * @return
	 */
	public int  index(){
		return info.index;
	}
	
	/**
	 * returns true if no blocks have been started
	 */
	public boolean hasNotStartedBlocks(){
		return !notstarted.isEmpty();
	}
	
	/**
	 * returns the next block to work on
	 * @return
	 */
	public BlockInfo popBlockContToWorkOn(){		
		BlockInfo bc=notstarted.remove(notstarted.size()-1);				
		started.add(bc);
		return bc;
	}
	
//	/**
//	 * moves block from started to not started
//	 * @param bi
//	 */
//	public void switchFromStartedToNotStarted(BlockInfo bi){		
//		if(!started.remove(bi)){
//			MLog.log("error! could not find block in started");
//			return;
//		}
//		notstarted.add(bi);
//	}
	
	public String toString(){
//		String s="";				
//		s+="completed=";
//		for(Block blo:completed){
//			s+=blo;
//		}
//		s+="started=";
//		for(BlockInfo blo:started){
//			s+=blo;
//		}
//		s+="notstarted=";
//		for(BlockInfo blo:notstarted){
//			s+=blo;
//		}
		return info.toString();
	}
	
	
	/**
	 * called when a block is complete to remove the block from started and put it into completed
	 * If the piece is finished this returns the completed piece otherwise it returns null
	 * @param completedBlock completed block
	 */
	public void addBlock(Block completedBlock){
		if(completedBlock.index()!=info.index){
			System.out.println("piece block prob! should never happen");
		}
		for(BlockInfo i:started){
			if (i.index==completedBlock.index()){
				completed.add(completedBlock);
				started.remove(i);		
				break;
			}
		}
		
		//return null;
	}
	
	/**
	 * creates a finished piece to be written
	 * @return
	 */
	public FinishedPiece createFinished(){
		if(!finished()){
			MLog.log("SHOULD NEVER HAPPEN");
		}
		Collections.sort(completed);
		ByteBuffer bb=ByteBuffer.allocate(info.length);
		for(Block blo:completed){
			bb.put(blo.data);		}
		return new FinishedPiece(info,bb.array());					
	}
	
	
	/**
	 * checks if a piece is completed
	 * @return true if piece is completed
	 */
	synchronized public boolean finished(){
		return completed.size()==info.numblocks&&started.isEmpty();			
			
	}
}

