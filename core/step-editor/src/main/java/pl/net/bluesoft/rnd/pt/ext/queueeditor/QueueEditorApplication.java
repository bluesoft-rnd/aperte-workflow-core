package pl.net.bluesoft.rnd.pt.ext.queueeditor;


import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.JavaScriptHelper;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class QueueEditorApplication extends GenericEditorApplication implements ParameterHandler, ClickListener, ValueChangeListener {

	private static final long		serialVersionUID	= 2136349026207825109L;
	private static final Logger	logger = Logger.getLogger(QueueEditorApplication.class.getName());
	private static final String     NAME = "Name";
	private static final String     DESCRIPTION = "Description";
	private static final String     ROLE_NAME = "Role name";
	private static final String     BROWSE_ALLOWED = "Browse allowed";

	private Window					mainWindow;
	private JavaScriptHelper		jsHelper;
	private String url;
	private Table defsTable;
	private SortedMap<Integer,QueueDef> queueDefs;
    private Integer prevSelected;
    private VerticalLayout rightsPanel;
    private Table rightsTable;
    private Button addDef;
    private Button removeDef;
    private Button addRight;
    private Button removeRight;
    private Button saveButton;
    private static final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		if (parameters == null || parameters.size() == 0) {
            // No parameters to handle, we are not interested in such a request
            // it may be a request for static resource e.g. <servlet>/APP/323/root.gif
            return;
        }

		String[] urls = parameters.get("callback_url");
		if (urls != null && urls.length > 0 && !StringUtils.isEmpty(urls[0])) {
			url = urls[0];
		}
		
		String[] queueConfig = parameters.get("queue_config");
		if (queueConfig != null && queueConfig.length > 0 && !StringUtils.isEmpty(queueConfig[0])) {
			createQueueDefs(queueConfig[0]);
		} else {
			createQueueDefs(null);
		}
	}

	private void createQueueDefs(String queueConfig) {
        
		try {
		  if (!StringUtils.isEmpty(queueConfig))
			queueDefs = mapper.readValue(queueConfig, new TypeReference<SortedMap<Integer,QueueDef>>(){});
		  else
			queueDefs = new TreeMap<Integer, QueueDef>();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating queue defs", e);
		}
		
		prevSelected = null;
		
		mainWindow.removeAllComponents();
		HorizontalLayout main = new HorizontalLayout();
		main.setSpacing(true);
		main.setMargin(true);
		main.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		
		
		VerticalLayout defsPanel = new VerticalLayout();
		rightsPanel = new VerticalLayout();
		main.addComponent(defsPanel);
		main.addComponent(rightsPanel);
		
		
		defsTable = new Table("Queue definitions");
		
		defsTable.addContainerProperty(NAME, String.class,  null);
		defsTable.addContainerProperty(DESCRIPTION,  String.class,  null);

		for (Integer tableId : queueDefs.keySet()) {
			defsTable.addItem(new Object[] {queueDefs.get(tableId).getName(), queueDefs.get(tableId).getDescription()}, tableId);
		}
		
		defsTable.setSelectable(true);
		defsTable.setImmediate(true);
        defsTable.setEditable(true);
        defsTable.addListener(this);
		
		saveButton = new Button("save", this);
		saveButton.setImmediate(true);
		
	    addDef = new Button("add", this);
	    addDef.setImmediate(true);
	    
	    removeDef = new Button("remove", this);
	    removeDef.setImmediate(true);
	    
	    
		defsPanel.addComponent(defsTable);
		HorizontalLayout defsButtons = new HorizontalLayout();
		defsButtons.addComponent(addDef);
		defsButtons.addComponent(removeDef);
		defsPanel.addComponent(defsButtons);
		defsPanel.addComponent(saveButton);
		mainWindow.setContent(main);
	}
	
	private void createRightsButtons() {
		addRight = new Button("add", this);
    	addRight.setImmediate(true);
    	
 	    removeRight = new Button("remove", this);
 	    removeRight.setImmediate(true);
	}
	
	private void save() {
		valueChange(null);
		for (Iterator i = defsTable.getItemIds().iterator(); i.hasNext();) {
		    int iid = (Integer) i.next();
		    QueueDef qd = queueDefs.get(iid);
		    qd.setName((String)defsTable.getItem(iid).getItemProperty(NAME).getValue());
		    qd.setDescription((String)defsTable.getItem(iid).getItemProperty(DESCRIPTION).getValue());
		}
		
		try {
		  String s = mapper.writeValueAsString(queueDefs);
		  jsHelper.postAndRedirectQueue(url, s);
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "Error creating JSON data", e);
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "Error creating JSON data", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating JSON data", e);
		}
	}
	
	
	@Override
	public void init() {
        super.init();
        
		mainWindow = new Window(I18NSource.ThreadUtil.getThreadI18nSource().getMessage("application.title"));
		jsHelper = new JavaScriptHelper(mainWindow);
		jsHelper.preventWindowClosing();
		mainWindow.addParameterHandler(this);
		setMainWindow(mainWindow);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getComponent() == addRight) {
			if (defsTable.getValue() != null) {
				  SortedMap<Integer,QueueRight> rightsList = queueDefs.get((Integer)defsTable.getValue()).getRights();
				  Integer tableId = rightsList.isEmpty() ? 0 : rightsList.lastKey() + 1;
				  rightsList.put(tableId, new QueueRight("", false));
				  rightsTable.addItem(new Object[] {"", false}, tableId);
				  rightsTable.requestRepaint();
			}
		} else if (event.getComponent() == removeRight) {
			if (defsTable.getValue() != null && rightsTable.getValue() != null) {
				  SortedMap<Integer,QueueRight> rightsList = queueDefs.get((Integer)defsTable.getValue()).getRights();
				  rightsList.remove((Integer)rightsTable.getValue());
				  rightsTable.removeItem(rightsTable.getValue());
				  rightsTable.requestRepaint();
			}
		} else if (event.getComponent() == saveButton) {
			save();
		} else if (event.getComponent() == addDef) {
			Integer tableId = queueDefs.isEmpty() ? 0 : queueDefs.lastKey() + 1;
			defsTable.removeListener(this);
			queueDefs.put(tableId, new QueueDef());
			defsTable.addItem(new Object[] {"", ""}, tableId);
			defsTable.requestRepaint();
			defsTable.addListener(this);
		} else if (event.getComponent() == removeDef) {
			if (defsTable.getValue() != null) {
				defsTable.removeListener(this);
				queueDefs.remove(((Integer)defsTable.getValue()).intValue());
				defsTable.removeItem(defsTable.getValue());
				defsTable.requestRepaint();
				prevSelected = null;
				rightsPanel.removeAllComponents();
				defsTable.addListener(this);
			}
		}
	}
	
	public void valueChange(ValueChangeEvent event) {
    	if (prevSelected != null) {
    		QueueDef qd = queueDefs.get(prevSelected);
    		qd.clearRights();
    		for (Iterator i = rightsTable.getItemIds().iterator(); i.hasNext();) {
    		    int iid = (Integer) i.next();
    		    Item item = rightsTable.getItem(iid);
    		    String roleName = (String)item.getItemProperty(ROLE_NAME).getValue();
    		    Boolean browseAllowed = (Boolean)item.getItemProperty(BROWSE_ALLOWED).getValue();
    		    qd.addRight(iid, new QueueRight(roleName, browseAllowed));
    		}
    	}
    	
    	rightsPanel.removeAllComponents();
    	
    	if (defsTable.getValue() != null) {
    		rightsTable = new Table("Queue rights");
    		rightsTable.addContainerProperty(ROLE_NAME, String.class,  null);
    		rightsTable.addContainerProperty(BROWSE_ALLOWED,  Boolean.class,  null);
    		SortedMap<Integer,QueueRight> rightsList = queueDefs.get((Integer)defsTable.getValue()).getRights();
    		for (Integer i : rightsList.keySet()) {
    			rightsTable.addItem(new Object[] {rightsList.get(i).getRoleName(), rightsList.get(i).isBrowseAllowed()}, i);
    		}
		
    		rightsTable.setSelectable(true);
    		rightsTable.setImmediate(true);
    		rightsTable.setEditable(true);
    		rightsPanel.addComponent(rightsTable);
    	
	    	createRightsButtons();
	 	    
	 	    HorizontalLayout rightsButtons = new HorizontalLayout();
		 	    rightsButtons.addComponent(addRight);
	 	    rightsButtons.addComponent(removeRight);
	 	    rightsPanel.addComponent(rightsButtons);
    	}
    	
    	prevSelected = (Integer)defsTable.getValue();
    }
	
	
}
