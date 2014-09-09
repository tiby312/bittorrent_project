package rubt.announcer;


/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 * 
 * AnnounceType is used to to decide what to put as the event parameter when sending an announce request. 
 *
 */

public abstract class AnnounceType{
	final String str;
	AnnounceType(String str){this.str=str;};
	public static class Start extends AnnounceType{
		public Start(){
			super("started");			
		}		
	}
	public static class Stop extends AnnounceType{
		public Stop(){
			super("stopped");			
		}		
	}
	public static class Complete extends AnnounceType{
		public Complete(){
			super("completed");			
		}		
	}
	public static class Nothing extends AnnounceType{
		public Nothing(){
			super("");
		}
		@Override
		public String getArg(){
			return "";
		}
	}
	public String getArg(){
		return "&event="+str;
	}

}