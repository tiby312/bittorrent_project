package rubt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * @author Ken Reed
 * @author Eric Brugel
 * @author Scott Xu
 *
 * MLog is our Logger and is responsible for recording messages for each event that occurs.  
 *
 */

public class MLog {

    public final static boolean DEBUG = true;

    static File file;
    static PrintWriter writer;
    public static void init(String filename){
    	File k=new File(filename);
    	try {
			writer=new PrintWriter(k);
		} catch (FileNotFoundException e) {
			System.out.println("could ont create logging file");
		} 					
    }
    public static void error(String message){    	
    	MLog.log(message);
    	System.exit(0);
    }
    
    public static void logNotice(String message){
    	System.out.println(message);
    	MLog.log(message);
    }
    public static void log(String message)
    {
    	
        if (DEBUG)
        {        	
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();            
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
            
            String s=className + "." + methodName + "() " + lineNumber + ": " + message+"\n";                        
            System.out.println(s);
            //writer.write(s);
        }
    }
    public static void close(){
    	writer.close();    	
    }

}
