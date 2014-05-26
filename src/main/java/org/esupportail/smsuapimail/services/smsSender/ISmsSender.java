package org.esupportail.smsuapimail.services.smsSender;

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
import org.esupportail.smsuapi.services.client.HttpRequestSmsuapiWS;
import org.esupportail.smsuapi.services.client.SmsuapiWSException;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.SmsSenderException;
import org.springframework.beans.factory.annotation.Autowired;

public class ISmsSender {

	private final Logger logger = new LoggerImpl(getClass());

	@Autowired private HttpRequestSmsuapiWS ws;

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
		messageId ++;
		saveMessageId();
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
				     " - Message id : " + messageId + "\n" + 
				     " - Sender id : " + senderId + "\n" + 
				     " - Phone number : " + phoneNumbers + "\n" + 
				     " - Account label : " + accountLabel + "\n" + 
				     " - content : " + content);
		}
		
		ws.sendSms(messageId, phoneNumbers, content, accountLabel, senderId);
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
	 * @param senderId
	 */
	public void setSenderId(final Integer senderId) {
		this.senderId = senderId;
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
