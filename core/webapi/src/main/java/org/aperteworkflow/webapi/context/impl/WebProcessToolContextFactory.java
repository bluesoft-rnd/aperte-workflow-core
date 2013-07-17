package org.aperteworkflow.webapi.context.impl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/** 
 * Factory for web process tool context based on servlet and portlet requests 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WebProcessToolContextFactory 
{
	public static IProcessToolRequestContext create(final HttpServletRequest request)
	{
		final WebProcessToolRequestContext processToolContext = new WebProcessToolRequestContext();
		
		ServletContext context = request.getSession().getServletContext();

		// is it really necessary?
		final ProcessToolRegistry reg = getRegistry() != null ? getRegistry() : (ProcessToolRegistry)context.getAttribute(ProcessToolRegistry.class.getName());
		
		processToolContext.setRegistry(reg);
		
		IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
		final UserData user = authorizationService.getUserByRequest(request);
		
		processToolContext.setUser(user);
		
		I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
		processToolContext.setMessageSource(messageSource);
		
		ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)context.getAttribute(ProcessToolBpmSession.class.getName());

		if(bpmSession == null && user != null)
		{
			ProcessToolBpmSession processToolBpmSession = reg.getProcessToolSessionFactory().
					createSession(processToolContext.getUser(), processToolContext.getUser().getRoleNames());

			request.getSession().setAttribute(ProcessToolBpmSession.class.getName(), processToolBpmSession);
			processToolContext.setBpmSession(processToolBpmSession);
		}
		return processToolContext;
	}
}
