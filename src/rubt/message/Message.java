
package rubt.message;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;




/**
 * Message class handles the creation and sending of messages between Peer and Client.
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 */
public class Message{
	
	public static class InvalidMessageException extends Throwable{
		private static final long serialVersionUID = 1L;
	}
	
	//Message IDs
	public final static byte KEEP_ALIVE   = -1;
	public final static byte CHOKE        = 0;
	public final static byte UNCHOKE      = 1;
	public final static byte INTERESTED   = 2;
	public final static byte UNINTERESTED = 3;
	public final static byte HAVE         = 4;
	public final static byte BITFIELD	   = 5;
	public final static byte REQUEST      = 6;
	public final static byte PIECE        = 7;	
	public final static byte CANCEL	   = 8;
	
		
	public final int length;
	public final byte id;
	public final byte[] payload;		
	
	
	/**
	 * Constructor for a KEEPALIVE message.
	 */
	public Message(){ 
		length=0;
		this.id=(byte)-1;
		this.payload=new byte[0];
	}
	
	
	/**
	 * Constructor for all other message types.
	 * @param len     the length of the message
	 * @param id      message ID to identify the message type
	 * @param payload the data in the message
	 */
	public Message(int len,int id,byte[] payload){		
		length=len;
		this.id=(byte)id;		
		this.payload=payload;						
	}
	
	
	/**
	 * Writes the message to the DataOutputStream. 
	 * @param writer       the output stream for the function to write to
	 * @throws IOException signals that an I/O exception of some sort has occurred (IO failed or interrupted)
	 */
	public void sendMessage(DataOutputStream writer) throws IOException{			
		ByteBuffer bb=ByteBuffer.allocate(length+4);		
		bb.put(ByteBuffer.allocate(4).putInt(length).array());
		if(length>0){
			bb.put(id);
		}
		bb.put(payload);
		writer.write(bb.array());
		writer.flush();
	}
	
	
	/**
	 * Converts data fields in a Message object to a string for console output.
	 */
	@Override
	public String toString(){
		String s="[length="+length+",id="+id + "]";
		
		
		/*

		final int MAXPAYLOADSIZE=50;
		String r =s;
		int aa;
		if(payload.length>MAXPAYLOADSIZE){
			r="...";
			aa=MAXPAYLOADSIZE;
		}else{
			r="";
			aa=payload.length;
		}
		
		for(int i=0;i<aa;i++){			
			s+=String.format("%02X", (int)(payload[i] & 0xFF));
		}
		
		s+=r+"}]";
		*/
		String custom="";
		String name="";
		switch (id){
		case Message.KEEP_ALIVE:
			name="keep alive";
			break;
		case Message.CHOKE:
			name="choke";
			break;
		case Message.UNCHOKE:
			name="unchoke";
			break;
		case Message.INTERESTED:
			name="interested";
			break;
		case Message.UNINTERESTED:
			name="uninterested";
			break;
		case Message.HAVE:
			name="have";
			custom+=MParse.have(payload);
			break;
		case Message.BITFIELD:
			name="bitfield";
			custom+=MParse.bitField(payload);
			break;
		case Message.REQUEST:
			name="request";
			custom+=MParse.request(payload);
			break;
		case Message.PIECE:
			name="piece";
			custom+=MParse.piece(payload);
			break;	
		case Message.CANCEL:
			name="cancel";
			custom+=MParse.cancel(payload);
		default:
			break;
		}		
		return name+s+custom;  		
	}
	
}


