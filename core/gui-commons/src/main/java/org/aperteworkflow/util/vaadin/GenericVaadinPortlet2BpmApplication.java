package org.aperteworkflow.util.vaadin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.aperteworkflow.util.liferay.LiferayBridge;
import org.aperteworkflow.util.liferay.PortalBridge;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.eventbus.listenables.Listenable;
import pl.net.bluesoft.util.eventbus.listenables.ListenableSupport;
import pl.net.bluesoft.util.lang.Strings;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class GenericVaadinPortlet2BpmApplication extends Application implements
        PortletApplicationContext2.PortletListener, TransactionProvider, I18NSource, VaadinExceptionHandler,
        Listenable<GenericVaadinPortlet2BpmApplication.RequestParameterListener> {

    private static Logger logger = Logger.getLogger(GenericVaadinPortlet2BpmApplication.class.getName());

    protected boolean loginRequired = true;
    protected UserData user = null;
    protected Collection<String> userRoles = null;
    protected boolean initialized = false;
    protected Locale locale = null;

    protected ProcessToolBpmSession bpmSession;
    protected I18NSource i18NSource;

    private List<UserData> users;
    private String showKeysString;
    private boolean showExitWarning = false;

    protected abstract void initializePortlet();

    protected abstract void renderPortlet();

    protected final ListenableSupport<RequestParameterListener> listenable = ListenableSupport.strongListenable();

    @Override
    public void init() {
        final Window mainWindow = new Window();
        setMainWindow(mainWindow);
        ApplicationContext applicationContext = getContext();
        if (applicationContext instanceof PortletApplicationContext2) {
            PortletApplicationContext2 portletCtx = (PortletApplicationContext2) applicationContext;
            portletCtx.addPortletListener(this, this);
        } else {
            mainWindow.addComponent(new Label(getMessage("please.use.from.a.portlet")));
        }
    }

    @Override
    public void withTransaction(final ProcessToolGuiCallback r) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        r.callback(ctx, bpmSession);
    }

    public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
        showKeysString = PortalBridge.getCurrentRequestParameter("showKeys");

		if (request.getLocale() != null) {
			setLocale(request.getLocale());
		}
		else {
			setLocale(Locale.getDefault());
		}

		user = PortalBridge.getLiferayUser(request);
        userRoles = user != null ? user.getRoleNames() : Collections.<String>emptyList();

        if (user == null) {
            if (loginRequired) {
                window.removeAllComponents();
                window.addComponent(new Label(getMessage("please.log.in")));
                return;
            }
        } else {
            PortletSession session = ((PortletApplicationContext2) (getContext())).getPortletSession();
            bpmSession = (ProcessToolBpmSession) session.getAttribute("bpmSession", PortletSession.APPLICATION_SCOPE);
            if (bpmSession == null) {
                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                session.setAttribute("bpmSession",
                        bpmSession = ctx.getProcessToolSessionFactory().createSession(user, userRoles),
                        PortletSession.APPLICATION_SCOPE);
            }
            setUser(user);
            
        }
        if (!initialized) {
            initializePortlet();
        }
        initialized = true;
        renderPortlet();
        handleRequestListeners();
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

    public UserData getUser() {
        return user;
    }

    public UserData getUser(String login) {
        return LiferayBridge.getLiferayUser(login, user.getCompanyId());
    }

    public UserData getUserByEmail(String email) {
        return LiferayBridge.getLiferayUserByEmail(email, user.getCompanyId());
    }

    public List<UserData> getAllUsers() {
        return users == null ? (users = LiferayBridge.getAllUsersByCurrentUser(user)) : users;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    @Override
    public String getMessage(String key) {
        return i18NSource.getMessage(key, key);
    }

    @Override
    public String getMessage(String key, String defaultValue) {
        return i18NSource.getMessage(key, defaultValue);
    }

    @Override
    public String getMessage(String key, Object... params) {
        return i18NSource.getMessage(key, params);
    }

    @Override
    public String getMessage(String key, String defaultValue, Object... params) {
        return i18NSource.getMessage(key, defaultValue, params);
    }

	@Override
	public void setLocale(Locale locale) {
		super.setLocale(locale);
		this.locale = locale;
		this.i18NSource = I18NSourceFactory.createI18NSource(locale);
	}

	public void handleActionRequest(ActionRequest request, ActionResponse response, Window window) {
        // nothing
    }

    public void handleEventRequest(EventRequest request, EventResponse response, Window window) {
        // nothing
    }

    public void handleResourceRequest(ResourceRequest request, ResourceResponse response, Window window) {
        if (showExitWarning && Strings.hasText(request.getResourceID()) && request.getResourceID().equals("UIDL")) {
            VaadinUtility.registerClosingWarning(getMainWindow(), getMessage("page.reload"));
        }
    }

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

    public static abstract class RequestParameterListener implements EventListener<RequestParameterEvent> {
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

        public Map getParameterMap() {
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

    private void handleRequestListeners() {
        if (listenable.hasListeners()) {
            Map<String, String[]> parameterMap = PortalBridge.getCurrentRequestParameterMap();
            listenable.fireEvent(new RequestParameterEvent(parameterMap));
        }
    }
}
