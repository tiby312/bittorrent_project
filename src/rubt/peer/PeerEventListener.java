package rubt.peer;

import rubt.commander.Commander;
import rubt.piece.BlockInfo;
import rubt.piece.FinishedPiece;


/**
 * ALL THESE FUNCTIONS WILL BE CALLED SYNCRUNOUSLYSDFLSDKJF
 * @author kr368
 *
 */
public interface PeerEventListener{
	
	
	//void onNewEvent(Commander.MCommand co);
	
	void onChoke(PeerInt p); //called when we peer goes from unchoke to choke
	void onUnChoke(PeerInt p);
	void onInterested(PeerInt p); //called when peer goes from uninterested to interseted
	void onUninterested(PeerInt p);
	void onHave(PeerInt p,int i); //passes the new piece that we now know they have
	void onBitfield(PeerInt p); //don't pass anything. if you want to see what the peer has look at its bitfield instead.
	void onRequest(PeerInt p,BlockInfo b);
	
	
	//void onSentBlock(Peer p,Block b);//called when peer successfully sends a block
	
	void onFinishedPiece(PeerInt p,FinishedPiece fp); //piece is not validated at this point, but we did get all blocks
	
	void onFinishedPieceFail(PeerInt p,int i);//peer failed to download its working piece. maybe dropped a block or too slow
	
	void onRequestAWorkingPiece(PeerInt p);//peer wants to get a new piece to work on.
	
	
	void onRequestDestroy(PeerInt p);//the peer wants to die
}