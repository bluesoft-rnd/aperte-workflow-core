package pl.net.bluesoft.rnd.processtool.ui.admin;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable;
import pl.net.bluesoft.util.lang.Strings;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.gwt.client.ui.AlignmentInfo.Bits;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;



public class AdminMainPane extends VerticalLayout implements Refreshable {
    private Application application;
    private I18NSource i18NSource;
    private ProcessToolBpmSession bpmSession;

    private TextField creatorSearchField = new TextField();
    private TextField assigneeSearchField = new TextField();
    private TextField taskNameSearchField = new TextField();
    private TextField executionIdSearchField = new TextField();
    private TextField externalIdSearchField = new TextField(); 
    private static final String FIELD_WIDTH = "150px";
    
    private BeanItemContainer<AdminTaskTableItem> bic = new BeanItemContainer<AdminTaskTableItem>(AdminTaskTableItem.class);
    
    private Window modalWindow;
	
    private LocalizedPagedTable table; 
    
    
    public AdminMainPane(Application application, I18NSource i18NSource, final ProcessToolBpmSession bpmSession) {
        this.application = application;
        this.i18NSource = i18NSource;
        this.bpmSession = bpmSession;
        
        setWidth("100%");
        setSpacing(true);
        
        removeAllComponents();
        
        HorizontalLayout hLay = new HorizontalLayout();
        
        creatorSearchField.setCaption(getMessage("admin.creator"));
        creatorSearchField.setWidth(FIELD_WIDTH);
		hLay.addComponent(creatorSearchField);
        
		assigneeSearchField.setCaption(getMessage("admin.assignee"));
		assigneeSearchField.setWidth(FIELD_WIDTH);
		hLay.addComponent(assigneeSearchField);

		taskNameSearchField.setCaption(getMessage("admin.taskName"));
		taskNameSearchField.setWidth(FIELD_WIDTH);
		hLay.addComponent(taskNameSearchField);
		
		executionIdSearchField.setCaption(getMessage("admin.executionId"));
		executionIdSearchField.setWidth(FIELD_WIDTH);
		hLay.addComponent(executionIdSearchField);
		
		externalIdSearchField.setCaption(getMessage("admin.externalId"));
		externalIdSearchField.setWidth(FIELD_WIDTH);
		hLay.addComponent(externalIdSearchField);

		Button searchBtn = VaadinUtility.button(getMessage("admin.search"), null, null,
	                new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {	
							refreshTable();
						}
	                });
		
		
		
		hLay.addComponent(searchBtn);
		hLay.setComponentAlignment(searchBtn, new Alignment(Bits.ALIGNMENT_BOTTOM));
		hLay.setSpacing(true);
		
		addComponent(hLay);
		
        createTaskTable();
        
