package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Select;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.ProcessDictionaryModelView;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.AddNewDictionaryItemActionRequest;

public class ProcessDictionaryTab extends DictionaryTab implements ValueChangeListener
{
	private Select selectProcess;
	private Select selectDictionary;
	private Select selectLocale;
	
	private Button addButton;

	public ProcessDictionaryTab(DictionariesMainPane mainPanel, ProcessDictionaryModelView processDictionaryModelView) 
    {
		super(mainPanel, processDictionaryModelView);
	}
    
    
    public void loadData(ProcessToolContext ctx)
    {
    	getProcessModelView().reloadData();

        for(ProcessDefinitionConfig conif: getProcessModelView().getBeanItemContainerConfigs().getItemIds())
			selectProcess.setItemCaption(conif, getMessage(conif.getDescription()));
        
    }
    
    @Override
    protected void init() 
    {
    	super.init();
    	
    	selectProcess = new Select();
    	selectProcess.setImmediate(true);
    	selectProcess.setVisible(true);
    	selectProcess.setNullSelectionAllowed(false);
    
    	
    	selectDictionary = new Select();
    	selectDictionary.setImmediate(true);
    	selectDictionary.setVisible(false);
    	selectDictionary.setNullSelectionAllowed(false);
    	

    	selectLocale = new Select();
    	selectLocale.setImmediate(true);
    	selectLocale.setVisible(false);
    	selectLocale.setNullSelectionAllowed(false);
    	
    	selectProcess.addListener((ValueChangeListener)this);
    	selectDictionary.addListener((ValueChangeListener)this);
    	selectLocale.addListener((ValueChangeListener)this);
    	
        addButton = VaadinUtility.addIcon(mainPanel.getApplication());
        addButton.setCaption(getMessage("dict.addentry"));
        addButton.setVisible(false);
        addButton.addListener((ClickListener)this);
        addButton.setDisableOnClick(true);
    	
    	headerLayout.addComponent(selectProcess);
    	headerLayout.addComponent(selectLocale);
    	headerLayout.addComponent(selectDictionary);
    	headerLayout.addComponent(addButton);
    	
    	dictionaryItemTable.setContainerDataSource(getProcessModelView().getBeanItemContainerDictionaryItems());
    	
    	selectProcess.setContainerDataSource(getProcessModelView().getBeanItemContainerConfigs());
        selectDictionary.setContainerDataSource(getProcessModelView().getBeanItemContainerDictionaries());
        selectLocale.setContainerDataSource(getProcessModelView().getBeanItemContainerLanguageCodes());
    }

	@Override
	public void valueChange(ValueChangeEvent event) 
	{
		/* Process selected, filter dictionaries by this process */
		if(event.getProperty().equals(selectProcess))
		{
			/* Disable dictionary item edition */
			dicardChanges();
			
			ProcessDefinitionConfig selectedConfig = (ProcessDefinitionConfig)selectProcess.getValue();
			
			getProcessModelView().setSelectedProcess(selectedConfig);

			selectLocale.setValue(null);
			selectDictionary.setValue(null);
			selectDictionary.setVisible(false);
			
			selectLocale.setVisible(selectedConfig != null);
		}
		/* Locale selected, show items */
		else if(event.getProperty().equals(selectLocale))
		{		
			/* Disable dictionary item edition */
			dicardChanges();
			
			getProcessModelView().setSelectedLocale((String)selectLocale.getValue());
			
			/* Selecting new locale changes selected dictionary */
			ProcessDBDictionary dictionaryInNewLocale = getProcessModelView().getSelectedDictionary();
			selectDictionary.select(dictionaryInNewLocale);
			
			selectDictionary.setVisible(true);
			
		}
		/* Dictionary selected, filter locals by this dictionary */
		else if(event.getProperty().equals(selectDictionary))
		{
			/* Disable dictionary item edition */
			dicardChanges();
			
			ProcessDBDictionary selectedDictionary = (ProcessDBDictionary)selectDictionary.getValue();
			getProcessModelView().setSelectedDictionary(selectedDictionary);

			dictionaryItemTable.sort();

			addButton.setVisible(selectedDictionary != null);
		}
	}
	
	@Override
	protected void disableEdition() 
	{
		addButton.setEnabled(true);
		
		super.disableEdition();
	}
	
	@Override
	public void buttonClick(ClickEvent event)  
	{
		if(event.getButton().equals(addButton))
		{
			ProcessDBDictionaryItem item = new ProcessDBDictionaryItem();
			ProcessDBDictionary selectedDictionary = (ProcessDBDictionary)selectDictionary.getValue();
			
			if(selectedDictionary == null)
				return;
			
			item.setDictionary(selectedDictionary);
			
			AddNewDictionaryItemActionRequest addNewActionReaquest = new AddNewDictionaryItemActionRequest(item);
			mainPanel.handleActionRequest(addNewActionReaquest);
		}
		else
			super.buttonClick(event);
		
	}
	
    public ProcessDictionaryModelView getProcessModelView() {
		return (ProcessDictionaryModelView)getModelView();
	}
}
