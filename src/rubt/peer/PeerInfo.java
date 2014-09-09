package rubt.peer;

import java.nio.ByteBuffer;

import rubt.Tools;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * PeerInfo class is a container to store info about each Peer.  PeerInfo keeps track of peer ID, ip address, and port number.  
 *
 */
public class PeerInfo {
	public final ByteBuffer peer_id;
	public final String ip;
	public final int port;
	public final boolean official;

	public PeerInfo(ByteBuffer peer_id, String ip, int port) {
		this.peer_id = peer_id;
		this.ip = ip;
		this.port = port;

		
		String kk="-RU11"; //TODO set official		
		if (new String(peer_id.array()).startsWith(kk)) {
			official = true;
		} else {
			official = false;
		}

	}

	public String toString() {
		String s = "";
		s += ip + ":" + port + ":" + new String(peer_id.array());
		return s;
	}
	public boolean equals(Object o){
		if(o instanceof PeerInfo){
			PeerInfo j=(PeerInfo)o;
			if(Tools.equals(peer_id.array(),j.peer_id.array())){
				return true;
			}			
		}
		return false;
	}
}