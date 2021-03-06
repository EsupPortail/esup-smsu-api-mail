package org.esupportail.smsuapimail.services.smsSender.ws;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author xphp8691
 *
 */
public class InitSSLParameters implements InitializingBean{

	/**
	 * trustStore.
	 */
	private String trustStore;

	/**
	 * trustStorepassword.
	 */
	private String trustStorePassword;
	
	/**
	 * keyStore.
	 */
	private String keyStore;
	
	/**
	 * keyStorePassword.
	 */
	private String keyStorePassword;

	
	/**
	 * A logger.
	 */
	private final Logger logger = Logger.getLogger(getClass());
	
	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		System.setProperty("javax.net.ssl.keyStore",
				this.keyStore);
		logger.info("used keystore: " + this.keyStore);
		System.setProperty("javax.net.ssl.keyStorePassword", 
				this.keyStorePassword);
		
		System.setProperty("javax.net.ssl.trustStore",
				this.trustStore);
		logger.info("used trustStore: " + this.trustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", 
				this.trustStorePassword);
	}
	
	/**
	 * the truststore is set for a servlet deployment.
	 * @param trustStore
	 */
	public void setTrustStore(final String trustStore) {
		this.trustStore = trustStore;
	}

	/**
	 * @return trustStore
	 */
	public String getTrustStore() {
		return trustStore;
	}

	/**
	 * @param trustStorepassword
	 */
	public void setTrustStorePassword(final String trustStorepassword) {
		this.trustStorePassword = trustStorepassword;
	}

	/**
	 * @return trustStorepassword
	 */
	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	/**
	 * the keystore is set for a servlet deployment.
	 * @param keyStore
	 */
	public void setKeyStore(final String keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * @return keyStore
	 */
	public String getKeyStore() {
		return keyStore;
	}

	/**
	 * @param keyStorePassword
	 */
	public void setKeyStorePassword(final String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	/**
	 * @return keyStorePassword
	 */
	public String getKeyStorePassword() {
		return keyStorePassword;
	}


}
