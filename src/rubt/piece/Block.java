/**
 * Block class used to manage blocks sent by Peers.
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 */
package rubt.piece;



/**
 *
 * Block class holds information about the block, and data in the block
 */
public class Block implements Comparable<Block>{	
	public Block(BlockInfo info,byte[] data){		
		this.data=data;
		this.info=info;
	
	}
	public final byte[] data;
	private BlockInfo info;
	
	/**
	 * returns the length of block
	 * @return
	 */
	public int length(){
		return info.length;
	}	
	
	/**
	 * returns the offset of the block
	 * @return
	 */
	public int offset(){
		return info.offset;
	}
	
	/**
	 * returns the index of the piece the block belongs to
	 * @return
	 */
	public int index(){
		return info.index;
	}
	public String toString(){
		return "full block="+info;
		
	}
	@Override
	public int compareTo(Block o) {		
		return info.compareTo(o.info);
	}
}

