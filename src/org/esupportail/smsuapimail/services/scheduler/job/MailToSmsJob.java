package org.esupportail.smsuapimail.services.scheduler.job;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.business.BusinessManager;
import org.esupportail.smsuapimail.services.scheduler.AbstractQuartzJob;
import org.quartz.JobDataMap;
import org.springframework.context.ApplicationContext;

public class MailToSmsJob extends AbstractQuartzJob {

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	private static final String BUSINESS_MANAGER_BEAN_NAME = "businessManager";
	
	@Override
	protected void executeJob(final ApplicationContext applicationContext, final JobDataMap jobDataMap) {
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(100);
			sb.append("Launching Quartz task MailPollingJob now");
			logger.debug(sb.toString());
		}
		
		final BusinessManager businessManager = (BusinessManager) applicationContext.getBean(BUSINESS_MANAGER_BEAN_NAME);
		businessManager.sendSMS();
		
		if (logger.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder(100);
			sb.append("End of Quartz task MailPollingJob");
			logger.debug(sb.toString());
		}
	}

}
