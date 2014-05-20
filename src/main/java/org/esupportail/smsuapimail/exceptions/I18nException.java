package org.esupportail.smsuapimail.exceptions;

import org.esupportail.commons.services.i18n.I18nService;

@SuppressWarnings("serial")
abstract public class I18nException extends Exception {

	abstract public String toI18nString(I18nService i18nService);

	public I18nException(final String arg0) {
		super(arg0);
	}
	public I18nException(final Throwable arg0) {
		super(arg0);	
	}
	public I18nException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
