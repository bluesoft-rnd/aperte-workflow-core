package pl.net.bluesoft.rnd.processtool.ui.activity;


import org.aperteworkflow.ui.view.IViewController;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataSimpleViewComponent;
import pl.net.bluesoft.rnd.processtool.ui.process.ToolbarProcessDataViewComponent;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * Simple panel for Activity Application. It displays only {@link ToolbarProcessDataViewComponent}
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ActivitySimplePanel extends AbstractActivityView implements ClickListener
{
	private static final long serialVersionUID = 2581408315265893361L;
	
	private static final String BLANK_VIEW_LAYOUT_STYLE = "blank-view-layout";
	private static final String CLOSE_BUTTON_STYLE = "standalone-close-button";
	
	private IViewController viewController;
	private ProcessDataSimpleViewComponent processDataView;
	
	private AbstractLayout blankView;
	private Button closeApplicationButton;
	private Label processEndedLayout;
	
	public ActivitySimplePanel(Application application, I18NSource i18nSource,ProcessToolBpmSession bpmSession) 
	{
		super(application, i18nSource, bpmSession);
		
		this.viewController = new OneProcessViewController();
		this.processDataView = new ProcessDataSimpleViewComponent(application, i18nSource, viewController);
		
		setWidth(100, Sizeable.UNITS_PERCENTAGE);
		initLayout();
	}

	@Override
	protected void displayProcessDataInPane(BpmTask task, ProcessToolBpmSession bpmSession, boolean forward) 
	{
		processDataView.attachProcessDataPane(task, bpmSession);
		
	}

	@Override   
	protected void initLayout() 
	{
		initBlankView();
		addComponent(processDataView);
		
	}
	
	private void initBlankView()
	{
		blankView = new CssLayout();
		blankView.addStyleName(BLANK_VIEW_LAYOUT_STYLE);
		
		processEndedLayout = new Label(getI18NSource().getMessage("standalone.blankview.label"));
		blankView.addComponent(processEndedLayout);
		
		closeApplicationButton = new Button(getI18NSource().getMessage("standalone.blankview.close.button"));
		closeApplicationButton.addListener((ClickListener)this);
		closeApplicationButton.addStyleName(CLOSE_BUTTON_STYLE);
		blankView.addComponent(closeApplicationButton);
		
	}
	
	private class OneProcessViewController implements IViewController
	{

		@Override
		public void displayPreviousView() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void displayCurrentView() 
		{
			
		}

		@Override
		public void refreshCurrentView() 
		{
			processDataView.refreshData();
			
		}

		@Override
		public void displayBlankView() 
		{
			ActivitySimplePanel.this.removeAllComponents();
			ActivitySimplePanel.this.addComponent(blankView);
			
		}
		
	}

	@Override
	public void buttonClick(ClickEvent event) 
	{
		if(event.getButton().equals(closeApplicationButton))
		{
			String javaScript = "window.open('', '_self', ''); window.close(); " +
					" ";
			this.getActivityApplication().getMainWindow().executeJavaScript(javaScript);
		}
		
	}

}
