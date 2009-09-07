package org.esupportail.smsuapimail.services.smsSender;

import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.SmsSenderException;

/**
 * Common interface used to send SMS to back office.
 * @author prqd8824
 *
 */
public interface ISmsSender {
	
	/**
	 * Send SMS to the back office.
	 * @param mailToSmsMessageList
	 * @throws SmsSenderException
	 */
	void sendSms(SmsMessage smsMessage) throws SmsSenderException; 
}
