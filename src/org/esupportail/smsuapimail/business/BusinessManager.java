package org.esupportail.smsuapimail.business;

import java.util.Arrays;
import java.util.List;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.MessageRetrieverConnectorException;
import org.esupportail.smsuapimail.exceptions.SmsSenderException;
import org.esupportail.smsuapimail.services.messageRetriever.IMessageRetrieverConnector;
import org.esupportail.smsuapimail.services.smsSender.ISmsSender;

/**
 * Business layer of the application.
 * @author prqd8824
 *
 */
public class BusinessManager {

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());

	/**
	 * Tool used to get message from mail box.
	 */
	private IMessageRetrieverConnector messageRetrieverConnector;

	/**
	 * Tool used to send SMS to the Back office.
	 */
	private ISmsSender smsSender;

	/**
	 * The account label of this application.
	 * This account label has to be known by the back office
	 */
	private String defaultAccountLabel;

	/**
	 * The max length of the message, it the message is longer, it's automatically trunked.
	 */
	private int messageMaxLength;

	/**
	 * a list with the allowed passwords
	 */
	private List<String> pwdList; 

	/**
	 * constructor.
	 */
	public BusinessManager() {
		//no special instruction.
	}
	/**
	 * Get the message from the email box and send the message to the back office.
	 */
	public void sendSMS() {
		try {
			// get the message list from the connector
			final List<SmsMessage> smsMessageList = messageRetrieverConnector.getMessages();


			// for each message, send all sms to the back office
			for (SmsMessage smsMessage : smsMessageList) {
				//check if the message contains an allowed pwd.
				if (checkPwd(smsMessage)) {
					// if no account was defined in the message
					// used the default account
					if (smsMessage.getAccount() == null) {
						smsMessage.setAccount(defaultAccountLabel);
						if (logger.isDebugEnabled()) {
							logger.debug("Adding the default account label to the message");
						}
					}

					// trunk the message if it is too long
					final String messageContent = smsMessage.getContent();
					if (messageContent != null && messageContent.length() > messageMaxLength) {
						final String tmp = messageContent.substring(0, messageMaxLength - 1);
						smsMessage.setContent(tmp);
						if (logger.isDebugEnabled()) {
							logger.debug("Trunking the message at " + messageMaxLength +
								     " characters\n" +
								     "Message is now : " + smsMessage.getContent());
						}

					}

					try {
						smsSender.sendSms(smsMessage);
					} catch (SmsSenderException e) {
						logger.error("Unable to send SMS with : \n" +
							     " - account : " + smsMessage.getAccount() + "\n" +
							     " - recipients : " + smsMessage.getPhoneNumbers() + "\n" +
							     " - content : " + smsMessage.getContent() + "\n",
							     e);
					}
				} else {
					logger.warn("Unable to send SMS with : \n" +
						     " - account : " + smsMessage.getAccount() + "\n" +
						     " - recipients : " + smsMessage.getPhoneNumbers() + "\n" +
						     " - content : " + smsMessage.getContent() + "\n" +
						     "Wrong password.");
				}
			}
		} catch (MessageRetrieverConnectorException e) {
			logger.error("Unable to get message from the mail box ", e);
		}
	}

	/**
	 * check if the message pwd is in the allowed pwd list.
	 */
	private boolean checkPwd(SmsMessage smsMessage) {
		Boolean retVal = false;
		String messagePwd = smsMessage.getPwd();

		if (logger.isDebugEnabled()) {
			logger.debug("message pwd : " + messagePwd + "\n");
		}
		if (this.pwdList.contains(messagePwd)) {
			retVal = true;
			if (logger.isDebugEnabled()) {
				logger.debug("pwd found into the list\n");
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("pwd not found into the list");
			}
		}

		return retVal;
	}

	/**
	 * Standard setter used by spring.
	 * @param messageRetrieverConnector
	 */
	public void setMessageRetrieverConnector(
			final IMessageRetrieverConnector messageRetrieverConnector) {
		this.messageRetrieverConnector = messageRetrieverConnector;
	}

	/**
	 * Standard setter used by spring.
	 * @param smsSender
	 */
	public void setSmsSender(final ISmsSender smsSender) {
		this.smsSender = smsSender;
	}

	/**
	 * Standard setter used by spring.
	 * @param defaultAccountLabel 
	 */
	public void setDefaultAccountLabel(final String defaultAccountLabel) {
		this.defaultAccountLabel = defaultAccountLabel;
	}

	/**
	 * Standard setter used by spring.
	 * @param messageMaxLength
	 */
	public void setMessageMaxLength(final int messageMaxLength) {
		this.messageMaxLength = messageMaxLength;
	}
	/**
	 * Standard setter used by spring.
	 * @param pwdList
	 */
	public void setPwdList(final List<String> pwdList) {
		this.pwdList = pwdList;
	}

	/**
	 * the setter used to init the pwd list.
	 * @param pwdListAsString
	 */
	public void setPwdListAsString(final String pwdListAsString) {
		String[] pwdTab = pwdListAsString.split(",");
		setPwdList(Arrays.asList(pwdTab));
	}

}
