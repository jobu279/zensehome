package zensemqtt;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Log {
    static FileHandler fh; 
    static Logger logger = Logger.getLogger("ZenseMQTT");  


	public static void logging(String desc) {
		logging("info", desc);
	}
	
	public static void logging(String type, String desc) {

	    try {  
	    	Calendar c = Calendar.getInstance();
	    	int year = c.get(Calendar.YEAR);
	    	int month = c.get(Calendar.MONTH) + 1;
	    	String path = new File("logs/" + year + "-" + month + "_ZenseLog.log").getAbsolutePath();

	    	// This block configure the logger with handler and formatter  
	        fh = new FileHandler(path, true);  // true appends instead of creating new files
	        logger.addHandler(fh);
	        //logger.setUseParentHandlers(false); //don't show in console
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  

	        // the following statement is used to log any messages  
	        if (type.contentEquals("err")) {
		        logger.severe(desc);
	        } else {
		        logger.info(desc);  
	        }
	    } catch (SecurityException e) {
	        e.printStackTrace();
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  finally {
	        fh.flush();
	        fh.close();	
	    }
	}
}
