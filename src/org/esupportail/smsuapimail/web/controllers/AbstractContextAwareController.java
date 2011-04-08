/**
 * ESUP-Portail esup-smsuapimail - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-smsuapimail
 */
package org.esupportail.smsuapimail.web.controllers;

import org.esupportail.smsuapimail.domain.beans.User;
import org.esupportail.commons.utils.Assert;

/**
 * An abstract class inherited by all the beans for them to get:
 * - the context of the application (sessionController).
 * - the domain service (domainService).
 * - the application service (applicationService).
 * - the i18n service (i18nService).
 */
@SuppressWarnings("serial")
public abstract class AbstractContextAwareController extends AbstractDomainAwareBean {

	/**
	 * The SessionController.
	 */
	private SessionController sessionController;

	/**
	 * Constructor.
	 */
	protected AbstractContextAwareController() {
		super();
	}

	/**
	 * @see org.esupportail.smsuapimail.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
	 */
	@Override
	public void afterPropertiesSetInternal() {
		Assert.notNull(this.sessionController, "property sessionController of class " 
				+ this.getClass().getName() + " can not be null");
	}

	/**
	 * @param sessionController the sessionController to set
	 */
	public void setSessionController(final SessionController sessionController) {
		this.sessionController = sessionController;
	}

	/**
	 * @return the sessionController
	 */
	public SessionController getSessionController() {
		return sessionController;
	}

	/**
	 * @see org.esupportail.smsuapimail.web.controllers.AbstractDomainAwareBean#getCurrentUser()
	 */
	@Override
	protected User getCurrentUser() {
		return sessionController.getCurrentUser();
	}

}
