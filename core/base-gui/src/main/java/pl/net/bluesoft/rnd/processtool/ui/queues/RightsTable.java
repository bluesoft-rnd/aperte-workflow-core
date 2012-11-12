package pl.net.bluesoft.rnd.processtool.ui.queues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.addon.customfield.CustomField;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.util.lang.Strings;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class RightsTable extends CustomField {
	
	private Table table = new Table();
	private BeanItemContainer<ProcessQueueRight> dataSource = new BeanItemContainer<ProcessQueueRight>(ProcessQueueRight.class);
	private final I18NSource source;
	private GenericVaadinPortlet2BpmApplication application;
	private Logger logger = Logger.getLogger(RightsTable.class.getName());
	private Window addWindow;
	private Window addNewWindow;
	
	public RightsTable(final I18NSource source, final GenericVaadinPortlet2BpmApplication application) {
		this.source = source;
		this.application = application;
		table.setContainerDataSource(dataSource);
		table.setTableFieldFactory(new DefaultFieldFactory() {
			
			@Override
			public Field createField(Container container, Object itemId,
					Object propertyId, Component uiContext) {

                Field field;
                
                if ("roleName".equals(propertyId)) {
                	
                	field = new TextField();
                	field.setReadOnly(true);
                    field.setWidth("80%");
                }
                else if ("browseAllowed".equals(propertyId)) {
                	field = new CheckBox();
                    field.setWidth("20%");
                }
                else
                	field = super.createField(container, itemId, propertyId, uiContext);
                
                return field;
	            
			}
		});
		
		table.setVisibleColumns(new Object[] {"roleName", "browseAllowed"});
		table.setColumnHeaders(new String[] {source.getMessage("queues.add.form.rights.roleName"), source.getMessage("queues.add.form.rights.browseAllowed")});
		table.setEditable(!isReadOnly());
		table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		table.setHeight(100, Sizeable.UNITS_PIXELS);
		table.setSelectable(true);
		
		Button addButton = new Button(source.getMessage("queues.add.form.rights.add"));
		Button addNewButton = new Button(source.getMessage("queues.add.form.rights.addnew"));
		Button removeButton = new Button(source.getMessage("queues.add.form.rights.remove"));
		
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.addComponent(table);
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.addComponent(addButton);
		hl.addComponent(addNewButton);
		hl.addComponent(removeButton);
		vl.addComponent(hl);
		setCompositionRoot(vl);
		
		addButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				VerticalLayout pane = getRoleFinder();
				pane.setWidth("400px");
				addWindow = getWindow(source.getMessage("queues.add.form.rights.add.title"), pane);
				getApplication().getMainWindow().addWindow(addWindow);
			}
		});
		
		addNewButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				VerticalLayout pane = getNewRoleFinder();
				pane.setWidth("400px");
				addNewWindow = getWindow(source.getMessage("queues.add.form.rights.add.title"), pane);
				getApplication().getMainWindow().addWindow(addNewWindow);
			}
		});
		
		removeButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				Object o = table.getValue();
				if (o == null)
					return;
				
				table.removeItem(o);
			}
		});
	}

	@Override
	public Class<?> getType() {
		return ArrayList.class;
	}
	
    @Override
    public Object getValue() {
        ArrayList<ProcessQueueRight> beans = new ArrayList<ProcessQueueRight>();
        for (Object itemId: dataSource.getItemIds())
            beans.add(dataSource.getItem(itemId).getBean());
        return beans;
    }
	
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof Collection<?>) {
            @SuppressWarnings("unchecked")
            Collection<ProcessQueueRight> beans = (ArrayList<ProcessQueueRight>) value;
            dataSource.removeAllItems();
            dataSource.addAll(beans);
            table.setPageLength(beans.size());
        } else
            throw new ConversionException("Invalid type");

        super.setPropertyDataSource(newDataSource);
    }
    
    private VerticalLayout getRoleFinder() {
    	
    	VerticalLayout vl = new VerticalLayout();
    	vl.setMargin(true);
    	vl.setSpacing(true);
    	
    	List<Role> roles = new ArrayList<Role>();
		try {
			roles = RoleLocalServiceUtil.getRoles(application.getUser().getCompanyId());
		} catch (SystemException e) {
			logger.log(Level.SEVERE, "Error getting liferay roles", e);
		}
    	BeanItemContainer<Role> ds = new BeanItemContainer<Role>(Role.class);
    	ds.addAll(roles);
    	
    	final Select select = new Select(source.getMessage("queues.add.form.rights.new.combo"));
		select.setContainerDataSource(ds);
		select.setItemCaptionPropertyId("name");
		select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
		select.setNullSelectionAllowed(false);
		select.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		
		Button addButton = new Button(source.getMessage("queues.add.form.rights.new.add"));
		addButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if (select.getValue() == null) {
					return;
				}
				
				Role r = (Role)select.getValue();
				
				ProcessQueueRight bean = new ProcessQueueRight();
				bean.setBrowseAllowed(true);
				bean.setRoleName(r.getName());
				dataSource.addBean(bean);
				
				getApplication().getMainWindow().removeWindow(addWindow);
			}
		});
		
    	vl.addComponent(select);
    	vl.addComponent(addButton);
    	
    	return vl;
    }
    
    private VerticalLayout getNewRoleFinder() {
    	
    	VerticalLayout vl = new VerticalLayout();
    	vl.setMargin(true);
    	vl.setSpacing(true);
    	
    	final TextField role = new TextField();
    	role.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		
		Button addButton = new Button(source.getMessage("queues.add.form.rights.new.add"));
		addButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				
				Object o = role.getValue();
				if (o == null)
					return;
				
				String r = (String)o;
				if (!Strings.hasText(r))
					return;
				
				ProcessQueueRight bean = new ProcessQueueRight();
				bean.setBrowseAllowed(true);
				bean.setRoleName(r);
				dataSource.addBean(bean);
				
				getApplication().getMainWindow().removeWindow(addNewWindow);
			}
		});
		
    	vl.addComponent(role);
    	vl.addComponent(addButton);
    	
    	return vl;
    }

    private Window getWindow(String caption, Layout lay) {
		Window window = new Window(caption, lay);
		window.setClosable(true);
		window.setModal(true);
		window.setSizeUndefined();
		window.setResizable(false);
		return window;
    }

}
