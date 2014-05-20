package org.esupportail.smsuapimail.exceptions;

import org.esupportail.commons.services.i18n.I18nService;

public class SmsSenderException extends I18nException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7216399876981057544L;

	private String i18nKey;

	public SmsSenderException(final String i18nKey) {
		super(i18nKey);
		this.i18nKey = i18nKey;
	}

	public SmsSenderException(final String i18nKey, final Throwable arg1) {
		super(i18nKey, arg1);
		this.i18nKey = i18nKey;
	}

	public String toI18nString(I18nService i18nService) {
		return i18nService.getString(i18nKey);
	}
}
