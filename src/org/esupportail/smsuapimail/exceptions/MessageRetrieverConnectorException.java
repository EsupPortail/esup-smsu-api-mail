package org.esupportail.smsuapimail.exceptions;

/**
 * 
 * @author prqd8824
 *
 */
public class MessageRetrieverConnectorException extends Exception {


	/**
	 * Serial version.
	 */
	private static final long serialVersionUID = -3233840597674167668L;

	public MessageRetrieverConnectorException() {
		super();
	}
	
	public MessageRetrieverConnectorException(final String message) {
		super(message);
	}
	
	public MessageRetrieverConnectorException(final String message, final Throwable t) {
		super(message, t);
	}
}
