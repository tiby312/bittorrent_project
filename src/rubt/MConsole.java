package rubt;

import java.util.Scanner;

import rubt.torrentjob.TorrentJobInt;


/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 * MConsole is responsible for handling the console input/output to interface with the end-user.  
 *
 */

public class MConsole{
	private Scanner reader;
	private Thread consoleThread;
	private MRun mrun;
	private TorrentJobInt torrentjob;
	MConsole(TorrentJobInt torrentjob){
		 reader = new Scanner(System.in);
		 mrun=new MRun();
		 consoleThread=new Thread(mrun);
		 this.torrentjob=torrentjob;
	}
	void start(){
		consoleThread.start();
	}
	private class MRun implements Runnable{
		public void run(){
			while(true){
				System.out.println("Torrent is running. Valid Commands=status,peers,quit. Log is saved to log.txt>>");
				 //get user input for a
				String s=reader.nextLine().toLowerCase();			
				if(s.equals("quit")){
					MLog.logNotice("quitting the program");
					//torrentjob.shutdown();
					//torrentjob.pushCommand(torrentjob.new ShutDownCommand());
					torrentjob.shutdownStart();										
					torrentjob.join();
					MLog.log("joined");
					break;
				}else if(s.equals("status")){
					torrentjob.printStatus();
					
				}else if(s.equals("peers")){
					torrentjob.printPeers();
				}
				 
			}
			System.out.println("console finish");
		}	
		
	}
	void join(){
		while(true){
			try {
				consoleThread.join();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
}