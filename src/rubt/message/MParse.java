package rubt.message;

import java.nio.ByteBuffer;

import rubt.BitField;
import rubt.piece.Block;
import rubt.piece.BlockInfo;


/**
 * 
 * @author Scott Xu
 * @author Scott Xu
 * @author Scott Xu
 * 
 * MParse is responsible for the parsing of messages.  
 *
 */
public class MParse{
	public static int have(byte[] payload){		
		return ByteBuffer.wrap(payload).getInt(); 
	}
	public static BitField bitField(byte[] payload){
		return new BitField(payload);
	}
	public static BlockInfo request(byte[] payload){
		ByteBuffer bb=ByteBuffer.wrap(payload);
		return new BlockInfo(bb.getInt(),bb.getInt(),bb.getInt());
	}
	public static BlockInfo cancel(byte[] payload){
		ByteBuffer bb=ByteBuffer.wrap(payload);
		return new BlockInfo(bb.getInt(),bb.getInt(),bb.getInt());
	}
	public static Block piece(byte[] payload){				
		ByteBuffer bb=ByteBuffer.wrap(payload);
		int index=bb.getInt();
		int begin=bb.getInt();
		byte[] data=new byte[payload.length-4*2];
		bb.get(data);		
		return new Block(new BlockInfo(index,begin,payload.length),data);		
	}
}
