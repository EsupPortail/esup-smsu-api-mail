package org.esupportail.smsuapimail.services.authentication;

import org.esupportail.smsuapimail.domain.beans.User;

/**
 * The interface of authenticators.
 */
public interface Authenticator {

	/**
	 * @return the authenticated user.
	 */
	User getUser();

}