        VerticalLayout tableCarrier = new VerticalLayout();
		tableCarrier.setWidth("100%");
		tableCarrier.addComponent(table);
		tableCarrier.addComponent(table.createControls(getMessage("admin.itemsPerPage"), getMessage("admin.page")));
		
        
        addComponent(tableCarrier);
  
    }
    
    
    private void fillTableContainer() {
    	ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        Collection<BpmTask> taskList = bpmSession.getAllTasks(ctx);
        
        for (BpmTask bpmTask : taskList) {
			String assignee = bpmTask.getOwner() == null ? "" : defaultString(bpmTask.getOwner().getLogin());
			String creator = defaultString(bpmTask.getCreator()); 
        	String id = defaultString(bpmTask.getInternalTaskId());
        	String taskName = defaultString(bpmTask.getTaskName());
        	String executionId = defaultString(bpmTask.getExecutionId());
        	String externalId = defaultString(bpmTask.getProcessInstance().getExternalKey());
        	 
        	
        	String assigneeField = defaultString((String)assigneeSearchField.getValue());
        	String creatorField = defaultString((String)creatorSearchField.getValue());
        	String taskNameField = defaultString((String)taskNameSearchField.getValue());
        	String executionIdField = defaultString((String)executionIdSearchField.getValue());
        	String externalIdField = defaultString((String)externalIdSearchField.getValue());
        	
        	
        	boolean include = true;
        	if (Strings.hasText(assigneeField) && !assignee.toUpperCase().contains(assigneeField.toUpperCase())) {
        		include = false;
        	}
        	if (include && Strings.hasText(creatorField) && !creator.toUpperCase().contains(creatorField.toUpperCase())) {
        		include = false;
        	}
        	if (include && Strings.hasText(taskNameField) && !taskName.toUpperCase().contains(taskNameField.toUpperCase())) {
        		include = false;
        	}
        	if (include && Strings.hasText(executionIdField) && !executionId.toUpperCase().contains(executionIdField.toUpperCase())) {
        		include = false;
        	}
        	if (include && Strings.hasText(externalIdField) && !externalId.toUpperCase().contains(externalIdField.toUpperCase())) {
        		include = false;
        	}
        	
        	if (include) {
        	  AdminTaskTableItem atti = new AdminTaskTableItem();
			  atti.setAssignee(assignee);
			  atti.setCreator(creator);
			  atti.setId(id);
			  atti.setTaskName(taskName);
			  atti.setExecutionId(executionId);
			  atti.setExternalId(externalId);
			  bic.addBean(atti);
        	}
		}
    }
    
	private void createTaskTable() {
		
		table = new LocalizedPagedTable();
		table.setSizeFull();
	
		table.setSortAscending(true);
		table.setSortContainerPropertyId("creator");
		
		table.setWidth("100%");
		//table.setHeight("300px");
		table.setImmediate(true); // react at once when something is selected
		table.setSelectable(true);
		
        fillTableContainer();
		
		table.setContainerDataSource(bic);
		setTableColumns();

		table.addListener(new ItemClickEvent.ItemClickListener() {
			public void itemClick(final ItemClickEvent event) {
				BeanItem<AdminTaskTableItem> item = (BeanItem<AdminTaskTableItem>)event.getItem();
				openModalWindow(item.getBean());
				
				
			}
		});
		table.setPageLength(25);
	}
    
	private void setTableColumns() {
		table.setVisibleColumns(new Object[] { "creator", "assignee", "taskName", "executionId", "externalId" });

		for (Object o : table.getVisibleColumns()) {
			table.setColumnHeader(o, getMessage("admin.tasks." + o));
		}
	}
	
    private void openModalWindow(AdminTaskTableItem item) {
    	
	
		UserSearchForm userSearchForm = new UserSearchForm(application, bpmSession, i18NSource, item, this);
        
		userSearchForm.setWidth("800px");
		userSearchForm.setMargin(true);
		modalWindow = createModalWindow(getMessage("admin.selectUser"), userSearchForm);
		
		
		application.getMainWindow().addWindow(modalWindow);
    }
	
    public void closeModalWindow() {
    	if (modalWindow != null) {
    		application.getMainWindow().removeWindow(modalWindow);
    		modalWindow = null;
    		refreshTable();
    	}
    }
    
    private String getMessage(String key) {
        return i18NSource.getMessage(key);
    }
    public void refreshData() {
       
    }
    public void refreshTable() {
    	bic.removeAllItems();
    	fillTableContainer();
    	table.setContainerDataSource(bic);
    	setTableColumns();
    }
    public Window createModalWindow(String title, ComponentContainer content) {
		Window window = new Window(title, content);
		window.setClosable(true);
		window.setModal(true);
		window.setSizeUndefined();
		window.setResizable(false);
		
		window.addListener(new CloseListener() {
			
			@Override
			public void windowClose(CloseEvent e) {
				modalWindow = null;
				refreshTable();
			}
		});
		return window;
	}
    private static String defaultString(String str) {
        return str == null ? "" : str;
    }
}
