package org.aperteworkflow.webapi.context.impl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.servlet.LocaleResolver;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.util.Locale;

/** 
 * Factory for web process tool context based on servlet and portlet requests 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
@Component
public class WebProcessToolContextFactory implements IWebProcessToolContextFactory
{
    @Autowired
    private ProcessToolSessionFactory sessionFactory;

    @Autowired
    private IAuthorizationService authorizationService;


	@Autowired
	private IPortalUserSource portalUserSource;

    @Autowired
    private I18NSourceFactory i18NSourceFactory;

    @Autowired
    private LocaleResolver localeResolver;

    public WebProcessToolContextFactory()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

	public IProcessToolRequestContext create(final HttpServletRequest request)
	{
		final WebProcessToolRequestContext processToolContext = new WebProcessToolRequestContext();
		
		if (request!=null && sessionFactory!=null) {
			ServletContext context = request.getSession().getServletContext();


			final UserData user = authorizationService.getUserByRequest(request);
			Locale locale = null;
			if(user != null)
			{
				processToolContext.setUser(user);
				/** Get locale from portal */
				locale = portalUserSource.getUserLocale(user.getLogin());
			}
			else
			{
				locale = localeResolver.resolveLocale(request);
			}
			processToolContext.setUser(user);



			I18NSource messageSource = i18NSourceFactory.createI18NSource(locale);
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
