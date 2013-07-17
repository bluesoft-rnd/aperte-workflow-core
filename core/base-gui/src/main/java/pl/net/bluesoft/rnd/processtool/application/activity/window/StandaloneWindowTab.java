package pl.net.bluesoft.rnd.processtool.application.activity.window;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.application.activity.ActivityStandAloneApplication;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.processtool.token.TokenWrapper;
import pl.net.bluesoft.rnd.processtool.token.exception.TokenException;
import pl.net.bluesoft.rnd.processtool.ui.activity.AbstractActivityView;
import pl.net.bluesoft.rnd.processtool.ui.activity.ActivitySimplePanel;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * This class represents browser tab. The logic was moved from Application
 * to support multiple tabs
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class StandaloneWindowTab extends Window implements ParameterHandler, ClickListener, I18NSource
{
	public static final String LOGIN_PAGE_LAYOUT_STYLE = "standalone-layout";
	public static final String ERROR_VIEW_LAYOUT_STYLE = "error-view-layout";
	
	private static final long serialVersionUID = -7644114768803440276L;
	
	@AutoInject
	protected ITokenService tokenService;
	
	@AutoInject
	protected IPortalUserSource userSource;
	
	@AutoInject
	protected IAuthorizationService authorizationService;
	
	private ActivityStandAloneApplication application;
	
	private UserData user;
	private BpmTask task;
	//protected I18NSource i18NSource;
	protected Locale locale = null;
	
	protected I18NSource i18NSource;

	
    private Button btnLogin = new Button("Login");
    private TextField login = new TextField ( "Username");
    private PasswordField password = new PasswordField ( "Password");
    
    private AbstractActivityView activityMainView;
    
    private String tokenId;
    private ProcessToolBpmSession bpmSession;
	
	public StandaloneWindowTab(ActivityStandAloneApplication application) 
	{
		this.application = application;
		
		addParameterHandler(this);
		setSizeFull();
		
        /* Dependency Injection */
        ObjectFactory.inject(this);
	}
	
	@Override
	public void handleParameters(Map<String, String[]> parameters) 
	{
		if(parameters.containsKey("tokenId"))
		{
			String newTokenId = ((String[]) parameters.get("tokenId"))[0];
			
			if(newTokenId != null)
			{
				/* If tokenId has changed (multiple tabs), reinitilize view */
				if(!newTokenId.equals(tokenId)) 
					resetView();

				tokenId = newTokenId;
				
				try 
				{
					tokenService = ObjectFactory.create(ITokenService.class);
					TokenWrapper tokenWrapper = tokenService.getTokenWrapperByTokenId(tokenId);

					user = tokenWrapper.getUser();
					task = tokenWrapper.getTask();
					
					application.setUser(user);
					
					renderView();
				}
				catch(TokenException ex)
				{
					resetView();
					
					setContent(createInvalidTokenComponent());
					
					return;
				}
			}
			else
				resetView();
		}
		else
			resetView();
		
	}
	
	private void resetView()
	{
		removeAllComponents();
		activityMainView = null;
		setContent(null);
		
		tokenId = null;
		task = null;
		
		application.setUser(null);
	}


	public void init(HttpServletRequest request) 
	{
		/* Check for client local */
		initializeLocal(request);
	
		
		if(user == null)
			user = authorizationService.getUserByRequest(request);
		
		if(user == null)
			user = (UserData)request.getSession().getAttribute("user");
		
		bpmSession =  (ProcessToolBpmSession) request.getSession().getAttribute("bpmSessionStandAlone"); 
		
		application.setUser(user);

		getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
			@Override
			public void withContext(ProcessToolContext ctx) {
				renderViewWithContext();
			}
		}); 
		

	}
	
	private void renderView()
	{
		getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
			@Override
			public void withContext(ProcessToolContext ctx) {
				renderViewWithContext();
			}
		}); 
	}
	
	private void renderViewWithContext() 
	{    	
		if(tokenId == null) 
		{
			removeAllComponents();
			
			setContent(createInvalidTokenComponent());
		}
		
		/* If no user is set for current rquest, show login window */
		else if(user == null)
        {
            
			removeAllComponents();
			setContent(createLoginComponent());
        }
        /* Otherwise, render main view */
        else
        {
        	renderActivityView();
        }
	}
	
	
	
	/** Check for local */
	private void initializeLocal(HttpServletRequest request)
	{
		if (request.getLocale() != null) {
			setLocale(request.getLocale());
		}
		else {
			setLocale(Locale.getDefault());
		}
		
		I18NSource.ThreadUtil.setThreadI18nSource(I18NSourceFactory.createI18NSource(request.getLocale()));
	}
	
	/** Render main view {@link ActivitySimplePanel} */
	private void renderActivityView()
	{
    	if(user != null && bpmSession == null)
    	{
    		bpmSession = getRegistry().getProcessToolSessionFactory().createSession(user, user.getRoleNames());
    	}
		 
    	/* If there is no main view initialize, create one */
    	if(activityMainView == null)  
    	{
        	activityMainView = new ActivitySimplePanel(application, this, bpmSession);
        	removeAllComponents();
        	setContent(activityMainView);
        	
        	if(task != null)
        	{
        		activityMainView.displayProcessData(task);
        	}
    	}
    	else if(getContent() == null)
    	{
    		setContent(activityMainView);
    	}
	}
	
	private ComponentContainer createInvalidTokenComponent()
	{
		CssLayout errorLayout = new CssLayout();
		errorLayout.addStyleName(ERROR_VIEW_LAYOUT_STYLE);
		errorLayout.addComponent(new Label(getMessage("invalid.tokenid")));
		
		return errorLayout;
	}
	
	
	private ComponentContainer createLoginComponent()
	{
		
		CssLayout layout = new CssLayout();
		layout.setSizeFull();
		layout.addStyleName(LOGIN_PAGE_LAYOUT_STYLE);
		
		Label pleaseLoginLabel = new Label ("Please login in order to use the application");
		
		layout.addComponent ( pleaseLoginLabel );
		layout.addComponent ( login );
		layout.addComponent ( password );
		layout.addComponent ( btnLogin );
		
		
		 btnLogin.addListener (this);
        
        return layout;
	}
	
	@Override
	public void buttonClick(ClickEvent event) 
	{
    	String providedLogin = (String)login.getValue();
    	String providedPassword = (String)password.getValue();
    	user = authorizationService.authenticateByLogin(providedLogin, providedPassword);
		
	}
	
	@Override
	public void setLocale(Locale locale) {
		super.setLocale(locale);
		this.locale = locale;
		this.i18NSource = I18NSourceFactory.createI18NSource(locale);
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

}
