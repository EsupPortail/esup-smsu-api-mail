package org.esupportail.smsuapimail.services.smsSender;

import java.util.List;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapi.services.client.HttpRequestSmsuapiWS;
import org.esupportail.smsuapi.services.client.SmsuapiWSException;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.SmsSenderException;
import org.springframework.beans.factory.annotation.Autowired;

public class ISmsSender {

	private final Logger logger = new LoggerImpl(getClass());

	@Autowired private HttpRequestSmsuapiWS ws;
	
	/**
	 * Sender id sent to the back office.
	 */
	private Integer senderId;

	/**
	 * Constructor
	 */
	public ISmsSender() {
		super();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.esupportail.smsuapimail.services.smsSender.ISmsSender#sendSms(SmsMessage smsMessage)
	 */
	public void sendSms(final SmsMessage smsMessage) throws SmsSenderException {
		
		final List<String> phoneNumbers = smsMessage.getPhoneNumbers();
		final String message = smsMessage.getContent();
		final String accountLabel = smsMessage.getAccount();
		
		final int nbSmsToSend = phoneNumbers.size();
		
		checkQuota(nbSmsToSend, accountLabel);
		sendMessage(phoneNumbers, message, accountLabel);
	}
	
	
	/**
	 * Quota check.
	 * @param nbSmsToSend
	 * @param accountLabel
	 * @throws SmsSenderException
	 */
	private void checkQuota(final int nbSmsToSend, final String accountLabel) throws SmsSenderException {

		try {
			
			if (logger.isDebugEnabled()) {
				logger.debug("Sending request mayCreateAccountCheckQuotaOk to back office with parameters : \n" + 
					     " - Nb of SMS : " + nbSmsToSend + "\n" + 
					     " - Account label : " + accountLabel);
			}
			
			ws.mayCreateAccountCheckQuotaOk(accountLabel, nbSmsToSend);
			
		} catch (SmsuapiWSException e) {
			throw new SmsSenderException("ERROR.BACK.OFFICE", e);
		} catch (java.lang.reflect.UndeclaredThrowableException e) {
			throw new SmsSenderException("ERROR.BACK.OFFICE", e.getCause());
		}
	}
	
	/**
	 * Send the message to the back office.
	 * @param phoneNumbers
	 * @param content
	 * @param accountLabel
	 */
	private void sendMessage(final List<String> phoneNumbers, final String content, final String accountLabel) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Sending request sendSMS to back office with parameters : \n" + 
				     " - Sender id : " + senderId + "\n" + 
				     " - Phone number : " + phoneNumbers + "\n" + 
				     " - Account label : " + accountLabel + "\n" + 
				     " - content : " + content);
		}
		
		ws.sendSms(null, phoneNumbers, content, accountLabel, senderId);
	}	
	
	/**
	 * Standard setter used by Spring.
	 * @param senderId
	 */
	public void setSenderId(final Integer senderId) {
		this.senderId = senderId;
	}
}
