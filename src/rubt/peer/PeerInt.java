package rubt.peer;

import java.nio.ByteBuffer;

import rubt.BitField;
import rubt.BitFieldArray;
import rubt.piece.Block;
import rubt.piece.PieceBuilder;

public interface PeerInt {
	public void run();
	public void startWorkingOnPiece(PieceBuilder p);
	public void register(PeerEventListener l);
	public void destroy();
	public boolean hasSameID(ByteBuffer a);

	public BitFieldArray getAvaiablePieces();
	public String toString();
	
	
	public boolean AreInterested();
	
	public void sendAndSetAreInterested();//we are interested in THEM
	public void sendAndSetUnInterested();//we are not interested in THEM
	public void sendHave(int index);
	public void sendBitField(BitField a);
	public void sendPiece(Block b);	
	public void choke(); //choke THEM
	public void unchoke(); //unchoke THEM
}

