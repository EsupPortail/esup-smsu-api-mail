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
							final StringBuilder sb = new StringBuilder(200);
							sb.append("Adding the default account label to the message");
							logger.debug(sb.toString());
						}
					}

					// trunk the message if it is too long
					final String messageContent = smsMessage.getContent();
					if (messageContent != null && messageContent.length() > messageMaxLength) {
						final String tmp = messageContent.substring(0, messageMaxLength - 1);
						smsMessage.setContent(tmp);
						if (logger.isDebugEnabled()) {
							final StringBuilder sb = new StringBuilder(200);
							sb.append("Trunking the message at ").append(messageMaxLength).
							append(" characters\n");
							sb.append("Message is now : ").append(smsMessage.getContent());
							logger.debug(sb.toString());
						}

					}

					try {
						smsSender.sendSms(smsMessage);
					} catch (SmsSenderException e) {
						final StringBuilder sb = new StringBuilder(200);
						sb.append("Unable to send SMS with : \n");
						sb.append(" - account : ").append(smsMessage.getAccount()).append("\n");
						sb.append(" - recipients : ").append(smsMessage.getPhoneNumbers()).append("\n");
						sb.append(" - content : ").append(smsMessage.getContent()).append("\n");
						logger.error(sb.toString(), e);
					}
				} else {
					final StringBuilder sb = new StringBuilder(200);
					sb.append("Unable to send SMS with : \n");
					sb.append(" - account : ").append(smsMessage.getAccount()).append("\n");
					sb.append(" - recipients : ").append(smsMessage.getPhoneNumbers()).append("\n");
					sb.append(" - content : ").append(smsMessage.getContent()).append("\n");
					sb.append("Wrong password.");
					logger.warn(sb.toString());
				}
			}
		} catch (MessageRetrieverConnectorException e) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Unable to get message from the mail box ");
			logger.error(sb.toString(), e);
		}
	}

	/**
	 * check if the message pwd is in the allowed pwd list.
	 */
	private boolean checkPwd(SmsMessage smsMessage) {
		Boolean retVal = false;
		String messagePwd = smsMessage.getPwd();

		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("message pwd : ").append(messagePwd).append("\n");
			logger.debug(sb.toString());
		}
		if (this.pwdList.contains(messagePwd)) {
			retVal = true;
			if (logger.isDebugEnabled()) {
				final StringBuilder sb = new StringBuilder(200);
				sb.append("pwd found into the list").append("\n");
				logger.debug(sb.toString());
			}
		} else {
			if (logger.isDebugEnabled()) {
				final StringBuilder sb = new StringBuilder(200);
				sb.append("pwd not found into the list").append("\n");
				logger.debug(sb.toString());
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
