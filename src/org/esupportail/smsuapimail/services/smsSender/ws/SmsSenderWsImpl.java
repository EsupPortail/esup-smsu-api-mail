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
				StringBuffer sb = new StringBuffer();
				sb.append("MessageId initialised with value : [");
				sb.append(messageId);
				sb.append("]");
				logger.debug(sb.toString());
			}

	}
}
