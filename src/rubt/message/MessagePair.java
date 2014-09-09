package rubt.message;


import rubt.peer.PeerInt;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * MessagePair is a container used to group a peer with the message that it sends.
 *
 */
public class MessagePair{
	public MessagePair(Message m,PeerInt p){
		this.m=m;
		this.p=p;
	}
	public final Message m;
	public final PeerInt p;
	public String toString(){
		return m+"  "+p;
	}
}
