package org.esupportail.smsuapimail.services.smsSender.ws;

import java.io.Serializable;

import org.esupportail.smsuapimail.exceptions.InsufficientQuotaException;
import org.esupportail.smsuapimail.exceptions.UnknownIdentifierApplicationException;

/**
 * The interface of the information remote service.
 * @author prqd8824
 *
 */
public interface ISendSms extends Serializable {

	/**
	 * check Quota. 
	 * @return true or false.
	 */
	Boolean isQuotaOk(Integer nbDest, String labelAccount)	throws UnknownIdentifierApplicationException, 
																   InsufficientQuotaException;
	
	/**
	 * send SMS.
	 */
	void sendSMS(Integer msgId, Integer perId, Integer bgrId, Integer svcId, String smsPhone, String labelAccount, String msgContent);
	

}
