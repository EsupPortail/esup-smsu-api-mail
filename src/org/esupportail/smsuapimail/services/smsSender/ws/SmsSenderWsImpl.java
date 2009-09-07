package org.esupportail.smsuapimail.services.smsSender.ws;

import java.util.List;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.InsufficientQuotaException;
import org.esupportail.smsuapimail.exceptions.SmsSenderException;
import org.esupportail.smsuapimail.exceptions.UnknownIdentifierApplicationException;
import org.esupportail.smsuapimail.services.smsSender.ISmsSender;

/**
 * 
 * @author prqd8824
 *
 */
public class SmsSenderWsImpl implements ISmsSender {

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * WS implementation bean.
	 */
	private ISendSms sendSms;

	/**
	 * Message id sent to the back office.
	 */
	private Integer messageId;
	
	/**
	 * Sender id sent to the back office.
	 */
	private Integer senderId;
	
	/**
	 * Group sender id sent to the back office.
	 */
	private Integer groupSenderId;
	
	/**
	 * Service id sent to the back office.
	 */
	private Integer serviceId;
	

	/* (non-Javadoc)
	 * @see org.esupportail.smsuapimail.services.smsSender.ISmsSender#sendSms(java.util.List)
	 */
	public void sendSms(final SmsMessage smsMessage) throws SmsSenderException {
		
		final List<String> phoneNumbers = smsMessage.getPhoneNumbers();
		final String message = smsMessage.getContent();
		final String accountLabel = smsMessage.getAccount();
		
		final int nbSmsToSend = phoneNumbers.size();
		
		checkQuota(nbSmsToSend, accountLabel);
		
		for (String phoneNumber : phoneNumbers) {
			sendMessage(phoneNumber, message, accountLabel);
		}

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
				final StringBuilder sb = new StringBuilder(200);
				sb.append("Sending request isQuotaOk to back office with parameters : \n");
				sb.append(" - Nb of SMS : ").append(nbSmsToSend).append("\n");
				sb.append(" - Account label : ").append(accountLabel);
				logger.debug(sb.toString());
			}
			
			sendSms.isQuotaOk(nbSmsToSend, accountLabel);
			
		} catch (UnknownIdentifierApplicationException e) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Unable to send SMS to back office due to identification problem");
			logger.error(sb.toString(), e);
			throw new SmsSenderException(sb.toString(), e);

		} catch (InsufficientQuotaException e) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Unable to send SMS to back office due to quota problem");
			logger.error(sb.toString(), e);
			throw new SmsSenderException(sb.toString(), e);
		}
	}
	
	/**
	 * Send the message to the back office.
	 * @param phoneNumber
	 * @param content
	 * @param accountLabel
	 */
	private void sendMessage(final String phoneNumber, final String content, final String accountLabel) {
		
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Sending request sendSMS to back office with parameters : \n");
			sb.append(" - Message id : ").append(messageId).append("\n");
			sb.append(" - Sender id : ").append(senderId).append("\n");
			sb.append(" - Group sender id : ").append(groupSenderId).append("\n");
			sb.append(" - Service id : ").append(serviceId).append("\n");
			sb.append(" - Phone number : ").append(phoneNumber).append("\n");
			sb.append(" - Account label : ").append(accountLabel).append("\n");
			sb.append(" - content : ").append(content);
			logger.debug(sb.toString());
		}
		
		sendSms.sendSMS(messageId, senderId, groupSenderId, serviceId, phoneNumber, accountLabel, content);
	}
	
	/**
	 * Standard setter used by Spring.
	 * @param sendSms
	 */
	public void setSendSms(final ISendSms sendSms) {
		this.sendSms = sendSms;
	}

	/**
	 * Standard setter used by Spring.
	 * @param messageId
	 */
	public void setMessageId(final Integer messageId) {
		this.messageId = messageId;
	}


	/**
	 * Standard setter used by Spring.
	 * @param senderId
	 */
	public void setSenderId(final Integer senderId) {
		this.senderId = senderId;
	}


	/**
	 * Standard setter used by Spring.
	 * @param groupSenderId
	 */
	public void setGroupSenderId(final Integer groupSenderId) {
		this.groupSenderId = groupSenderId;
	}


	/**
	 * Standard setter used by Spring.
	 * @param serviceId
	 */
	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}
}
