package org.esupportail.smsuapimail.services.messageRetriever.pop;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class is used to convert a email body into a valid mailtoSmsMessage.
 * @author prqd8824
 *
 */
public class MessageBodyToMailToSmsMessageConverter implements InitializingBean {

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * the separator used between the tag and the value.
	 */
	private static final String SEPARATOR_CHAR = "=";
	
	/**
	 * Message index in message format result.
	 */
	private static final int MESSAGE_FORMAT_PWD_IDX = 0;
	
	/**
	 * Message pattenr for message format.
	 */
	private static final String MESSAGE_FORMAT_PWD = "{0}";
	
	/**
	 * Recipient index in message format result.
	 */
	private static final int MESSAGE_FORMAT_RECIPIENTS_IDX = 1;
	
	/**
	 * Recipient pattenr for message format.
	 */
	private static final String MESSAGE_FORMAT_RECIPIENTS = "{1}";
	
	/**
	 * Account index in message format result.
	 */
	private static final int MESSAGE_FORMAT_ACCOUNT_IDX = 2;
	
	/**
	 * Account pattenr for message format.
	 */
	private static final String MESSAGE_FORMAT_ACCOUNT = "{2}";
	
	/**
	 * Message index in message format result.
	 */
	private static final int MESSAGE_FORMAT_MESSAGE_IDX = 3;
	
	/**
	 * Message pattenr for message format.
	 */
	private static final String MESSAGE_FORMAT_MESSAGE = "{3}";
	
	
	/**
	 * the separator comma used to separate phone number.
	 */
	private static final String COMMA_SEPARATOR_REGULAR_EXPR = ",";
	
	/**
	 * Tag used in email to defined pwd.
	 */
	private String pwdTag;
	
	/**
	 * Tag used in email to defined recipients.
	 */
	private String recipientsTag;
	
	/**
	 * Tag used email to defined account.
	 */
	private String accountTag;
	
	/**
	 * tag used in email to defined content.
	 */
	private String contentTag;
	
	/**
	 * the valid phone number pattern.
	 */
	private String phoneNumerPattern;
	
