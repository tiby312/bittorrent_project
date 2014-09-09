package rubt.piece;

import rubt.torrentjob.TorrentInfoPlus;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * The PieceInfo class holds the information for a Piece object.  It stores index number, length, and number of blocks it contains.
 *
 */
public class PieceInfo{
	public final int index;
	public final int length;
	public final int numblocks;	
	public PieceInfo(int index,TorrentInfoPlus ti){
		this.index=index;
		length=ti.getPieceLength(index);
		
		int nblocks=length/BlockInfo.typicalsize;
		int remainder=length % BlockInfo.typicalsize;
		if(remainder>0){
			nblocks++;
		}
		numblocks=nblocks;	
	}
	public String toString(){
		return "index="+index+" length="+length+" numblocks="+numblocks;
	}
}
