package org.esupportail.smsuapimail.services.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.BeanUtils;
import org.esupportail.smsuapimail.exceptions.MessageRetrieverConnectorException;
import org.esupportail.smsuapimail.services.messageRetriever.pop.PopMessageRetrieverConnector;

/**
 * Servlet used only during test.
 *
 */
public class TestServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -518464944485395827L;

	  /**
     * logger
     */
	private final Logger logger = new LoggerImpl(getClass());
	

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest req, 
    					 final HttpServletResponse resp) throws ServletException,
        										   			  	IOException {
        try {
			execute(req, resp);
		} catch (Throwable t) {
			logger.error(t);
		}
    }
    
    private void execute(final HttpServletRequest req, 
    					 final HttpServletResponse resp) {
    	
    	
    	final String testId = req.getParameter("testId");
    	
    	if ("pop".equalsIgnoreCase(testId)) {
    		testPop();
    	} 
    }
    
    private void testPop() {
    	PopMessageRetrieverConnector popMessageRetrieverConnector = (PopMessageRetrieverConnector) BeanUtils.getBean("popMessageRetrieverConnector");
    	try {
    		popMessageRetrieverConnector.getMessages();
		} catch (MessageRetrieverConnectorException e) {
			logger.error(e);
		}
    }
   
}

