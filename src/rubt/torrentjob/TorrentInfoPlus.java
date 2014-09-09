
package rubt.torrentjob;
import edu.rutgers.cs.cs352.bt.TorrentInfo;
import edu.rutgers.cs.cs352.bt.exceptions.BencodingException;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * The TorrentInfoPlus class simply extend the TorrentInfo class, and adds extra functions specific to the torrent file.  It includes functions to get the number of pieces and the length of the pieces.
 *
 */
public class TorrentInfoPlus extends TorrentInfo{

	public TorrentInfoPlus(byte[] torrent_file_bytes) throws BencodingException {
		super(torrent_file_bytes);		
	}

	public int getPieceLength(int index){
		int length=piece_length;
		if(index==piece_hashes.length-1){
			length=file_length-index*piece_length;
		}
		return length;
	}
	public int getNumPieces(){
		return this.piece_hashes.length;
	}
	
}
