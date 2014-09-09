package rubt.commander;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import rubt.MLog;





/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * The TorrentJobCommander class reads from a blocking queue of commands.
 * There are many different types of commands, and most of the commands will be handle message commands.    
 *
 */
public class Commander{
	//Only need one for entire program
	static private ExecutorService pool=Executors.newCachedThreadPool();	
	private boolean shutdown=false;
	private MCommandQueue commandqueue=new MCommandQueue();	
	private Thread thread;
	private CommanderTask task;
	private ArrayList<ASyncCommand> asyncs=new ArrayList<ASyncCommand>();
	public Commander(){
		task=new CommanderTask();
		thread=new Thread(task);		
	
	}
	/**
	 * Interface commands must implement so that the commander can execute them synchronously
	 * @author kr368
	 *
	 */
	public interface MCommand {
		void doCommand();
	}
	
	public interface ASyncInt{
		void start();
		void async();
		void finish();
		void cancel();
	}

	
	
	
	private class ASyncCommand implements MCommand{
		int stage=0;
		ASyncInt ba;
		
		ASyncCommand(ASyncInt ba){
			this.ba=ba;
		}
		@Override
		public void doCommand() {			
			switch(stage){
				case 0:
					
					ba.start();
					asyncs.add(this);
					stage++;
					pool.execute(new Blep(this));										
					break;
				case 2:
					ba.finish();
					asyncs.remove(this);
					break;
				default:
					MLog.error("SHOULD NEVER HAPPEN "+stage);
					
			}						
		}						
		private class Blep implements Runnable{
			ASyncCommand co;
			
			Blep(ASyncCommand co){
				this.co=co;				
			}
			public void run(){
				co.ba.async();
				stage++;
				pushCommand(ASyncCommand.this);
		
			}
		}
	}
	

	//start executing commands
	public void start(){
		thread.start();
	}
	
	
	/*
	 * we call this function to wait to join the thread
	 */
	public void join(){		
		while(true){
			try {
				thread.join();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	

	
	
	
	
	
	private class CommanderTask implements Runnable{ 
		public void run(){						
			while(!shutdown){								
					try {									
						MCommand command=commandqueue.read();						
						command.doCommand();						
					} 
					catch (InterruptedException e1) {
						continue;
					}				
			}
			
			//pool.shutdownNow();
			for(ASyncCommand ca:asyncs){
				ca.ba.cancel();
			}
			try {
				pool.awaitTermination(1,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				
			}
			asyncs.clear();
			pool.shutdown();
			MLog.log("commander shutdown");
		}
	}

	private class MCommandQueue{
		private BlockingQueue<MCommand> queue=new LinkedBlockingQueue<MCommand>();
		public synchronized void push(MCommand m){
			queue.add(m);
		}
		public MCommand read() throws InterruptedException{		
			while(true){		
				return  queue.take();			
			}		
		}
	}
	public void pushCommand(MCommand c){
		commandqueue.push(c);
	}
	public void pushASync(ASyncInt s){
		ASyncCommand ca=new ASyncCommand(s);
		asyncs.add(ca);
		commandqueue.push(ca);
	}

	public class ShutDownCommand implements Commander.MCommand{
		@Override
		public void doCommand() {
			shutdown=true;			
		}		
	}
	
}
