package rubt;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 * TODO: consolidate with BitField Class
 *
 */
public class BitFieldArray {
	public final int length;
	private final boolean[] arr;
	public BitFieldArray(int size,boolean val){
		arr=new boolean[size];
		length=size;
		for(int i=0;i<arr.length;i++){
			arr[i]=val;
		}
	}

	boolean get(int i){
		return arr[i];
	}

	public void set(int i,boolean val){
		arr[i]=val;
	}
	public boolean contains(int i){
		return arr[i];
	}
	public void setFrom(BitField bf){	
		for(int i = 0; i < length;i++){
			arr[i]=false;
		}
		for(int i = 0; i < length;i++){			
			set(i,bf.get(i));
		}
	}

	public String toString(){
		String s=length + " ";
		for(int i=0;i<arr.length;i++){
			if(arr[i]){
				s+="1";
			}else{
				s+="0";
			}			
		}
		return s;
	}

	public BitField toBitField(){
		int length;
		if(arr.length%8!=0){
		length = arr.length/8+1;
		}
		else{
			length = arr.length/8;
		}

		//			length = arr.length/8;
		byte[] bitfield = new byte[length];
		for(int i = 0; i < length;i++){
			bitfield[i]=0;
		}
		for(int i = 0; i < arr.length;i++){
			if(arr[i]){
				bitfield[i/8] |= (1<<(7-i%8)) ;
			}
		}
		return new BitField(bitfield);


	}
}
