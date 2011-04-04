package org.esupportail.smsuapimail.services.smsSender.ws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	 * a file to save and initialize the message ID
	 */
	private String fileName;


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
	

	/**
	 * Constructor
	 */
	public SmsSenderWsImpl() {
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
		messageId ++;
		saveMessageId();
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
				logger.debug("Sending request mayCreateAccountCheckQuotaOk to back office with parameters : \n" + 
					     " - Nb of SMS : " + nbSmsToSend + "\n" + 
					     " - Account label : " + accountLabel);
			}
			
			sendSms.mayCreateAccountCheckQuotaOk(nbSmsToSend, accountLabel);
			
		} catch (UnknownIdentifierApplicationException e) {
			String s = "Unable to send SMS to back office due to identification problem";
			logger.error(s, e);
			throw new SmsSenderException(s, e);

		} catch (InsufficientQuotaException e) {
			String s = "Unable to send SMS to back office due to quota problem";
			logger.error(s, e);
			throw new SmsSenderException(s, e);
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
			logger.debug("Sending request sendSMS to back office with parameters : \n" + 
				     " - Message id : " + messageId + "\n" + 
				     " - Sender id : " + senderId + "\n" + 
				     " - Group sender id : " + groupSenderId + "\n" + 
				     " - Service id : " + serviceId + "\n" + 
				     " - Phone number : " + phoneNumber + "\n" + 
				     " - Account label : " + accountLabel + "\n" + 
				     " - content : " + content);
		}
		
		sendSms.sendSMS(messageId, senderId, groupSenderId, serviceId, phoneNumber, accountLabel, content);
	}
	
	/**
	 * save the current messageID in a file.
	 */
	private void saveMessageId() {
		PrintWriter pw;
	    
		try {
			pw =  new PrintWriter(new BufferedWriter
			   (new FileWriter(fileName)));
			pw.println(messageId);
			pw.close();
		} catch (IOException e) {
			logger.warn("Enable to save messageId to file " + fileName, e);
		}
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
	
	/**
	 * Standard setter used by Spring.
	 * @param fileName
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
		try {
			File ips=new File(fileName);
			InputStreamReader ipsr=new FileReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			ligne=br.readLine();
			if (ligne == null){
				messageId = 1;
				saveMessageId();
			} else {
				try {
					messageId = Integer.valueOf(ligne);
				} catch (NumberFormatException e) {
					messageId = 1;
					saveMessageId();
				}
			}
			br.close();
			} catch (FileNotFoundException e) {
				messageId = 1;
				saveMessageId();
			} catch (IOException e) {
				messageId = 1;
				saveMessageId();
			}
			
			
			
			if (logger.isDebugEnabled()) {
				logger.debug("MessageId initialised with value : [" + messageId + "]");
			}

	}
}
