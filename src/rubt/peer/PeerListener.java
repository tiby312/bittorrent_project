package rubt.peer;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import rubt.MLog;
import rubt.Tools;
import rubt.commander.Commander;
import rubt.commander.Commander.MCommand;

/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * PeerListener listens on the server port, waiting for peers to connect.  When it finds a connection, it initiates the HandShakeTask.
 *
 */



class NewPeerConnectEvent{
	ArrayList<NewPeerConnectListener> listeners=new ArrayList<NewPeerConnectListener>();
	public void register(NewPeerConnectListener e){
		listeners.add(e);
	}
//	public void fireNewPeerConnect(){
//		for(NewPeerConnectListener e:listeners){
//			e.newPeerConnect();
//		}
//	}
	public void fireNewEvent(Socket c){
		for(NewPeerConnectListener e:listeners){
			e.newEvent(c);
		}
	}
}

public class PeerListener{
	//private TorrentJob job;
	
	//private ExecutorService handshaker;
	//private ExecutorService handshaker=Executors.newCachedThreadPool();
	NewPeerConnectEvent event=new NewPeerConnectEvent();
	private Thread thread;
	private PeerListenerTask task;
	private boolean running=false;
	ServerSocket server;
	Commander commander;
	public PeerListener(Commander commander){
		this.commander=commander;
		try {
			server =  Tools.getFreePort();
		} catch (IOException e) {
			MLog.error("could not get a free port");
		}
		//this.job = torrentJobControl;
		MLog.log("server started");
		task=new PeerListenerTask();
		thread=new Thread(task);
		
	}

	public void register(NewPeerConnectListener l){
		event.register(l);
	}
	public int getPort(){
		return server.getLocalPort();
	}
	
	public void start(){
		running=true;
		thread.start();
	}
	
	public void stop(){
		running=false;
		//handshaker.shutdownNow();
		try {
			server.close();
		} catch (IOException e1) {
			MLog.log("error closing server");
		}
		while(true){
			try {
				thread.join();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
	
	private class PeerListenerTask implements Runnable{
		@Override
		public void run() {
			MLog.log("running server");
			//!server.isClosed()
			while(running){
				try {
					Socket connection = server.accept();
					MLog.log("got a peer connection");
					if(connection!= null){
						commander.pushCommand(new NewPeerCommand(connection));																	
					}
				}catch (IOException e) {
					if(running){
						MLog.log("failed to connect to a peer's socket");
					}else{
						break;
					}
				}
			}
			MLog.log("Server is closing");
		}
	}
	private class NewPeerCommand implements Commander.MCommand{
		Socket connection;
		NewPeerCommand(Socket connection){
			this.connection=connection;
		}
		@Override
		public void doCommand() {
			event.fireNewEvent(connection);			
			
		}
		
	}

}


	

