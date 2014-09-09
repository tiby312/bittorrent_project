
package rubt;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * BitField class holds the bitfield bytearray and its length.
 * 
 */
public class BitField{
	public final int length;
	public final byte[] bitfield;
	public BitField(byte[] p){
		bitfield=p;
		length=p.length;		
	}

	public BitField(boolean[] bool){
		if(bool.length%8!=0){
			length = bool.length/8+1;
		}
		else{
			length = bool.length;
		}
		//length = bool.length/8;
		bitfield = new byte[length];
		for(int i = 0; i < bool.length;i++){
			bitfield[i/8] |= (1<<(7-i%8));
		}
	}
	/**
	 * gets the ith bit
	 * @param i
	 * @return
	 */
	public boolean get(int i){
		return ((bitfield[i/8]>>(7-i%8))&1)==1;
	}
	public String toString(){		
		String s="";
		for(int i = 0; i < bitfield.length; i++){//for each byte
			for(int j = 7; j >= 0; j--){//for each bit in each byte
				boolean k=((bitfield[i]>>j)&0x1) == 0x1 ;
				if(k){
					s+="1";
				}else{
					s+="0";
				}				
			}
		}
		return s;
	}
}
