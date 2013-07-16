package pl.net.bluesoft.rnd.processtool.application.activity;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.event.SaveTaskEvent;
import pl.net.bluesoft.rnd.processtool.event.ValidateTaskEvent;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
@Component
public class WindowManager 
{
    @Autowired
    private ProcessToolRegistry processToolRegistry;
    
    @Autowired
    private EventBus eventBus;
    
    private boolean initialized = false;
    
    private Map<String, WidgetViewWindow> widgetWindows = new HashMap<String, WidgetViewWindow>();
    
    public void addWindow(String windowName, WidgetViewWindow widgetWinow)
    {
    	if(!initialized)
    		init();
    	
    	this.widgetWindows.put(windowName, widgetWinow);
    }
    
    public WidgetViewWindow getWindow(String windowName)
    {
    	
    	WidgetViewWindow window = this.widgetWindows.get(windowName);
    	
    	return window;
    }
    
    public void removeWindow(String windowName)
    {
    	WidgetViewWindow windowToDestroy = getWindow(windowName);
    	windowToDestroy.destroy();
    	
    	this.widgetWindows.remove(windowName);
    }
    
    private synchronized void init()
    {
    	initialized = true;
    	eventBus.register(this);
    }
    
    @Subscribe
    public void listen(final ValidateTaskEvent event)
    {
    	processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() 
    	{
	
			@Override
			public void withContext(ProcessToolContext ctx) 
			{	
				for(WidgetViewWindow window: widgetWindows.values())
					window.validateWidgets(event);
			}
		});
    }

    @Subscribe
    public void listen(final SaveTaskEvent event)
    {
    	processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() 
    	{
	
			@Override
			public void withContext(ProcessToolContext ctx) 
			{	
				for(WidgetViewWindow window: widgetWindows.values())
					window.saveWidgets(event);
			}
		});
    }

}
