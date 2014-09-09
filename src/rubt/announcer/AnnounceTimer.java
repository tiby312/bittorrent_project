package rubt.announcer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rubt.MLog;
import rubt.commander.Commander;
import rubt.message.Message;

import edu.rutgers.cs.cs352.bt.exceptions.BencodingException;



/**
 * Announce is a runnable object used to announce to the tracker.  It uses a schedule to determine when to reannounce.  
 * 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 */




class AnnounceTimerEvent{
	ArrayList<AnnounceTimerListener> listeners=new ArrayList<AnnounceTimerListener>();
	void register(AnnounceTimerListener l){
		listeners.add(l);
	}
	void fireAnnouncePlease(AnnounceTimer a){
		for(AnnounceTimerListener l:listeners){
			l.announcePlease(a);
		}
	}
}

public class AnnounceTimer{		
	public static class Fail extends Exception{
		public Fail(String string) {
			super(string);
		}

		private static final long serialVersionUID = 1L;
		
	}	
	final public int defaultAnnounceInterval=10; //in seconds
	final public int timeout=3;	
	private AnnounceTimerEvent event=new AnnounceTimerEvent();
	private ScheduledThreadPoolExecutor exec;
	
	
	/**
	 * @author Ken Reed 
	 * @author Eric Brugel
	 * @author Scott Xu
	 *
	 * AnnouncerTask is the task to be reannounced.  
	 * 
	 */
	
	private class AnnouncerTask implements Runnable{
		@Override
		public void run() {
			event.fireAnnouncePlease(AnnounceTimer.this);								
		}	
	}
	
	public void register(AnnounceTimerListener l){
		event.register(l);
	}
	/**
	 * announces to the tracker, if we are finished adds the finish event
	 * @param torrentJobControl
	 */
	public AnnounceTimer(){		
		exec=new ScheduledThreadPoolExecutor(1);	
	}
	public void resetTimer(int wait){
		exec.schedule(new AnnouncerTask(),wait,TimeUnit.SECONDS);
	}
	
	/**
	 * TODO EB JAVADOC
	 */
	public void shutdown(){			
		exec.shutdownNow();		
//		//job.pushASync(job.new AnnounceTask(Announcer.this,new AnnounceType.Nothing())); //TODO :LKDFJS:LKJFSD
//		try { //SLeep is bad. instead wait must join with exec thread.
//			Thread.sleep(timeout+100);
//		} catch (InterruptedException e) {
//			
//		}
		
	}
	
}
