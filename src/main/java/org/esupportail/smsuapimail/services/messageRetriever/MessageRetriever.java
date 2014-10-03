package org.esupportail.smsuapimail.services.messageRetriever;

import java.util.List;
import java.util.LinkedList;

import javax.mail.internet.InternetAddress;

import org.esupportail.commons.services.i18n.I18nService;
import org.apache.log4j.Logger;
import org.esupportail.commons.services.smtp.SmtpService;
import org.esupportail.commons.utils.Assert;
import org.esupportail.smsuapimail.domain.beans.RawMessage;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.MessageRetrieverConnectorException;
import org.esupportail.smsuapimail.exceptions.I18nException;
import org.esupportail.smsuapimail.exceptions.ParsingMessageBodyException;
import org.esupportail.smsuapimail.services.messageRetriever.pop.MessageBodyToMailToSmsMessageConverter;
import org.springframework.beans.factory.InitializingBean;


public class MessageRetriever implements InitializingBean {
	
	/**
	 * A logger.
	 */
	private final Logger logger = Logger.getLogger(getClass());
	
	/**
	 * Charset supported in email.
	 */
	private String mailCharset;
	
	/**
	 * Email to notify in case of errors
	 */
	private String exceptionHandlingEmail;

	/**
	 * Tool used to get message from mail box.
	 */
	private IMessageRetrieverConnector messageRetrieverConnector;

	/**
	 * Tool used to parse mail body.
	 */
	private MessageBodyToMailToSmsMessageConverter messageBodyToMailToSmsMessageConverter;

	private SmtpService smtpService;
	private I18nService i18nService;

	public void afterPropertiesSet() {
		Assert.notNull(this.mailCharset, "property mailCharset of class " 
				+ this.getClass().getName() + " can not be null");
		Assert.notNull(this.messageRetrieverConnector, "property messageRetrieverConnector of class " 
				+ this.getClass().getName() + " can not be null");
		Assert.notNull(this.messageBodyToMailToSmsMessageConverter, "property messageBodyToMailToSmsMessageConverter of class " 
				+ this.getClass().getName() + " can not be null");
	}


	public List<SmsMessage> getMessages() {
		List<SmsMessage> msgs = new LinkedList<SmsMessage>();
		for (RawMessage email : getRawMessages()) {
			SmsMessage msg = convertToSmsMessage(email);
			if (msg != null) msgs.add(msg);
		}
		return msgs;
	}

	private List<RawMessage> getRawMessages() {
		try {
			return messageRetrieverConnector.getMessages();
		} catch (MessageRetrieverConnectorException e) {
			logger.error(e.getMessage(), e.getCause());
			return new LinkedList<RawMessage>();
		}
	}

	private SmsMessage convertToSmsMessage(RawMessage email) {
		String msgContent = getContent(email);
		if (msgContent == null) return null;

		if (logger.isDebugEnabled()) {
			logger.debug("Text extract from message is : \n" + msgContent);
		}
		try {
			SmsMessage msg = messageBodyToMailToSmsMessageConverter.convertMessageBodyToMailToSmsMessage(msgContent);
			msg.setRawMessage(email);
			return msg;
		} catch (ParsingMessageBodyException e) {
			logger.error(e);
			warnSenderMessageInvalid(email, e);
			return null;
		}
	}

	private String getContent(RawMessage email) {
		try {
			return new String(email.getContent(), mailCharset);
		} catch (java.io.UnsupportedEncodingException e) {
			logger.error(e);
			return null;
		}
	}

	public void warnSenderMessageInvalid(RawMessage email, I18nException e) {
		warnSenderMessageInvalid(email, e.toI18nString(i18nService));
	}

	private void warnSenderMessageInvalid(RawMessage email, String error) {
		String to = email.getFrom().toString();
		String subject = i18nService.getString("ERROR.MAIL.SUBJECT", email.getSubject());
		String msg = i18nService.getString("ERROR.MAIL.BODY", error, getContent(email));
		sendMail(to, exceptionHandlingEmail, subject, msg);
	}

	private void sendMail(String to, String cc, String subject, String textBody) {
		smtpService.sendtocc(sendMailAdresses(to), sendMailAdresses(cc), 
				     null, subject, null, textBody, null);
	}

	private InternetAddress[] sendMailAdresses(String address) {
		try {
			return new InternetAddress[]{ new InternetAddress(address) };
		} catch (javax.mail.internet.AddressException e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setExceptionHandlingEmail(final String exceptionHandlingEmail) {
		this.exceptionHandlingEmail = exceptionHandlingEmail;
	}
	
	/**
	 * Standard setter used by Spring.
	 * @param mailCharset 
	 */
	public void setMailCharset(final String mailCharset) {
		this.mailCharset = mailCharset;
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
	 * Standard setter used by Spring.
	 * @param messageBodyToMailToSmsMessageConverter
	 */
	public void setMessageBodyToMailToSmsMessageConverter(
			final MessageBodyToMailToSmsMessageConverter messageBodyToMailToSmsMessageConverter) {
		this.messageBodyToMailToSmsMessageConverter = messageBodyToMailToSmsMessageConverter;
	}

	public void setSmtpService(SmtpService smtpService) {
		this.smtpService = smtpService;
	}

	public void setI18nService(I18nService i18nService) {
		this.i18nService = i18nService;
	}

}
