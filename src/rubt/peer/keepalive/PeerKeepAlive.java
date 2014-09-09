package rubt.peer.keepalive;// in charge of peer keep alive code

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rubt.MLog;
import rubt.message.Message;
import rubt.peer.network.PeerNetworkListener;

/**
 * @author Scott Xu
 * @author Scott Xu
 * @author Scott Xu
 * 
 * PeerKeepAlive class is responsible for sending the KeepAlive messages for each peer.
 *
 */




class PeerKeepAliveEvent{
	ArrayList<PeerKeepAliveListener> listeners=new ArrayList<PeerKeepAliveListener>();
	void register(PeerKeepAliveListener l){
		listeners.add(l);
	}
	void fireRequestSendKeepAlive(){
		for(PeerKeepAliveListener l:listeners){
			l.onRequestSendKeepAlive();
		}
	}
	void fireRequestDestroy(){
		for(PeerKeepAliveListener l:listeners){
			l.onRequestDestroy();
		}
	}	
}

public class PeerKeepAlive implements PeerNetworkListener {
	private PeerKeepAliveEvent event;
	public PeerKeepAlive() {		
		event=new PeerKeepAliveEvent();
		keepAliveScheduler = new ScheduledThreadPoolExecutor(2);//TODO hard code?
				
	}
	public void run(){
		sendKeepAlive = keepAliveScheduler.schedule(new KeepAliveTask(),
				keepAliveInterval, TimeUnit.SECONDS);
		destroy = keepAliveScheduler.schedule(new DestroyTask(),
				keepAliveInterval, TimeUnit.SECONDS);
	}
	public void register(PeerKeepAliveListener p){
		event.register(p);
	}
	private class KeepAliveTask implements Runnable {
		public void run() {
			//if (PeerKeepAlive.this.isAlive()) {
				//sendMessage(MCreates.keepAlive());
				event.fireRequestSendKeepAlive();
				MLog.log("sent keep alive");				
			//}
		}
	}

	private class DestroyTask implements Runnable {
		public void run() {

			//if (PeerKeepAlive.this.isAlive()) {
				MLog.log("keep alive timed out destroying peer "
						+ this.toString());
				onRequestDestroy();
			//}

		}
	}

	public void destroy() {
		MLog.log("destory keep alive thread");
		//super.destroy();
		//peernetwork.destroy();
		keepAliveScheduler.shutdownNow();		
	}

	
	//private PeerNetwork peernetwork;
	private final long keepAliveInterval = 120; // in seconds
	private final long destroyInterval = keepAliveInterval + 20;
	private ScheduledThreadPoolExecutor keepAliveScheduler;
	private ScheduledFuture<?> destroy; // if we dont recieve messages destroy
	// peer
	private ScheduledFuture<?> sendKeepAlive; // if we havent sent messages send

	// keep alive
	private void resetSendKeepAlive() {		
		sendKeepAlive.cancel(false);		
		sendKeepAlive = keepAliveScheduler.schedule(new KeepAliveTask(),
				keepAliveInterval, TimeUnit.SECONDS);
	}

	void resetDestroy() {		
		destroy.cancel(false);		
		destroy = keepAliveScheduler.schedule(new DestroyTask(),
				destroyInterval, TimeUnit.SECONDS);
	}


	@Override
	public void onSendMessage(Message m) {
		resetSendKeepAlive();			
	}
	@Override
	public void onNewMessage(Message m) {		
		resetDestroy();
	}
	@Override
	public void onRequestDestroy() {}

}