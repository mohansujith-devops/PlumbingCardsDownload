/**
 * 
 */
package com.wssc.PlumbingCardsDownload;

import java.io.IOException;
import java.util.Properties;

/**
 * @author MGOKAP
 *
 */
public class PropertyReader {

	static PropertyReader propertySingleton = null;
	private Properties propertyBag = new Properties();
	
	private PropertyReader() { 
		
		try {
			
			propertyBag.load(this.getClass().getResourceAsStream("global.properties"));
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch(NullPointerException e) {
			e.printStackTrace();
			
		}catch(Exception e){
			e.printStackTrace();
			
		}
	}
	
	/**
	 * @return Returns singleton handle for Property reader
	 */
	public static PropertyReader getInstance() {
	
		if(propertySingleton == null)
			propertySingleton = new PropertyReader();
		return propertySingleton;
	}
	
    /**
     * getValueAsString method returns the value for the specified key
     * @param key
     * @return
     */
    public String getValueAsString(String key){
    	
    	return (propertyBag.getProperty(key)).trim();
    	
    }
    public Properties getPropertyBag(){
    	
    	return propertyBag;
    }
}
