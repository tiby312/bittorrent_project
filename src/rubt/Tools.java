package rubt;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Tools class contains miscellaneous functions 
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 */
public class Tools {
		/**
		 * getFreePort obtains an unused port
		 * @return a socket on a free port
		 * @throws IOException
		 */
		public static ServerSocket getFreePort() throws IOException{			
		   for (int tport=6881;tport<=6889;tport++) {
		        try {
		            return new ServerSocket(tport);
		        } catch (IOException ex) {
		            continue; // try next port
		        }
		    }			    
		    throw new IOException("no free port found");		    
		}
		/**
		 * generatePeerID generates a random PeerID beginning with GP11
		 * @return a ByteBuffer containing the randomly generated peer ID
		 */
		public static ByteBuffer generatePeerId()
		{
			int rang1='Z'-'A';		
			Random r=new Random();

			String peer_id = "GP11";				
			for(int i = 4; i < 20; i++)
			{
				//r.nextInt(2);
				int k=r.nextInt(rang1);
				peer_id+=(char)('A'+k);			
			}
			MLog.log("Generated Peer Id: "+new String(ByteBuffer.wrap(peer_id.getBytes()).array()));
			return ByteBuffer.wrap(peer_id.getBytes());

		}
		
		/**
		 * convertToEscapedHex converts given byte array into an escaped hex string
		 * @param ar The byte array to be converted
		 * @return   The escaped string
		 */
		public static String convertToEscapedHex(byte[] ar){		
			String hash="";
			for(int i=0;i<ar.length;i++){
				hash+="%"+String.format("%02X", (int)(ar[i] & 0xFF));
			}
			return hash;
		}

		/**
		 * equals compares two byte arrays for equality
		 * @param b1 first byte array to be compared
		 * @param b2 second byte array to be compared
		 * @return   boolean true if equal, false if not
		 */
		public static boolean equals(byte[] b1, byte[] b2){
			  if (b1 == null && b2 == null){
			    return true;
			  }
			  if (b1 == null || b2 == null){
			    return false;
			  }
			  if (b1.length != b2.length){
			    return false;
			  }
			  for (int i=0; i<b1.length; i++){
			    if (b1[i] != b2[i]){
			      return false;
			    }
			  }
			  return true;
			}
	}



