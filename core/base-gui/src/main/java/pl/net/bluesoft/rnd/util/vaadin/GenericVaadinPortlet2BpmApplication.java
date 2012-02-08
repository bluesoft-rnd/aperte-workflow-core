package pl.net.bluesoft.rnd.util.vaadin;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.i18n.DefaultI18NSource;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.liferay.LiferayBridge;

import javax.portlet.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class GenericVaadinPortlet2BpmApplication extends Application implements
		PortletApplicationContext2.PortletListener, TransactionProvider, I18NSource, VaadinExceptionHandler {

	private static Logger logger = Logger.getLogger(GenericVaadinPortlet2BpmApplication.class.getName());

	protected boolean loginRequired = true;
	protected UserData user = null;
	protected Collection<String> userRoles = null;
	protected boolean initialized = false;
	protected Locale locale = null;

	protected ProcessToolBpmSession bpmSession;
	protected I18NSource i18NSource;

	private RenderRequest request;

	private List<UserData> users;

	protected abstract void initializePortlet();

	protected abstract void renderPortlet();

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
        setErrorHandler(new Terminal.ErrorListener() {
            @Override
            public void terminalError(Terminal.ErrorEvent errorEvent) {
                onThrowable(errorEvent.getThrowable());
            }
        });
	}

	@Override
	public void withTransaction(final ProcessToolGuiCallback r) {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		r.callback(ctx, bpmSession);
	}

	public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
		this.request = request;
		locale = request.getLocale();
		i18NSource = new DefaultI18NSource();
		i18NSource.setLocale(locale);
		user = LiferayBridge.getLiferayUser(request);
		userRoles = LiferayBridge.getLiferayUserRoles(request);
		if (locale == null) {
			locale = Locale.getDefault();
		}

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

		}
		if (!initialized)
			initializePortlet();
		initialized = true;
		renderPortlet();

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

	public List<UserData> getAllUsers() {
		if (users == null)
			users = LiferayBridge.getAllUsers(user);
		return users;
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

    public void handleActionRequest(ActionRequest request, ActionResponse response, Window window) {
		// nothing
	}

	public void handleEventRequest(EventRequest request, EventResponse response, Window window) {
		// nothing
	}

	public void handleResourceRequest(ResourceRequest request, ResourceResponse response, Window window) {
		// nothing
	}

    @Override
    public void onThrowable(Throwable e) {
        logger.log(Level.SEVERE, e.getMessage(), e);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        getMainWindow().showNotification(VaadinUtility.validationNotification(i18NSource.getMessage("process-tool.exception.occurred"),
                                         getMessage(e.getMessage())));
    }
}
