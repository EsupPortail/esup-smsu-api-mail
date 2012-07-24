package org.esupportail.smsuapimail.services.messageRetriever;

import java.util.List;

import org.esupportail.smsuapimail.domain.beans.RawMessage;
import org.esupportail.smsuapimail.exceptions.MessageRetrieverConnectorException;

/**
 * Common interface for all message retrievers.
 * @author prqd8824
 *
 */
public interface IMessageRetrieverConnector {

	/**
	 * Get all messages.
	 * @return
	 * @throws MessageRetrieverConnectorException
	 */
	List<RawMessage> getMessages() throws MessageRetrieverConnectorException;
}
