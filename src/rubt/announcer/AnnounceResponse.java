
package rubt.announcer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rubt.peer.PeerInfo;

import edu.rutgers.cs.cs352.bt.exceptions.BencodingException;
import edu.rutgers.cs.cs352.bt.util.Bencoder2;


//data structure of announce repsonse for easy parsing
/**
 * AnnounceResponse class used to parse the bencoded response from the tracker and store it in an easy-to-access format. The object is temporary is mainly used to make it easier for ourselves.    
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 */
public class AnnounceResponse {
	public final int announceInterval;
	public final ArrayList<PeerInfo> peers;
	/**
	 * Constructor for AnnounceResponse object. 
	 * @param data the bytearray of data to be decoded
	 * @throws BencodingException Bencoding error given by Rob.
	 */
	public AnnounceResponse(byte[] data) throws BencodingException{
		peers=new ArrayList<PeerInfo>();
		Object o=Bencoder2.decode(data);

		@SuppressWarnings("unchecked")
		Map<ByteBuffer,Object> map=(Map<ByteBuffer,Object>)o;

		int interval=(Integer)map.get(ByteBuffer.wrap("interval".getBytes()));
		announceInterval=interval;

		@SuppressWarnings("unchecked")
		List<Map<ByteBuffer,Object>> peers=(List<Map<ByteBuffer,Object>>)map.get(ByteBuffer.wrap("peers".getBytes()));

		for(Map<ByteBuffer,Object> peer:peers){
			String ip=new String( ((ByteBuffer)peer.get(ByteBuffer.wrap("ip".getBytes()))).array() );
			ByteBuffer peer_id= ((ByteBuffer)peer.get(ByteBuffer.wrap("peer id".getBytes()))) ;
			Integer port=(Integer)peer.get(ByteBuffer.wrap("port".getBytes()));

			PeerInfo pp=new PeerInfo(peer_id,ip,port);
			this.peers.add(pp);			
		}
	}
	public String toString(){
		String s="interval="+announceInterval;
		for(PeerInfo i:peers){
			s+=i+"\n";
		}
		return s;
	}
	
	public boolean contains(PeerInfo info){
		return peers.contains(info);
	}
	PeerInfo search(String ip, int port){
		for(PeerInfo p : peers){
			if(p.ip.compareTo(ip) == 0 && port == p.port){
				return p;
			}
		}
		return null;
	}
}

