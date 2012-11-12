package pl.net.bluesoft.rnd.processtool.ui.activity;

import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;
import org.aperteworkflow.util.view.AbstractListPane;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TaskTableItem;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderBase;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderParams;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public abstract class ProcessListPane extends AbstractListPane {
    protected ActivityMainPane activityMainPane;
    protected VerticalLayout dataPane = new VerticalLayout();
    
    protected int limit = 10;
    protected int offset = 0;
    protected int totalResults = 0;
    
    private List<BpmTask> bpmTasks = Collections.synchronizedList(new ArrayList<BpmTask>());
    
    private TasksFilterBox filterBox;
    private ProcessInstanceFilter filter;
    
    private NavigationComponent topNavigationComponent;
    private NavigationComponent bottomNavigationComponent;

    public ProcessListPane(ActivityMainPane activityMainPane, String title, ProcessInstanceFilter filter) {
        super(activityMainPane.getApplication(), activityMainPane.getI18NSource(), title);
        this.activityMainPane = activityMainPane;
        this.filter = filter;
		this.addRefreshButton = false;
    }

    public ProcessListPane(ActivityMainPane activityMainPane, String title) {
        this(activityMainPane, title, null);
    }

    public void setFilter(ProcessInstanceFilter filter) {
        this.filter = filter;
    }

    @Override
    public ProcessListPane init() 
    {
        super.init();
        
        limit = 10;
        offset = 0;
        totalResults = 0;
        
        filter = filter == null ? getDefaultFilter() : filter;

        VerticalLayout marginPanel = new VerticalLayout();
        marginPanel.addComponent(new Label(getMessage("activity.tasks.help.short"), Label.CONTENT_XHTML));
        marginPanel.setMargin(true);
        marginPanel.setWidth("100%");
        addComponent(marginPanel);

        filterBox = new TasksFilterBox(messageSource, getBpmSession(), application, this);
        filterBox.setFilter(filter);
        filterBox.setLimit(limit);

        marginPanel.addComponent(filterBox);
        addComponent(dataPane);
        setExpandRatio(dataPane, 1.0f);
        
        refreshData();
        
        topNavigationComponent = new NavigationComponent();
        bottomNavigationComponent = new NavigationComponent();
        
        reloadView();

        filterBox.addListener(new TasksFilterBox.ItemSetChangeListener() {
            @Override
            public void itemSetChange() {
                reloadView();
            }
        });
        return this;
    }

    protected ProcessToolBpmSession getBpmSession() {
        return activityMainPane.getBpmSession();
    }

    protected abstract ProcessInstanceFilter getDefaultFilter();

    public void reloadView() 
    {
        dataPane.setSpacing(getDataPaneUsesSpacing());
        dataPane.setMargin(true);
        dataPane.setWidth("100%");
        dataPane.removeAllComponents();

        
        topNavigationComponent.refresh();
        bottomNavigationComponent.refresh();

        dataPane.addComponent(topNavigationComponent);
        dataPane.setComponentAlignment(topNavigationComponent, Alignment.TOP_RIGHT);


    	for(BpmTask bpmTask: bpmTasks)
    		dataPane.addComponent(getTaskItem(new TaskTableItem(bpmTask)));

        if(bpmTasks.size() > 2)
        {
	        dataPane.addComponent(bottomNavigationComponent);
	        dataPane.setComponentAlignment(bottomNavigationComponent, Alignment.TOP_RIGHT);
        }
    }

    protected void sortTaskItems(List<TaskTableItem> taskItems) {
    }
    
    @Override
    public void refreshData() 
    {
    	/* get current filter from filter box */
		filter = filterBox.getFilter();
		
		if(filter == null)
			new ResultsPageWrapper<BpmTask>();
		
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		totalResults = getBpmSession().getFilteredTasksCount(filter, ctx);

		/* Get tasks filtered by given filter */
		bpmTasks = Collections.synchronizedList(getBpmSession().findFilteredTasks(filter, ctx, offset, limit));
    	
    }
    
	public List<BpmTask> getBpmTasks() 
	{
		return bpmTasks;
	}
	
    protected boolean getDataPaneUsesSpacing() {
        return true;
    }

    protected abstract Component getTaskItem(TaskTableItem tti);
    
    private class NavigationComponent extends  AligningHorizontalLayout implements ClickListener
    {
    	private Button prevButton;
    	private Button nextButton;
    	
    	private Label resultsLabel;

		public NavigationComponent() 
		{
			super(Alignment.MIDDLE_RIGHT, true);
			init();
		}
		
		public void refresh()
		{
			boolean privButtonEnabled = offset-limit >= 0;
			boolean nextButtonEnabled = offset+limit <= totalResults - 1;
			
            prevButton.setEnabled(privButtonEnabled);
            nextButton.setEnabled(nextButtonEnabled);
            
	        int first = getTotalResults() > 0 ? offset + 1 : 0;
	        int last = Math.min(offset + limit, getTotalResults());
            resultsLabel.setValue(String.format(getMessage("activity.tasks.of.line"), first, last, getTotalResults()));
		}
		
		private void init()
		{
	        setWidth("100%");

	        prevButton = VaadinUtility.link(getMessage("activity.tasks.previous"));
	        prevButton.addListener((ClickListener)NavigationComponent.this);
	        
	        nextButton = VaadinUtility.link(getMessage("activity.tasks.next"));
	        nextButton.addListener((ClickListener)NavigationComponent.this);

	        int first = getTotalResults() > 0 ? offset + 1 : 0;
	        int last = Math.min(offset + limit, getTotalResults());
	        
	        HorizontalLayout resultsLayout = new HorizontalLayout();
	        resultsLayout.setMargin(false);
	        resultsLayout.setWidth("75px");

	        resultsLabel = new Label(String.format(getMessage("activity.tasks.of.line"), first, last, getTotalResults()));
	        resultsLayout.addComponent(resultsLabel);
	        resultsLayout.setComponentAlignment(resultsLabel, Alignment.MIDDLE_CENTER);
	        
	        refresh();
	        

	        setMargin(false, true, false, true);
	        addComponents(new Component[] {new Label() {{
	            setWidth("100%");
	        }}, prevButton, resultsLayout, nextButton});
		}

		@Override
		public void buttonClick(ClickEvent event) 
		{
			if(event.getButton().equals(prevButton))
			{
                offset -= limit;

                refreshData();
                reloadView();
			}
			else if(event.getButton().equals(nextButton))
			{
                offset += limit;
                
                refreshData();
                reloadView();
			}
			
		}
    	
    }

    public void setNewSearch() {
        offset = 0;
        titleLabel.setValue(getMessage("activity.search.results"));
    }

    public ActivityMainPane getActivityMainPane() {
        return activityMainPane;
    }

    public int getTotalResults() {
        return totalResults;
    }

	protected static String getLogin(UserData userData) {
		return userData != null ? userData.getLogin() : null;
	}


    protected TaskItemProviderBase getTaskItemProvider(final ProcessToolContext ctx, ProcessInstance pi) 
    {
        String itemClass = pi.getDefinition().getTaskItemClass();
        if (hasText(itemClass) && !itemClass.equals("null")) {
            try {
                return new TaskItemProviderBase(ctx.getRegistry().makeTaskItemProvider(itemClass.trim()));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new TaskItemProviderBase(null);
    }

    private TaskItemProviderParams createTaskItemProviderParams() {
        return new TaskItemProviderParams() {
            @Override
            public Resource getImage(String image) {
                return ProcessListPane.this.getImage(image);
            }

            @Override
            public Resource getResource(String path) {
                return ProcessListPane.this.getResource(path);
            }

            @Override
            public Resource getStreamResource(String path, final byte[] bytes) {
                Resource res = new StreamResource(new StreamResource.StreamSource() {
                    @Override
                    public InputStream getStream() {
                        return new ByteArrayInputStream(bytes);
                    }
                }, path, application);
                cacheResource(path, res);
                return res;
            }

            @Override
            public void onClick() {
                ProcessListPane.this.onClick(this);
            }
        };
    }

    protected TaskItemProviderParams getTaskItemProviderParams(ProcessToolContext ctx, TaskTableItem tti) {
        TaskItemProviderParams params = createTaskItemProviderParams();
        params.setCtx(ctx);
        params.setBpmSession(getBpmSession());
        params.setI18NSource(messageSource);
        params.setProcessInstance(tti.getTask().getProcessInstance());
        params.setTask(tti.getTask());
        params.setProcessStateConfiguration(tti.getStateConfiguration());
        params.setState(tti.getState());
        params.setQueue(null);
        return params;
    }

    protected abstract void onClick(TaskItemProviderParams params);
}
