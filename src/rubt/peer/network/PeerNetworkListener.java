package rubt.peer.network;

import rubt.message.Message;

public interface PeerNetworkListener{
	public void onNewMessage(Message m);
	public void onRequestDestroy();
	public void onSendMessage(Message m);
}
