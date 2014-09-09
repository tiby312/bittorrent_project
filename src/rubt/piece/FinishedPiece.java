package rubt.piece;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *  
// * The FinishedPiece class contains the finished piece.  It is given to the TorrentFile to write. 
 *
 */
public class FinishedPiece{
	public final PieceInfo info;
	public final byte[] data;
	public final int length;
	public FinishedPiece(PieceInfo info,byte[] data){
		this.info=info;
		this.data=data;
		length=data.length;
	}
	public int index(){
		return info.index;
	}
	public String toString(){
		return "finished:="+info;
	}
	
}

