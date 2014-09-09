package rubt.peer.keepalive;


public interface PeerKeepAliveListener{
	public void onRequestSendKeepAlive();
	public void onRequestDestroy();
}
