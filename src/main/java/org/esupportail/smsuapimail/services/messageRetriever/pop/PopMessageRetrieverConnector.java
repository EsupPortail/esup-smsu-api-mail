package org.esupportail.smsuapimail.services.messageRetriever.pop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.domain.beans.RawMessage;
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
	 * Get the message on the pop server.
	 * @return messageList
	 * @throws MessageRetrieverConnectorException
	 */
	public List<RawMessage> getMessages() throws MessageRetrieverConnectorException {
		return getMessagesFromPopServer();
	}

	private Store getStore() throws MessageRetrieverConnectorException {
		try {
			final Properties props = System.getProperties();
			Session session = Session.getDefaultInstance(props, null);
			return session.getStore(POP_STORE_TYPE);
		} catch (NoSuchProviderException e) {
			String s = "unable to get message (due to NoSuchProviderException) from server : " + popServerAdress;
			throw new MessageRetrieverConnectorException(s, e);
		}
	}

	private Store getStoreAndConnect() throws MessageRetrieverConnectorException {
		try {
			Store store = getStore();
			
			if (logger.isDebugEnabled()) {
				logger.debug("Connecting to pop serveur : \n" + 
					     " - server address : " + popServerAdress + "\n" +
					     " - server login : " + popServerLogin + "\n" + 
					     " - server pass : " + "password is hidden" + "\n");
			}
			store.connect(popServerAdress, popServerLogin, popServerPassword);
			return store;
		} catch (MessagingException e) {
			String s = "unable to connect to server " + popServerAdress;
			throw new MessageRetrieverConnectorException(s, e);
		}
	}

	private Folder getFolder(Store store) throws MessageRetrieverConnectorException {
		try {
			// Go to the default folder
			Folder defaultFolder = store.getDefaultFolder();
			if (defaultFolder == null) {
				String s = "unable to get default folder on server : " + popServerAdress;
				throw new MessageRetrieverConnectorException(s);
			}
			
			// Go to the folder containing email
			Folder folder = defaultFolder.getFolder(popFolderName);
			if (folder == null) {
				String s = "unable to access folder : " + popFolderName + " on server : " + popServerAdress;
				throw new MessageRetrieverConnectorException(s);
			}
			return folder;
		} catch (MessagingException e) {
			String s = "unable to get message (due to MessagingException) from server : " + popServerAdress;
			throw new MessageRetrieverConnectorException(s, e);
		}
	}
	
	/**
	 * 
	 * @return a list of messages from the pop server
	 * @throws MessageRetrieverConnectorException
	 */
	private List<RawMessage> getMessagesFromPopServer() throws MessageRetrieverConnectorException {
		Store store = null;
		try {
			store = getStoreAndConnect();
			return openFolderAndGetRawMessage(getFolder(store));
		} finally {
			closeStore(store);
		}
	}

	private List<RawMessage> openFolderAndGetRawMessage(Folder folder) throws MessageRetrieverConnectorException {
		try {
			// Open the folder for read and write (write right must be present to allow delete)
			folder.open(Folder.READ_WRITE);
			
			final List<RawMessage> messageToProcessList = new LinkedList<RawMessage>();
		
			// Get the message wrappers and process them
			for (Message email : folder.getMessages()) {			
				RawMessage msg = mayCreateRawMessageFromMessage(email);
				if (msg != null)
					// An error on a single email does not perform a global error
					messageToProcessList.add(msg);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Found " + messageToProcessList.size() + " emails to proceed");
			}
		
			return messageToProcessList;
			
		} catch (MessagingException e) {
			String s = "unable to get message (due to MessagingException) from server : " + popServerAdress;
			throw new MessageRetrieverConnectorException(s, e);
		} finally {
			closeFolder(folder);
		}
	}

	private RawMessage mayCreateRawMessageFromMessage(final Message email) throws MessageRetrieverConnectorException {
		try {
			if (!email.isSet(Flags.Flag.DELETED)) {
				return new RawMessage(email);
			}
		} catch (MessagingException e) {
			logger.error("Unable to manage email with subject : " + safeGetSubject(email) + " due to MessagingException", e);
		} catch (IOException e) {
			logger.error("Unable to manage email with subject : " + safeGetSubject(email) + " due to IOException", e);
		} finally {
			try {
				// whetever happen mark delete the message
				email.setFlag(Flags.Flag.DELETED, true);
			} catch (MessagingException e) {
				logger.error("unable to to mark message deleted", e);
			}
		}
		return null;
	}

	private void closeFolder(Folder folder) {
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
	}

	private void closeStore(Store store) {
		try {
			if (store != null) store.close();
		} catch (MessagingException e) {
			logger.warn("Unable to get close the store" + " on server : " + popServerAdress, e);
		}
	}

	private String safeGetSubject(Message email) {
		try {
			return email.getSubject();
		} catch (MessagingException e) {
			return "";
		}
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

}