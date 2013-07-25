package pl.net.bluesoft.rnd.processtool.ui.queues;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

public class QueueForm extends Form {
	public QueueForm(final I18NSource source, final GenericVaadinPortlet2BpmApplication application) {
		
		setFormFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Item item, Object propertyId, Component uiContext) {
		    	Field field = super.createField(item, propertyId, uiContext);

		    	if ("process".equalsIgnoreCase(propertyId.toString())) {
			        NativeSelect select = new NativeSelect();
			        Collection<ProcessDefinitionConfig> processes = getThreadProcessToolContext().getProcessDefinitionDAO().getActiveConfigurations();
			        BeanItemContainer<ProcessDefinitionConfig> ds = new BeanItemContainer<ProcessDefinitionConfig>(ProcessDefinitionConfig.class);
			        ds.addAll(processes);
			        select.setContainerDataSource(ds);
			        select.setItemCaptionPropertyId("description");
			        select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
			        select.setNullSelectionAllowed(false);
			        select.setRequired(true);
			        select.setPropertyDataSource(item.getItemProperty(propertyId));
			        field = select;
		    	}
		    	else if ("name".equalsIgnoreCase(propertyId.toString())) {
		    		TextField text = new TextField();
		    		text.setNullRepresentation("");
		    		text.setRequired(true);
		    		field = text;
		    	}
		    	else if ("description".equalsIgnoreCase(propertyId.toString())) {
		    		TextField text = new TextField();
		    		text.setNullRepresentation("");
		    		text.setRequired(true);
		    		field = text;
		    	}
		    	else if ("rights".equalsIgnoreCase(propertyId.toString())) {
		    		RightsTable rights = new RightsTable(source, application);
		    		//rights.setHeight("150px");
		    		rights.setRequired(true);
		    		field = rights;
		    	}
		    	
		    	field.setCaption(source.getMessage("queues.add.form." + propertyId));
		    	field.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		    	return field;
            }
        });
		
	}
}
