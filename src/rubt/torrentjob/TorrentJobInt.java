package rubt.torrentjob;


public interface TorrentJobInt {
	public void printStatus();
	public void printPeers();
	public void shutdownStart();
	public void join();//only returns when finished shutting down
	public void start();
	public String toString();
}
