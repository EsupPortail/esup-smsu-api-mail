package org.esupportail.smsuapimail.services.messageRetriever.pop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.MessageRetrieverConnectorException;
import org.esupportail.smsuapimail.services.messageRetriever.IMessageRetrieverConnector;

/**
 * This class is used to get mail from Pop server.
 * @author prqd8824
 *
 */
public class PopMessageRetrieverConnector implements IMessageRetrieverConnector {

	
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * The store type on the server.
	 */
	private static final String POP_STORE_TYPE = "pop3";
	
	/**
	 * The content type plain text.
	 */
	private static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";
	
	/**
	 * Tool used to parse mail body.
	 */
	private MessageBodyToMailToSmsMessageConverter messageBodyToMailToSmsMessageConverter;

	/**
	 * Pop server address.
	 */
	private String popServerAdress;
	
	/**
	 * Login used to log on the pop server.
	 */
	private String popServerLogin;
	
	/**
	 * Password used to log on the pop server.
	 */
	private String popServerPassword;
	
	/**
	 * Name of the folder containing mail.
	 */
	private String popFolderName;
	
	/**
	 * Charset supported in email.
	 */
	private String mailCharset;
	

	/**
	 * Get the message on the pop server.
	 * @return messageList
	 * @throws MessageRetrieverConnectorException
	 */
	public List<SmsMessage> getMessages() throws MessageRetrieverConnectorException {
		List<SmsMessage> messageList = getEmailFromPopServer();

		return messageList;
	}
	
	/**
	 * 
	 * @return a list of messages from the pop server
	 * @throws MessageRetrieverConnectorException
	 */
	private List<SmsMessage> getEmailFromPopServer() throws MessageRetrieverConnectorException {
		final List<SmsMessage> messageToProcessList = new LinkedList<SmsMessage>();
		
		Store store = null;
		Folder defaultFolder = null;
		Folder folder = null;

		try {
			// Get the session
			final Properties props = System.getProperties();
			Session session = Session.getDefaultInstance(props, null);
			
			// Connect to the pop server
			if (logger.isDebugEnabled()) {
				logger.debug("Connecting to pop serveur : \n" + 
					     " - server address : " + popServerAdress + "\n" +
					     " - server login : " + popServerLogin + "\n" + 
					     " - server pass : " + "password is hidden" + "\n");
			}
			
			store = session.getStore(POP_STORE_TYPE);
			store.connect(popServerAdress, popServerLogin, popServerPassword);

			// Go to the default folder
			defaultFolder = store.getDefaultFolder();
			if (defaultFolder == null) {
				String s = "unable to get default folder on server : " + popServerAdress;
				logger.error(s);
				throw new MessageRetrieverConnectorException(s);
			}
			
			// Go to the folder containing email
			folder = defaultFolder.getFolder(popFolderName);
			if (folder == null) {
				String s = "unable to get folder : " + popFolderName + " on server : " + popServerAdress;
				logger.error(s);
				throw new MessageRetrieverConnectorException(s);
			}
			// Open the folder for read and write (write right must be present to allow delete)
			folder.open(Folder.READ_WRITE);
			
			// Get the message wrappers and process them
			final Message[] msgs = folder.getMessages();
			
			for (int msgNum = 0; msgNum < msgs.length; msgNum++) {
				final Message messageTmp = msgs[msgNum];
				
				// this try catch block is here to allow best effort managment.
				// An error on a single email does not perform a global error
				try {

					// only process message not marked as deleted
					if (!messageTmp.isSet(Flags.Flag.DELETED)) {
						// convert the email into MailToSmsMessage
						final SmsMessage mailToSmsMessage = convertMessageToMailToSmsMessage(messageTmp);
						messageToProcessList.add(mailToSmsMessage);
					}
					
				} catch (MessagingException e) {
					logger.error("Unable to manage email with subject : " + messageTmp.getSubject() + " due to MessagingException", e);
				} catch (IOException e) {
					logger.error("Unable to manage email with subject : " + messageTmp.getSubject() + " due to IOException", e);
				} finally {
					// whetever append mark delete the message
					messageTmp.setFlag(Flags.Flag.DELETED, true);
				}
			}

			
		} catch (NoSuchProviderException e) {
			String s = "unable to get message (due to NoSuchProviderException) from server : " + popServerAdress;
			logger.error(s, e);
			throw new MessageRetrieverConnectorException(s, e);
		} catch (MessagingException e) {
			String s = "unable to get message (due to MessagingException) from server : " + popServerAdress;
			logger.error(s, e);
			throw new MessageRetrieverConnectorException(s, e);
		} finally {
	    	// close the folder
			try {
				if (folder != null) {
					// true because we want to delete message marked as deleted
					folder.close(true);
				}
			} catch (MessagingException e) {
				logger.warn("Unable to close folder : " + popFolderName + 
					    " on server : " + popServerAdress, e);
			}
				
			try {
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				logger.warn("Unable to get close the store" + " on server : " + popServerAdress, e);
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + messageToProcessList.size() + " emails to proceed");
		}
		
		return messageToProcessList;
	}
	