	/**
	 * Message format used to parse recipient expression.
	 */
	private MessageFormat messageFormater;
	
	
	
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		messageFormater = new MessageFormat(pwdTag + SEPARATOR_CHAR + MESSAGE_FORMAT_PWD +
											recipientsTag + SEPARATOR_CHAR + MESSAGE_FORMAT_RECIPIENTS +
											accountTag + SEPARATOR_CHAR + MESSAGE_FORMAT_ACCOUNT +
											contentTag + SEPARATOR_CHAR + MESSAGE_FORMAT_MESSAGE);
		
	}
	
	/**
	 * convert the mail body given as a String into a MailToSmsMessage.
	 * @param messageBody
	 * @return
	 */
	public SmsMessage convertMessageBodyToMailToSmsMessage(final String messageBody) throws MessagingException {
		
		final SmsMessage smsMessage = formatTextMessage(messageBody);
		
		final List<String> recipients = smsMessage.getPhoneNumbers();
		
		// if no recipient found : error
		if (recipients == null || recipients.size() == 0) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("No recipient found in message : \n");
			sb.append(" - message : ").append(messageBody);
			logger.error(sb.toString());
			throw new MessagingException(sb.toString());
		}
		
		
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("convertMessageBodyToMailToSmsMessage return value : \n");
			sb.append(" - recipient list : ").append(recipients).append("\n");
			sb.append(" - account : ").append(smsMessage.getAccount()).append("\n");
			sb.append(" - content : ").append(smsMessage.getContent()).append("\n");
			logger.debug(sb.toString());
		}
		
		return smsMessage;
		
	}
	
	
	/**
	 * Parse the message to extract datas.
	 * @param messageBody
	 * @return
	 * @throws MessagingException
	 */
	private SmsMessage formatTextMessage(final String messageBody) throws MessagingException {
		SmsMessage retVal = null;
		
		Object[] result = null;
		final ParsePosition parsePosition = new ParsePosition(0);
		
		// /!\ Message format is not thread safe 
		
		synchronized (messageFormater) {
			// trim the body to avoid white spaces parsing problems
			final String messageBodyTrimed  = messageBody.trim();
			result = messageFormater.parse(messageBodyTrimed, parsePosition);

			// if error index != -1 => parsing error 
			if (parsePosition.getErrorIndex() != -1) {
				final StringBuilder sb = new StringBuilder(200);
				sb.append("The message body does not match with the pattern \n");
				sb.append(" - message body : [").append(messageBodyTrimed).append("]\n");
				sb.append(" - pattern : [").append(messageBody).append(messageFormater.toPattern()).append("]\n");
				sb.append(" - Parse index error is : ").append(parsePosition.getErrorIndex());
				logger.error(sb.toString());
				throw new MessagingException(sb.toString());
			}
		}

		if (result != null && result.length >= MESSAGE_FORMAT_MESSAGE_IDX) {
			final String rawPwd = (String) result[MESSAGE_FORMAT_PWD_IDX];
			final String rawRecipients = (String) result[MESSAGE_FORMAT_RECIPIENTS_IDX];
			final String rawAccount = (String) result[MESSAGE_FORMAT_ACCOUNT_IDX];
			final String rawMessage = (String) result[MESSAGE_FORMAT_MESSAGE_IDX];

			final String pwd = getPwdFromString(rawPwd);
			final List<String> recipients = getRecipientsFromString(rawRecipients);
			final String account = getAccountFromString(rawAccount);
			final String message = getMesssageFromString(rawMessage);
			
			retVal = new SmsMessage();
			retVal.setPwd(pwd);
			retVal.setPhoneNumbers(recipients);
			retVal.setAccount(account);
			retVal.setContent(message);
			
		} else {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("The message body does not match with the pattern \n");
			sb.append(" - message body : ").append(messageBody);
			logger.error(sb.toString());
			throw new MessagingException(sb.toString());
		}
		
		return retVal;		
	}
	
	/**
	 * Extract a recipients list from a String.
	 * @param recipientsAsStringWithTag
	 * @return
	 */
	private List<String> getRecipientsFromString(final String recipientsAsString) {
		List<String> retVal  = new LinkedList<String>();
		
		
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Start parsing line : ").append(recipientsAsString);
			logger.debug(sb.toString());
		}

		final String tmpRecipientAsString = removeCarriageReturn(recipientsAsString);
		
		final String[] recipientArray = tmpRecipientAsString.split(COMMA_SEPARATOR_REGULAR_EXPR);

		for (int i = 0; i < recipientArray.length; i++) {
			String tmpPhoneNumber = recipientArray[i];
			tmpPhoneNumber = tmpPhoneNumber.trim();

			// if phone number has a valid format, add it to the return value 
			final boolean isValidPhoneNumberFormat = Pattern.matches(phoneNumerPattern, tmpPhoneNumber);
			if (isValidPhoneNumberFormat) {
				retVal.add(tmpPhoneNumber);
				if (logger.isDebugEnabled()) {
					final StringBuilder sb = new StringBuilder(200);
					sb.append("Valid recipient phone number found : ").append(tmpPhoneNumber);
					logger.debug(sb.toString());
				}
			} else {
				final StringBuilder sb = new StringBuilder(200);
				sb.append("InValid recipient phone number found : ").append(tmpPhoneNumber).append("\n");
				sb.append("The phone number is rejected");
				logger.warn(sb.toString());
			}

		}
		
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Return value for method getRecipientsFromStringWithTag with line : ").append(recipientsAsString);
			sb.append("\n - return value : ").append(retVal);
		}
		
		return retVal;
	}
	
	
	/**
	 * Extract an account from a string.
	 * @param contentAsStringWithTag
	 * @return
	 */
	private String getAccountFromString(final String accountAsString) {
		String retVal = null;

		String account = accountAsString.trim();
		account = removeCarriageReturn(account);
		
		if (account.length() > 0) {
			retVal = account;
		}
					
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Return value for method getAccountFromStringWithTag with line : ").append(accountAsString);
			sb.append("\n - return value : ").append(retVal);
		}
		
		return retVal;
	}
	
	
	/**
	 * extract a message.
	 * @param contentAsStringWithTag
	 * @return
	 */
	private String getMesssageFromString(final String messageAsString) {
		String retVal = null;

		String message = messageAsString.trim();
		message = removeCarriageReturn(message);
		
		if (message.length() > 0) {
			retVal = message;
		}
					
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Return value for method getMesssageFromString with line : ").append(messageAsString);
			sb.append("\n - return value : ").append(retVal);
		}
		
		return retVal;
	}
	
	/**
	 * extract a pwd.
	 * @param pwdAsString
	 * @return the pwd or null.
	 */
	private String getPwdFromString(final String pwdAsString) {
		String retVal = null;
		
		String pwd = pwdAsString.trim();
		pwd = removeCarriageReturn(pwd);
		
		if (pwd.length() > 0) {
			retVal = pwd;
		}
		
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(200);
			sb.append("Return value for method getPwdFromString with line : ").append(pwdAsString);
			sb.append("\n - return value : ").append(retVal);
		}
		
		return retVal;

	}
	/**
	 * remove carriage return in the specified string.
	 * @param str
	 * @return
	 */
	private String removeCarriageReturn(final String str) {
		final String retVal =  str.replace("\n", "");
		return retVal;
	}
	
	/**
	 * Standard setter used by spring.
	 * @param recipientsTag
	 */
	public void setRecipientsTag(final String recipientsTag) {
		this.recipientsTag = recipientsTag;
	}

	/**
	 * Standard setter used by spring.
	 * @param accountTag
	 */
	public void setAccountTag(final String accountTag) {
		this.accountTag = accountTag;
	}

	/**
	 * Standard setter used by spring.
	 * @param contentTag
	 */
	public void setContentTag(final String contentTag) {
		this.contentTag = contentTag;
	}
	
	/**
	 * Standard setter used by spring.
	 * @param phoneNumerPattern
	 */
	public void setPhoneNumerPattern(final String phoneNumerPattern) {
		this.phoneNumerPattern = phoneNumerPattern;
	}
	
	/**
	 * Standard setter used by spring.
	 * @param pwdTag
	 */
	public void setPwdTag(final String pwdTag) {
		this.pwdTag = pwdTag;
	}
}
