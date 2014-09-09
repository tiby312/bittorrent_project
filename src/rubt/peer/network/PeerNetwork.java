package rubt.peer.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import rubt.MLog;
import rubt.message.Message;
import rubt.peer.PeerEmbryo;
import tools.MEvent;
import tools.MEventLis;





class PeerNetworkEvent{
	ArrayList<PeerNetworkListener> listeners=new ArrayList<PeerNetworkListener>();
	void register(PeerNetworkListener l){
		listeners.add(l);
	}
	void fireNewMessage(Message m){
		for(PeerNetworkListener l:listeners){
			l.onNewMessage(m);
		}
	}
	void fireRequestDestroy(){
		for(PeerNetworkListener l:listeners){
			l.onRequestDestroy();
		}
	}
	void fireSendMessage(Message m){
		for(PeerNetworkListener l:listeners){
			l.onSendMessage(m);
		}
	}
}


//the read and write thread extend this as well as peer network
//only these two functions need to be mutually exclusive so we put it in its own class
//to take advangage of synchronized functions and just extend it
class LivingObj {
	private boolean alive = false;

	synchronized void setAlive(boolean a) {
		alive = a;
	}

	synchronized boolean isAlive() {
		return alive;
	}
}

//in charge of network layer
public class PeerNetwork extends LivingObj {
	private Socket socket;
	protected PeerReader reader;
	private PeerWriter writer;
	private Thread readerThread;
	private Thread writerThread;
	private PeerNetworkEvent event;

	public PeerNetwork(PeerEmbryo pe) {
		event=new PeerNetworkEvent();
		reader = new PeerReader(pe.reader);
		writer = new PeerWriter(pe.writer);
		readerThread = new Thread(reader);
		writerThread = new Thread(writer);		
		this.socket=pe.socket;
	}

	public void run() {
		this.setAlive(true);
		readerThread.start();
		writerThread.start();
		
	}

	public void registerListener(PeerNetworkListener l){
		event.register(l);
	}

	public void destroy() {
		MLog.log("destorying peer" + this);
		this.setAlive(false);
		readerThread.interrupt();
		writerThread.interrupt();
		reader.close();
		writer.close();
		try {
			socket.close();
		} catch (IOException e1) {
			MLog.log("problem closing peer socket");
			
		}
		
		//MLog.log("STOP PEER START");
		while (true) {
			try {
				readerThread.join();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
		//MLog.log("READER STOPPED");
		while (true) {
			try {
				writerThread.join();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
		//MLog.log("WRITER STOPPED");
	}

	public void sendMessage(Message m) {		
		writer.queue.add(m);
		event.fireSendMessage(m);
	}


	private class PeerReader implements Runnable {
		DataInputStream reader;

		// NewMessageEvent newmessage;

		PeerReader(DataInputStream reader) {
			// newmessage=new NewMessageEvent();
			this.reader = reader;
		}

		@Override
		public void run() {
			//setAlive(true);
			while (isAlive()) {
				try {
					//MLog.log("listening");
					Message m = readMessage();
					//MLog.log("new message!");
					//PeerNetwork.this.onNewMessage(m);
					PeerNetwork.this.event.fireNewMessage(m);
				} catch (IOException e) {
					continue;
				}
			}
		}

		private Message readMessage() throws IOException {

			byte[] z = new byte[4];

			reader.readFully(z);

			int messageLength = ByteBuffer.wrap(z).getInt();

			if (messageLength == 0) { // KeepAlive message
				return new Message();
			}

			int id = reader.readByte();
			int payloadLength = messageLength - 1;

			if (payloadLength > 0) { // Message contains a data block
				byte[] pl = new byte[payloadLength];
				reader.readFully(pl);
				return new Message(messageLength, id, pl);
			} else { // Message is not data
				return new Message(messageLength, id, new byte[0]);
			}

		}	
		public void close(){
			try {
				reader.close();
			} catch (IOException e) {
				MLog.log("couldn't close reader");
			}
		}

	}

	private class PeerWriter implements Runnable {
		DataOutputStream writer;

		BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();

		PeerWriter(DataOutputStream writer) {
			this.writer = writer;
		}

		@Override
		public void run() {
			//setAlive(true);
			while (isAlive()) {
				try {
					Message m = queue.take();					
					m.sendMessage(writer);
				} catch (InterruptedException e) {
					continue;
				} catch (IOException e) {
					MLog.log("failed to send message requesting to be destroyed");
					PeerNetwork.this.event.fireRequestDestroy();
					//e.printStackTrace();
				}
			}
		}	
		void close(){
			try {
				writer.close();
			} catch (IOException e) {
				MLog.log("couldn't close writer");
			}			
		}
	}

}