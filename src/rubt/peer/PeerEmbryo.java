package rubt.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * PeerEmbryo is a skeleton class for the Peer class.  
 *
 */
public class PeerEmbryo{
	public final DataInputStream reader;
	public final DataOutputStream writer;
	public final PeerInfo peerinfo;
	public final byte[] infoHash;
	public final Socket socket;
	public PeerEmbryo(Socket socket,DataInputStream r,DataOutputStream w,PeerInfo pi,byte[] in){
		this.socket=socket;
		reader=r;
		writer=w;
		peerinfo=pi;
		infoHash=in;
	}
	public void close(){
		try {
			reader.close();
			writer.close();
			socket.close();
			
		} catch (IOException e) {
			
		}
		
		
	}
}