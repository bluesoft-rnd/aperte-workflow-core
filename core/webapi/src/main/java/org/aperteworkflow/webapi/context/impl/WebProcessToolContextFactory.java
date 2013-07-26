package org.aperteworkflow.webapi.context.impl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

/** 
 * Factory for web process tool context based on servlet and portlet requests 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WebProcessToolContextFactory 
{
	public static IProcessToolRequestContext create(final HttpServletRequest request, final ProcessToolSessionFactory sessionFactory)
	{
		final WebProcessToolRequestContext processToolContext = new WebProcessToolRequestContext();
		
		if (request!=null && sessionFactory!=null) {
			ServletContext context = request.getSession().getServletContext();

			IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
			final UserData user = authorizationService.getUserByRequest(request);
			
			processToolContext.setUser(user);
			
			I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
			processToolContext.setMessageSource(messageSource);
			
			ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)context.getAttribute(ProcessToolBpmSession.class.getName());

			if(bpmSession == null && user != null)
			{
				ProcessToolBpmSession processToolBpmSession = sessionFactory.createSession(user);

				request.getSession().setAttribute(ProcessToolBpmSession.class.getName(), processToolBpmSession);
				processToolContext.setBpmSession(processToolBpmSession);
			}
		}
		
		return processToolContext;
	}
}
