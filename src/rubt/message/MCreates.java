package rubt.message;

import java.nio.ByteBuffer;

import rubt.BitField;
import rubt.piece.Block;
import rubt.piece.BlockInfo;


/**
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
// * MCreates is our constructor class to handle the creation of different messages.
 */

public class MCreates{
	public static Message keepAlive(){
		return new Message();
	}
	public static Message choke(){
		return new Message(1,Message.KEEP_ALIVE,new byte[0]);
	}
	public static Message unchoke(){
		return new Message(1,Message.UNCHOKE,new byte[0]);
	}
	public static Message interested(){
		return new Message(1,Message.INTERESTED,new byte[0]);
	}
	public static Message notinterested(){
		return new Message(1,Message.UNINTERESTED,new byte[0]);
	}
	public static Message have(int index){		
		return new Message(5,Message.HAVE,ByteBuffer.allocate(4).putInt(index).array());
	}
	public static Message bitField(BitField bf){
		return new Message(1+bf.length,Message.BITFIELD,bf.bitfield);
	}
	public static Message request(BlockInfo bc){		
		ByteBuffer pl=ByteBuffer.allocate(4*3);
		pl.put(ByteBuffer.allocate(4).putInt(bc.index).array());
		pl.put(ByteBuffer.allocate(4).putInt(bc.offset).array());
		pl.put(ByteBuffer.allocate(4).putInt(bc.length).array());		
		return new Message(13,Message.REQUEST,pl.array());
	}
	public static Message cancel(BlockInfo bc){		
		ByteBuffer pl=ByteBuffer.allocate(4*3);
		pl.put(ByteBuffer.allocate(4).putInt(bc.index).array());
		pl.put(ByteBuffer.allocate(4).putInt(bc.offset).array());
		pl.put(ByteBuffer.allocate(4).putInt(bc.length).array());		
		return new Message(13,Message.CANCEL,pl.array());
	}
	public static Message piece(Block b){
		ByteBuffer pl=ByteBuffer.allocate(4*2+b.length());
		pl.put(ByteBuffer.allocate(4).putInt(b.index()).array());
		pl.put(ByteBuffer.allocate(4).putInt(b.offset()).array());
		pl.put(b.data);		
		return new Message(9+b.length(),Message.PIECE,pl.array());
	}	
}