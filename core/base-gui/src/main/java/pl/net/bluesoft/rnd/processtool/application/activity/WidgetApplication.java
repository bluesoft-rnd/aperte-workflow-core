package pl.net.bluesoft.rnd.processtool.application.activity;

import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import com.google.common.eventbus.EventBus;
import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;

/**
 * Activity application standalone version to use outside portal portlet and
 * for fast link process view
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WidgetApplication extends Application  implements HttpServletRequestListener
{
	protected I18NSource i18NSource;
	protected Locale locale = null;
	
	private static Logger logger = Logger.getLogger(WidgetApplication.class.getName());
    
    private boolean initialized = false;

    @Autowired
    private ProcessToolRegistry processToolRegistry;
    
    @Autowired
    private EventBus eventBus;
    
    private Window blankWindow;

    
    public WidgetApplication()
    {
    	this.i18NSource = I18NSourceFactory.createI18NSource(Locale.getDefault());
    }

    
   
	@Override
	public synchronized Window getWindow(String name) 
	{

		WebApplicationContext context = (WebApplicationContext)this.getContext();
		if(context == null)
		{
			if(blankWindow == null)
			{
				blankWindow = new Window();
				//setMainWindow(blankWindow);
			}
			
			return blankWindow;
		}
		
		logger.warning("Window get: "+name+", context: "+this.getContext()+" appId: "+Thread.currentThread().getName());

		ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)context.getHttpSession().getAttribute(ProcessToolBpmSession.class.getName());
		
		Window window = super.getWindow(name);
		
		/* Window for specified tab with given name already exists, return it */
		if(window != null)
			return window;
		
		if(i18NSource == null)
			this.i18NSource = I18NSourceFactory.createI18NSource(Locale.getDefault());
		
		/* New tab was opened, create new window for it */
		WidgetViewWindow newWindow = new WidgetViewWindow(processToolRegistry, bpmSession, this, i18NSource, eventBus);
		newWindow.setSizeFull();
		newWindow.setName(name);
		
		addWindow(newWindow);
		//newWindow.open(new ExternalResource(newWindow.getURL()));

		return newWindow; 
	}



	@Override
	public void onRequestStart(final HttpServletRequest request, HttpServletResponse response) 
	{	
		if(!initialized)
			init();
		
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
//		String windowName = request.getParameter("windowName");
//		String onUnloadBurst = request.getParameter("onunloadburst");
//
//		if(windowName != null)
//		{
//			Window window = getWindow(windowName);
//			if("1".equals(onUnloadBurst))
//			{
//				removeWindow(window);
//			}
//		}
		logger.warning("request url: "+request.getRequestURL());
		if(ctx == null)
		{
			UserData user = (UserData)request.getSession().getAttribute(UserData.class.getName());
			if(user == null)
			{
				processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
					
					@Override
					public void withContext(ProcessToolContext ctx) 
					{
						IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
						
						UserData user = authorizationService.getUserByRequest(request);
						
						request.getSession().setAttribute(UserData.class.getName(), user);
						
						ProcessToolBpmSession bpmSession =  (ProcessToolBpmSession) request.getSession().getAttribute(ProcessToolBpmSession.class.getName()); 
						if(bpmSession == null)
						{
							bpmSession = ctx.getProcessToolSessionFactory().createSession(user, user.getRoleNames());
							request.getSession().setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
						}
						
						WidgetApplication.this.i18NSource = I18NSourceFactory.createI18NSource(request.getLocale());
						
					}
				});
			}
			

		}
	}
	
	@Override
	public void onRequestEnd(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void init() 
	{
		if(initialized)
			return;
		
		initialized = true;
		
		setMainWindow(getWindow(null));
		
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		
	}
}
