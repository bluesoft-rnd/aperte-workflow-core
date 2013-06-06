package pl.net.bluesoft.rnd.processtool.ui.process;

import org.aperteworkflow.ui.view.IViewController;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ProcessDataMultiViewComponent extends ProcessDataAbstractViewComponent 
{
	private ProcessMultiViewDataPane pdp;

    public ProcessDataMultiViewComponent(Application application, I18NSource messageSource, IViewController viewController) 
    {
    	super(application, messageSource, viewController);
        init();
    }
    
    private void init() {
        setWidth(100, UNITS_PERCENTAGE);
        setSpacing(true);
        setMargin(true);
    }
	
	protected void initLayout()
	{
        addComponent(pdp);
        setExpandRatio(pdp, 1.0f);
	}

    public void attachProcessDataPane(BpmTask task, ProcessToolBpmSession bpmSession) {
        removeAllComponents();
        setTitleLabel(new Label());
        setBpmSession(bpmSession);
        this.pdp = new ProcessMultiViewDataPane(getActivityApplication(), getBpmSession(), getMessageSource(), task, new ProcessDataDisplayContext() {
            @Override
            public void hide() {
                setShowExitWarning(getActivityApplication(), false);
                VaadinUtility.unregisterClosingWarning(getActivityApplication().getMainWindow());
                getViewController().displayPreviousView();
            }

            @Override
            public void setCaption(String newCaption) {
                getTitleLabel().setValue(newCaption);
            }
        });

        initLayout();

        setShowExitWarning(getActivityApplication(), true);
        VaadinUtility.registerClosingWarning(getActivityApplication().getMainWindow(), getMessageSource().getMessage("page.reload"));

        focus();
    }

    @Override
    public void refreshData() {
    }

    protected void setShowExitWarning(Application application, boolean show) {
        if (application instanceof GenericVaadinPortlet2BpmApplication) {
            ((GenericVaadinPortlet2BpmApplication) application).setShowExitWarning(show);
        }
    }

	public ProcessMultiViewDataPane getProcessDataPane() {
		return pdp;
	}
}
