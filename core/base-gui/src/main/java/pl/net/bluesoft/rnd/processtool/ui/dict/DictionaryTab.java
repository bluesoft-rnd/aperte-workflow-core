package pl.net.bluesoft.rnd.processtool.ui.dict;

import static org.aperteworkflow.util.vaadin.VaadinUtility.validationNotification;

import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.DictionaryModelView;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public abstract class DictionaryTab extends VerticalLayout implements ClickListener
{
	protected HorizontalLayout headerLayout;
	protected DictionaryItemTable dictionaryItemTable;
	protected DictionaryItemValueTable dictionaryItemValuesTable;
    
	protected DictionariesMainPane mainPanel;
    
	private DictionaryModelView modelView;
    
    protected Button addValueButton;

	
	public DictionaryTab(DictionariesMainPane mainPanel, DictionaryModelView modelView) 
	{
		this.mainPanel = mainPanel;
		this.modelView = modelView;
		
		init();
	}
	
	protected void refreshData()
	{
		getModelView().refreshData();
    	dictionaryItemTable.sort();
	}
	
	protected void init()
	{
		headerLayout = new HorizontalLayout();
	
		
		dictionaryItemTable = new DictionaryItemTable(getModelView().getBeanItemContainerDictionaryItems(), mainPanel.getI18NSource(), mainPanel.getVaadinApplication());
		dictionaryItemTable.addActionRequestListener(mainPanel);
		dictionaryItemTable.addEntryValidator(mainPanel);
		
		dictionaryItemValuesTable = new DictionaryItemValueTable(getModelView().getBeanItemContainerDictionaryItemsValues(), mainPanel.getI18NSource(), mainPanel.getVaadinApplication());
		dictionaryItemValuesTable.addActionRequestListener(mainPanel);
		dictionaryItemValuesTable.setVisible(false);
	
		
		addValueButton = VaadinUtility.addIcon(mainPanel.getApplication());
		addValueButton.setCaption(getMessage("dict.add.value"));
		addValueButton.setDescription(getMessage("dict.add.value"));
		addValueButton.setVisible(false);
		addValueButton.addListener((ClickListener)this);
		
		addComponent(headerLayout);
		addComponent(dictionaryItemTable);
		addComponent(addValueButton);
		addComponent(dictionaryItemValuesTable);
	}
	
	public void loadData()
	{
		
	}

    
    protected String getMessage(String key, Object ... parameters)
    {
    	return mainPanel.getMessage(key, parameters);
    }
    
	public void updateItem(ProcessDBDictionaryItem item) {

		
	}
	
    
    public void editItem(ProcessDBDictionaryItem item)
    {
    	modelView.addDictionaryItem(item);
    	
    	modelView.setSelectedDictionaryItem(item);
    	BeanItem<ProcessDBDictionaryItem> bean = modelView.getBeanItemContainerDictionaryItems().getItem(item);
    	dictionaryItemValuesTable.setPropertyDataSource(bean.getItemProperty("values"));
    
    	
    	dictionaryItemTable.setEditable(true);
    	dictionaryItemTable.setEditableItem(item);
    	/* Refresh */
    	dictionaryItemValuesTable.setVisible(true);
    	dictionaryItemValuesTable.setEditable(true);
    	dictionaryItemValuesTable.sort();
    	addValueButton.setVisible(true);
    	
    	dictionaryItemTable.setContainerDataSource(modelView.getBeanItemContainerDictionaryItems());
    }
    
    public void dicardChanges()
    {
		dictionaryItemTable.discard();
		dictionaryItemValuesTable.discard();
		
		disableEdition();
    }
    
    public void commitChanges()
    {
    	try
    	{
    		dictionaryItemTable.commit();
    		dictionaryItemValuesTable.commit();
    		
    		disableEdition();
    	}
    	catch(InvalidValueException ex)
    	{
    		validationNotification(mainPanel.getApplication(), mainPanel.getI18NSource(), getMessage("validate.dictentry.exists"));
    	}

    }
    
    private void disableEdition()
    {
    	dictionaryItemTable.setEditable(false);
    	dictionaryItemTable.setEditableItem(null);
    	
    	dictionaryItemValuesTable.setEditable(false);
    	
    	dictionaryItemValuesTable.setVisible(false);
    	addValueButton.setVisible(false);
    	
    	getModelView().refreshData();
    	
    	dictionaryItemTable.sort();
    }
  
    
	@Override
	public void buttonClick(ClickEvent event)  
	{
		if(event.getButton().equals(addValueButton))
		{
			ProcessDBDictionaryItem item = dictionaryItemTable.getEditableItem();
			if(item == null)
				throw new RuntimeException("Error, there is no item selected");
			
			ProcessDBDictionaryItemValue newValue = new ProcessDBDictionaryItemValue();
			newValue.setItem(item);
			newValue.setValue("");
			item.getValues().add(newValue);
			
			modelView.setSelectedDictionaryItem(item);
			modelView.addDictionaryItemValue(newValue);
		}
		
	}

	protected DictionaryModelView getModelView() {
		return modelView;
	}

	protected void setModelView(DictionaryModelView modelView) {
		this.modelView = modelView;
	}
}
