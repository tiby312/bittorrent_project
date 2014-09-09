package rubt.peer;

import java.net.Socket;

public interface NewPeerConnectListener {
		void newEvent(Socket c);	//This will be called SYNCHRONOUSLY
}
