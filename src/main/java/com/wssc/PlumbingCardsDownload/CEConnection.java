package com.wssc.PlumbingCardsDownload;

import java.util.Properties;
import java.util.logging.Level;
import javax.security.auth.Subject;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.UserContext;


 

public class CEConnection {

	static CEConnection instance;
	private static Properties props;
	private String uri;
	private String userName;
	private String password;
	private String domainName;
	private String objectStoreName;
	private String loginModule;
	private Connection con = null;
	private ObjectStore objStore = null;
	
	private String ePermitObjectStoreName;
	private ObjectStore ePermitObjectStore = null;
	private Domain domain = null;
	
	public static void main(String arg[]) {
		CEConnection ce = new CEConnection();
		ce.getObjectStore();
	}
	
	private CEConnection() {
		props = PropertyReader.getInstance().getPropertyBag();
		uri = props.getProperty("com.wssc.filenet.uri").trim();
		userName = props.getProperty("com.wssc.filenet.user").trim();
		password = props.getProperty("com.wssc.filenet.password").trim();
		domainName = props.getProperty("com.wssc.filenet.domain").trim();
		objectStoreName = props.getProperty("com.wssc.filenet.objectstore").trim();
		loginModule = props.getProperty("com.wssc.filenet.loginmodule").trim();
		//ePermitObjectStoreName = props.getProperty("epermit.wssc.filenet.os").trim();
	}
	
	public static CEConnection getInstance() {
		instance = new CEConnection();		
		return instance;
	}
	
	/**
	 * getConnection method returns the filenet connection object
	 * @return
	 */
	private Connection getConnection() {
		Connection con = null;
		try {
			props = PropertyReader.getInstance().getPropertyBag();
			con = Factory.Connection.getConnection(uri);
			Subject sub = UserContext.createSubject(con, userName, password, loginModule);
			UserContext uc = UserContext.get();
			uc.pushSubject(sub);
			
		}catch(EngineRuntimeException e ){
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return con;
	}
	
	/**
	 * getObjectStore method return FileNet ObjectStore using connection and domain objects
	 * @return
	 */
	public ObjectStore getObjectStore() {
		try {
			domain = Factory.Domain.fetchInstance(getConnection(),domainName,null);
			objStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
			//System.out.println("Stubs and Checks Object Store:"+objStore.get_Name());
		}catch(EngineRuntimeException e ){
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			con = null;
		}
		return objStore;
	}
	
	
}
