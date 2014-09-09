
package rubt.torrentjob;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import rubt.MLog;
import rubt.PieceChooser;
import rubt.piece.Block;
import rubt.piece.BlockInfo;
import rubt.piece.FinishedPiece;
import rubt.piece.PieceInfo;

import edu.rutgers.cs.cs352.bt.TorrentInfo;

/**
 * TorrentFile class to validate downloaded pieces and write them to the file.  
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 */
public class TorrentFile {
	private RandomAccessFile file;
	private TorrentInfoPlus info;
	TorrentFile(){				
	}
	public TorrentFile(TorrentInfoPlus info){		
		this.info=info;				
	}
	
	/**
	 * init function. if the file doesnt exist, write to the end of the file to make sure the entire file is allocated.
	 * @param targetFile
	 * @throws IOException
	 */
	public void init(String targetFile) throws IOException{
		File k=new File(targetFile);
		file=new RandomAccessFile(k, "rw");	
				
		if(!k.exists()){				
			file.setLength(info.file_length);
			file.seek(info.file_length-4);
			file.write(2);
			file.seek(0);
		}	
	}
	/**
	 * retreives a block from file. this does not check if we have the piece. must first check if we have piece
	 * before calling this function to ensure the block of data retreives is right.
	 * @param bi
	 * @return
	 */
	Block retreiveBlock(BlockInfo bi){				
		try {
			byte[] data=new byte[bi.length];			
			int n1=bi.index*info.piece_length+bi.offset;			
			MLog.log("trying ot retreive block "+bi+"at pos="+n1/1000+","+(n1+bi.length)/1000);
			file.seek(n1);
			file.read(data);			
			Block block=new Block(bi,data);
			return block;				
		} catch (IOException e) {
			return null;
 		}	
		
	}
	
	/**
	 * returns null if not a valid piece
	 * @param info
	 * @param i
	 * @return
	 */
	public FinishedPiece readIthPiece(TorrentInfoPlus info,int i){
		int len=info.getPieceLength(i);				
		byte[] b=new byte[len];
		try {					
			file.seek(i*info.piece_length);					
			file.read(b);
		} catch (IOException e) {
			MLog.log("failed to read this section");
			return null;
		}
		FinishedPiece fp=new FinishedPiece(new PieceInfo(i,info),b);
		if(!validatePiece(info,fp)){
			return null;
		}
		return fp;		
	}
//	/*
//	 * returns true if we already have the file
//	 */
//	public boolean getFilePieces(TorrentInfoPlus info, PieceChooser piecechooser){
//		for(int i=0;i<info.piece_hashes.length;i++){				
//			int len=info.getPieceLength(i);				
//			byte[] b=new byte[len];
//			try {					
//				file.seek(i*info.piece_length);					
//				file.read(b);
//			} catch (IOException e) {
//				MLog.log("failed to read this section");
//				continue;
//			}				
//			FinishedPiece fp=new FinishedPiece(new PieceInfo(i,info),b);
//			if(!validatePiece(info,fp)){
//				MLog.log("don't have this piece in file");				
//			}else{
//				notstarted.set(fp.index(),false);
//				completed.set(fp.index(),true);
//			}
//			
//			//piecechooser.onfinishedPieceFromFile(fp);
//	
//			
//		}
//		if(piecechooser.completed()){
//			return true;
//		}
//		return false;
//	}
	void close(){
		try {
			file.close();
		} catch (IOException e) {
			MLog.log("failed to close file");
		}
	}
	
	/**
	 * writes a piece to the file we are downloading
	 * @param info
	 * @param piece
	 * @throws IOException
	 */
	boolean writePiece(TorrentInfo info,FinishedPiece piece)  {
		//System.out.println("writing="+p.index);
		
			//System.out.println(p);			
			try {
				file.seek(piece.index()*info.piece_length);
				file.write(piece.data);
				return true;
			} catch (IOException e) {
				return false;
			}
									
				
	}
	

	/**
	 * uses sha1 hash to validate piece
	 * @param info
	 * @param finishedPiece
	 * @return true if piece is valid
	 */
	public static boolean validatePiece(TorrentInfoPlus info,FinishedPiece finishedPiece){
		if(finishedPiece.index()<0||finishedPiece.index()>=info.getNumPieces()){
			return false;
		}

		ByteBuffer ba=info.piece_hashes[finishedPiece.index()];
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			byte[] hash = md.digest(finishedPiece.data);

			if(ba.equals(ByteBuffer.wrap(hash))){
				//System.out.println("verified!");
				return true;
			}



		} catch (NoSuchAlgorithmException e) {
			MLog.error("cannot find sha-1 algorithm");
		}	
		return false;
	}
	
	
}