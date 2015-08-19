package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.eventbus.listenables.Listenable;
import pl.net.bluesoft.util.eventbus.listenables.ListenableSupport;
import pl.net.bluesoft.util.lang.Strings;

import javax.portlet.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class GenericVaadinPortlet2BpmApplication extends Application implements
        PortletApplicationContext2.PortletListener, I18NSource, VaadinExceptionHandler,
        Listenable<GenericVaadinPortlet2BpmApplication.RequestParameterListener> {

    private static Logger logger = Logger.getLogger(GenericVaadinPortlet2BpmApplication.class.getName());

    protected boolean loginRequired = true;
    protected UserData user = null;
    protected Collection<String> userRoles = null;
    protected boolean initialized = false;
    protected Locale locale = null;
    
    protected ProcessToolBpmSession bpmSession;
    protected I18NSource i18NSource;

    private String showKeysString;
    private boolean showExitWarning;
    private Locale lastLocale;
    
    @Autowired
    protected IPortalUserSource portalUserSource;
    
    protected abstract void initializePortlet();

    protected abstract void renderPortlet();

    protected final ListenableSupport<RequestParameterListener> listenable = ListenableSupport.strongListenable();

    @Override
    public void init() 
    {
        final Window mainWindow = new Window();
        setMainWindow(mainWindow);
        ApplicationContext applicationContext = getContext();
        
        
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        if (applicationContext instanceof PortletApplicationContext2) 
        {
            PortletApplicationContext2 portletCtx = (PortletApplicationContext2) applicationContext;
            
            portletCtx.addPortletListener(this, this);
        } else {
            mainWindow.addComponent(new Label(getMessage("please.use.from.a.portlet")));
        }
    }

    @Override
	public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window)
    {
        showKeysString = request.getParameter("showKeys");

		if (request.getLocale() != null) {
			setLocale(request.getLocale());
		}
		else {
			setLocale(Locale.getDefault());
		}
		
		if (lastLocale!=null && !getLocale().equals(lastLocale)){
			VaadinUtility.informationNotification(this, getMessage("please.relog.lang.change"));
		}
		lastLocale = getLocale();
		
    	/* init user source */
		ObjectFactory.inject(this);
		
		user = portalUserSource.getUserByRequest(request);
        userRoles = user != null ? user.getRoles() : Collections.<String>emptyList();

        if (user == null) {
            if (loginRequired) {
                window.removeAllComponents();
                window.addComponent(new Label(getMessage("please.log.in")));
                return;
            }
        } else {
            PortletSession session = ((PortletApplicationContext2)getContext()).getPortletSession();
            bpmSession = (ProcessToolBpmSession) session.getAttribute("bpmSession", PortletSession.APPLICATION_SCOPE);

            if (bpmSession == null) {
                session.setAttribute("bpmSession",
                        bpmSession = getRegistry().getProcessToolSessionFactory().createSession(user.getLogin(), userRoles),
                        PortletSession.APPLICATION_SCOPE);
            }
        }
        if (!initialized) {
            initializePortlet();
        }
        initialized = true;
        renderPortlet();
        handleRequestListeners(request);
    }

    public boolean showKeys() {
        return showKeysString != null;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    @Override
	public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    @Override
    public String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    @Override
    public String getMessage(String key, Object... params) {
        return i18NSource.getMessage(key, params);
    }

	@Override
	public void setLocale(Locale locale) {
		super.setLocale(locale);
		this.locale = locale;
		this.i18NSource = I18NSourceFactory.createI18NSource(locale);
	}

	@Override
	public void handleActionRequest(ActionRequest request, ActionResponse response, Window window) {
        // nothing
    }

    @Override
	public void handleEventRequest(EventRequest request, EventResponse response, Window window) {
        // nothing
    }

    @Override
	public void handleResourceRequest(ResourceRequest request, ResourceResponse response, Window window) {
        if (showExitWarning && Strings.hasText(request.getResourceID()) && request.getResourceID().equals("UIDL")) {
            VaadinUtility.registerClosingWarning(getMainWindow(), getMessage("page.reload"));
        }
    }

    @Override
	public void onThrowable(Throwable e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
		if (e instanceof TaskAlreadyCompletedException) {
			VaadinUtility.errorNotification(this, i18NSource, i18NSource.getMessage("task.already.completed"));
		}
		else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			getMainWindow().showNotification(VaadinUtility.validationNotification(i18NSource.getMessage("process-tool.exception.occurred"),
					getMessage(e.getMessage())));
		}
    }

    public boolean hasMatchingRole(String roleName) {
        for (String role : userRoles) {
            if (role != null && role.matches(roleName)) {
                return true;
            }
        }
        return false;
    }

    public void setShowExitWarning(boolean showExitWarning) {
        this.showExitWarning = showExitWarning;
    }

    public abstract static class RequestParameterListener implements EventListener<RequestParameterEvent> {
        protected final Set<String> supportedParameters;

        public RequestParameterListener(final String... supportedParameters) {
            this(new HashSet<String>() {{
                for (String param : supportedParameters) {
                    add(param);
                }
            }});
        }

        public RequestParameterListener(Set<String> supportedParameters) {
            if (supportedParameters.isEmpty()) {
                throw new IllegalArgumentException("Supported parameters set cannot be empty");
            }
            this.supportedParameters = supportedParameters;
        }

        @Override
        public void onEvent(RequestParameterEvent requestParameterEvent) {
            Map<String, String[]> parameterMap = requestParameterEvent.getParameterMap();
            Map<String, String[]> mappedParameters = new HashMap<String, String[]>();
            for (String param : supportedParameters) {
                String[] values = parameterMap.get(param);
                if (values != null && values.length > 0) {
                    mappedParameters.put(param, values);
                }
            }
            if (!mappedParameters.isEmpty()) {
                handleRequestParameters(mappedParameters);
            }
        }

        public abstract void handleRequestParameters(Map<String, String[]> parameters);
    }

    public static class RequestParameterEvent {
        private final Map<String, String[]> parameterMap;

        public RequestParameterEvent(Map<String, String[]> parameterMap) {
            this.parameterMap = parameterMap;
        }

        public Map<String, String[]> getParameterMap() {
            return parameterMap;
        }
    }

    @Override
    public void addListener(RequestParameterListener listener) {
        listenable.addListener(listener);
    }

    @Override
    public void removeListener(RequestParameterListener listener) {
        listenable.addListener(listener);
    }

    private void handleRequestListeners(RenderRequest request) {
        if (listenable.hasListeners()) 
        {
            Map<String, String[]> parameterMap = request.getParameterMap();
            listenable.fireEvent(new RequestParameterEvent(parameterMap));
        }
    }
}
