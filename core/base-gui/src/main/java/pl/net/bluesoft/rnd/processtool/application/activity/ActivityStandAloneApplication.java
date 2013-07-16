package pl.net.bluesoft.rnd.processtool.application.activity;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.rnd.processtool.application.activity.window.StandaloneWindowTab;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

/**
 * Activity application standalone version to use outside portal portlet and
 * for fast link process view
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ActivityStandAloneApplication extends Application  implements HttpServletRequestListener
{
	protected ProcessToolRegistry processToolRegistry;

	protected I18NSource i18NSource;
	protected Locale locale = null;

    
    private HttpServletRequest request;
    
    private boolean initialized = false;
    private StandaloneWindowTab mainTabWindow;

   
	@Override
	public Window getWindow(String name) 
	{
		
		Window window = super.getWindow(name);
		
		/* Window for specified tab with given name already exists, return it */
		if(window != null)
			return window;
		
		/* New tab was opened, create new window for it */
		StandaloneWindowTab newWindow = new StandaloneWindowTab(this);
		newWindow.init(request);
		newWindow.setName(name);
		
		addWindow(newWindow);
		//newWindow.open(new ExternalResource(newWindow.getURL()));
		
		return newWindow; 
	}


	@Override
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) 
	{	
		
		this.request = request;
		
		if(processToolRegistry == null)
		{
			ServletContext context = request.getSession().getServletContext();	
			processToolRegistry = (ProcessToolRegistry)context.getAttribute(ProcessToolRegistry.class.getName());
		}

		if(!initialized)
		{
			mainTabWindow = new StandaloneWindowTab(this);
			mainTabWindow.init(request);
	        setMainWindow(mainTabWindow); 
	        
	        setTheme("aperteworkflow");
	        
	        /* Dependency Injection */
	        ObjectFactory.inject(this);
	        
	        initialized = true;
		}
		
	}
	
	@Override
	public void onRequestEnd(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}




    

	


}
