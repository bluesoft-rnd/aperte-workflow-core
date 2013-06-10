package pl.net.bluesoft.rnd.processtool.ui.process;

import org.aperteworkflow.ui.view.IViewController;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public abstract class ProcessDataAbstractViewComponent extends VerticalLayout implements Refreshable 
{ 
    private IViewController viewController;

	private I18NSource messageSource;

	private Label titleLabel;

    private Application application;
	private ProcessToolBpmSession bpmSession;
	
	public abstract void attachProcessDataPane(BpmTask task, ProcessToolBpmSession bpmSession);

	public ProcessDataAbstractViewComponent(Application application, I18NSource messageSource, IViewController viewController) {
        this.application = application;
        this.messageSource = messageSource;
        this.viewController = viewController;
	}
	
    public I18NSource getMessageSource() {
		return messageSource;
	}

	public IViewController getViewController() {
		return viewController;
	}

	public Label getTitleLabel() {
		return titleLabel;
	}

	public ProcessToolBpmSession getBpmSession() {
		return bpmSession;
	}

	public void setViewController(IViewController viewController) {
		this.viewController = viewController;
	}

	public void setTitleLabel(Label titleLabel) {
		this.titleLabel = titleLabel;
	}

	public void setBpmSession(ProcessToolBpmSession bpmSession) {
		this.bpmSession = bpmSession;
	}
	
	public Application getActivityApplication() {
		return application;
	}
    
    

}
