package org.esupportail.smsuapimail.domain.beans;

import java.io.IOException;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.io.IOUtils;


/**
 * This class is used to create intermediate objects between Message and SmsMessage.
 * It is useful because a Message do not survive the closing of pop3 connection
 *
 */
public class RawMessage {

	/**
	 * The content type plain text.
	 */
	private static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";
	
	private Address from;
	private String subject;
	private byte[] content;
	
	public RawMessage(Message msg) throws MessagingException, IOException {
		from = msg.getFrom()[0];
		subject = msg.getSubject();
		content = getMessageContent(msg); 
	}

	public Address getFrom() {
		return from;
	}
	public String getSubject() {
		return subject;
	}
	public byte[] getContent() {
		return content;
	}

	static private byte[] getMessageContent(final Message email) throws IOException, MessagingException {
		Part messagePart = getFirstBodyPart(email);
		String contentType = messagePart.getContentType();
		
		// only accept plain text message
		if (contentType.contains(PLAIN_TEXT_CONTENT_TYPE)) {
			return IOUtils.toByteArray(messagePart.getInputStream());
		} else {
			String s = "Message with subject : " + email.getSubject() + 
			    " rejected because it is not a plain/text email";
			throw new MessagingException(s);
		}
	}

	static private Part getFirstBodyPart(Message email) throws IOException, MessagingException {
		final Object messageContent = email.getContent();	
		if (messageContent instanceof Multipart) {
			return ((Multipart) messageContent).getBodyPart(0);
		} else {
			return email;
		}	
	}
	
}
