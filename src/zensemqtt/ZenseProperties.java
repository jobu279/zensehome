package zensemqtt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ZenseProperties {
	InputStream inputStream;
	String result = "";

 
	public Map<String, String> getValues() {
        Map<String, String> map;
        map = new HashMap<String, String>();
		try {
			
			try {
				Properties prop = new Properties();
		    	//String propFileName = new File("zensemqtt/config.properties").getAbsolutePath();
				String propFileName = "zensemqtt/config.properties";
	 
				inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
	 
				if (inputStream != null) {
					prop.load(inputStream);
				} else {
					throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
				}
	  
				// get the property values and add to map
				map.put("mqttIP",prop.getProperty("mqttIP"));
				map.put("zenseIP",prop.getProperty("zenseIP"));
				map.put("zensePort",prop.getProperty("zensePort"));
				map.put("zenseID",prop.getProperty("zenseHomeBoxID"));			
			} catch (Exception e) {
				Log.logging("err", "Exception: " + e);
			} finally {
				inputStream.close();
			}
		} catch (IOException ioe) {
			Log.logging("err", "Exception: " + ioe);
		}
		return map;
	}
}