package org.esupportail.smsuapimail.services.messageRetriever;

import java.util.List;
import java.util.LinkedList;

import javax.mail.MessagingException;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.esupportail.smsuapimail.domain.beans.RawMessage;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.MessageRetrieverConnectorException;
import org.esupportail.smsuapimail.services.messageRetriever.pop.MessageBodyToMailToSmsMessageConverter;
import org.springframework.beans.factory.InitializingBean;


public class MessageRetriever implements InitializingBean {
	
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * Charset supported in email.
	 */
	private String mailCharset;

	/**
	 * Tool used to get message from mail box.
	 */
	private IMessageRetrieverConnector messageRetrieverConnector;

	/**
	 * Tool used to parse mail body.
	 */
	private MessageBodyToMailToSmsMessageConverter messageBodyToMailToSmsMessageConverter;

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
		String msgContent;
		try {
			msgContent = new String(email.getContent(), mailCharset);
		} catch (java.io.UnsupportedEncodingException e) {
			logger.error(e);
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Text extract from message is : \n" + msgContent);
		}
		try {
			return messageBodyToMailToSmsMessageConverter.convertMessageBodyToMailToSmsMessage(msgContent);
		} catch (MessagingException e) {
			logger.error(e);
			return null;
		}
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

}
