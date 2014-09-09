package rubt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import rubt.torrentjob.TorrentInfoPlus;
import rubt.torrentjob.TorrentJobInt;
import rubt.torrentjob.TorrentJobManager;

import edu.rutgers.cs.cs352.bt.exceptions.BencodingException;

/**
 * Client class runs the TorrentJob thread
 * @author Ken Reed
 * @author Eric Brugel 
 * @author Scott Xu
 *
 */

public class Client {
	
	/**
	 * Reads the torrent file and creates a TorrentJob. Runs the TorrentJobControl thread with the TorrentJob object.  
	 * @param args Don't worry about it. 
	 */
	public static void run(String[] args){
		if(args.length!=2){
			System.out.println("you must enter 1) torrent file 2) outfile");
			return;
		}
		try {
			MLog.init("log.txt");
			byte[]torrent = readTorrentFile(args[0]);
			TorrentInfoPlus torrentInfo = new TorrentInfoPlus(torrent);
			
			TorrentJobInt t = TorrentJobManager.createTorrentJob(torrentInfo,args[1]);			
			t.start();			
			
			MConsole console=new MConsole(t);
			console.start();			
			console.join();
						
			MLog.close();
		} catch (IOException e) {			
			System.out.println(e);
		} catch (BencodingException e) {
			System.out.println("problem with bencoding");
		} 
		
	}
	
		/**
		 * readTorrentFile converts the contents of a torrent file to a byte array.  
		 * @param s Name of torrent to be read.
		 * @return  A byte array with the contents of the torrent file. 
		 * @throws IOException
		 */
		public static byte[] readTorrentFile(String s) throws IOException{
			File file = new File(s);					
			byte[] b=new byte[(int)file.length()];
			
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				fileInputStream.read(b);
				fileInputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return b;		 		
		}
}