	/**
	 * 
	 * @param email
	 * @return a Sms message from a mail
	 * @throws IOException
	 * @throws MessagingException
	 */
	private SmsMessage convertMessageToMailToSmsMessage(final Message email) throws IOException, MessagingException {
		// extract message text from email 
		final String message = getTextFromMessage(email);
		
		// convert text to MailToSms
		final SmsMessage retVal = messageBodyToMailToSmsMessageConverter.convertMessageBodyToMailToSmsMessage(message);
		return retVal;
	}
	
	/**
	 * 
	 * @param email
	 * @return a text from a message.
	 * @throws IOException
	 * @throws MessagingException
	 */
	private String getTextFromMessage(final Message email) throws IOException, MessagingException {
		
		final StringBuilder retVal = new StringBuilder(300);
		
		Part messagePart = email;
		final Object messageContent = messagePart.getContent();
		
		if (messageContent instanceof Multipart) {
			messagePart = ((Multipart) messageContent).getBodyPart(0);
		}
		
		String contentType = messagePart.getContentType();
		
		// only accept plain text message
		if (contentType.contains(PLAIN_TEXT_CONTENT_TYPE)) {
			final InputStream is = messagePart.getInputStream();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String currentLine = reader.readLine();
			while (currentLine != null) {
				// this line is used to charset managment to prevent 
				// pb with french accents
				final String tmp = new String(currentLine.getBytes(), mailCharset);
				retVal.append(tmp).append("\n");
				currentLine = reader.readLine();
			}
		} else {
			String s = "Message with subject : " + email.getSubject() + 
			    " rejected because it is not a plain/text email";
			logger.error(s);
			throw new MessagingException(s);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Text extract from message is : \n" + retVal.toString());
		}
		
		return retVal.toString();
	}
	
	/**
	 * Standard setter used by Spring.
	 * @param popServerAdress
	 */
	public void setPopServerAdress(final String popServerAdress) {
		this.popServerAdress = popServerAdress;
	}

	/**
	 * Standard setter used by Spring.
	 * @param popServerLogin
	 */
	public void setPopServerLogin(final String popServerLogin) {
		this.popServerLogin = popServerLogin;
	}

	/**
	 * Standard setter used by Spring.
	 * @param popServerPassword
	 */
	public void setPopServerPassword(final String popServerPassword) {
		this.popServerPassword = popServerPassword;
	}
	
	/**
	 * Standard setter used by Spring.
	 * @param popFolderName
	 */
	public void setPopFolderName(final String popFolderName) {
		this.popFolderName = popFolderName;
	}
	
	/**
	 * Standard setter used by Spring.
	 * @param messageBodyToMailToSmsMessageConverter
	 */
	public void setMessageBodyToMailToSmsMessageConverter(
			final MessageBodyToMailToSmsMessageConverter messageBodyToMailToSmsMessageConverter) {
		this.messageBodyToMailToSmsMessageConverter = messageBodyToMailToSmsMessageConverter;
	}
	
	/**
	 * Standard setter used by Spring.
	 * @param mailCharset 
	 */
	public void setMailCharset(final String mailCharset) {
		this.mailCharset = mailCharset;
	}

}