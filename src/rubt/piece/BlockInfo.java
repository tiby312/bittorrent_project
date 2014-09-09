package rubt.piece;

/**
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * The BlockInfo class contains the info used for constructing Block objects.  
 *
 */
public class BlockInfo implements Comparable<BlockInfo>{
	public static final int typicalsize=1<<14; //block size is 2^14 bytes
	public final int index;
	public final int offset;
	public final int length;
	public BlockInfo(int index,int offset,int length){
		this.index=index;
		this.offset=offset;	
		this.length=length;
	}

	@Override
	public int compareTo(BlockInfo co) {
		if(this.index!=co.index){
			System.out.println("shouldnt compare bocks belong ot different pieces");
		}
		if(this.offset<co.offset){
			return -1;
		}else if(this.offset>co.offset){
			return 1;
		}		
		System.out.println("Should never have blocks at same offset");
		return 0;
		
	}
	public String toString(){
		return "index="+index+" offset="+offset+" length"+length;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof BlockInfo){
			BlockInfo ba=(BlockInfo)o;
			return ba.index==index&&ba.offset==offset&&ba.length==length;
		}
		return false;		
	}
	
}