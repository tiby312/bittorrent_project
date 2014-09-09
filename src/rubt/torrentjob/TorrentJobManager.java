package rubt.torrentjob;

public class TorrentJobManager {
	public static TorrentJobInt createTorrentJob(TorrentInfoPlus torrentinfo,String targetFile){
		return new TorrentJob(torrentinfo,targetFile);
	}
}
