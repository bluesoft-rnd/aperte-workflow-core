package pl.net.bluesoft.rnd.processtool.ui.process;

import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import org.aperteworkflow.ui.view.ViewController;
import org.aperteworkflow.ui.view.ViewEvent;
import org.aperteworkflow.ui.view.ViewEvent.Type;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.EventHandler;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;

import java.io.Serializable;
import java.util.List;

public class ProcessDataViewComponent extends VerticalLayout implements Refreshable {
    private ViewController viewController;
    private I18NSource messageSource;

    private Label titleLabel;

    private Application application;
	private ProcessToolBpmSession bpmSession;

	private ProcessDataPane pdp;

    public ProcessDataViewComponent(Application application, I18NSource messageSource, ViewController viewController) {
        this.application = application;
        this.messageSource = messageSource;
        this.viewController = viewController;
        init();
    }

    private void init() {
        setWidth(100, UNITS_PERCENTAGE);
        setSpacing(true);
        setMargin(true);
    }

    private HorizontalLayout buildToolbar(List<Component> otherButtons, final boolean canSaveProcessData) {
        Button backButton = VaadinUtility.link(messageSource.getMessage("activity.back"), VaadinUtility.imageResource(application, "go_previous.png"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (canSaveProcessData) {
                            VaadinUtility.displayConfirmationWindow(
									getApplication(),
									messageSource,
									messageSource.getMessage("activity.close.process.confirmation.title"),
                                    messageSource.getMessage("activity.close.process.confirmation.question"),
									new String[] {
											"activity.close.process.confirmation.ok",
											"activity.close.process.confirmation.save",
											"activity.close.process.confirmation.cancel"
									},
									new EventHandler[] {
											saveEventHandler,
											saveAndCloseEventHandler,
											null,
									},
									null);
                        }
                        else {
                            saveEventHandler.onEvent();
                        }
                    }

                    EventHandler saveEventHandler = new SaveEventHandler();
					EventHandler saveAndCloseEventHandler = new SaveAndCloseEventHandler();
                });

        AligningHorizontalLayout toolbar = new AligningHorizontalLayout(Alignment.MIDDLE_RIGHT);
        toolbar.setWidth(100, UNITS_PERCENTAGE);
        toolbar.setMargin(false);
        toolbar.setSpacing(false);

        toolbar.addComponent(titleLabel);
        for (Component comp : otherButtons) {
            if (comp instanceof Button) {
                Button button = (Button) comp;
                if (button.getIcon() != null && button.getWidth() > 0 && button.getWidthUnits() == UNITS_PIXELS) {
                    button.setWidth(button.getWidth() + 20, UNITS_PIXELS);
                }
            }
            toolbar.addComponent(comp);
        }
        toolbar.addComponent(backButton);

        toolbar.setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT);
        toolbar.recalculateExpandRatios();

        return toolbar;
    }

	private void saveAndCloseAction(boolean save) {
		if (save) {
			if (!pdp.saveProcessDataButtonAction()) {
				return;
			}
		}
		setShowExitWarning(application, false);
		VaadinUtility.unregisterClosingWarning(application.getMainWindow());
		bpmSession.getEventBusManager().post(new ViewEvent(Type.ACTION_COMPLETE));
		viewController.displayPreviousView();
	}

    public void attachProcessDataPane(BpmTask task, ProcessToolBpmSession bpmSession) {
        removeAllComponents();
        titleLabel = new Label();
        this.bpmSession = bpmSession;
        this.pdp = new ProcessDataPane(application, bpmSession, messageSource, task, new ProcessDataDisplayContext() {
            @Override
            public void hide() {
                setShowExitWarning(application, false);
                VaadinUtility.unregisterClosingWarning(application.getMainWindow());
                viewController.displayPreviousView();
            }

            @Override
            public void setCaption(String newCaption) {
                titleLabel.setValue(newCaption);
            }
        });

        addComponent(buildToolbar(pdp.getToolbarButtons(), pdp.canSaveProcessData()));
        addComponent(pdp);
        setExpandRatio(pdp, 1.0f);

        setShowExitWarning(application, true);
        VaadinUtility.registerClosingWarning(application.getMainWindow(), messageSource.getMessage("page.reload"));

        focus();
    }

    @Override
    public void refreshData() {
    }

    private void setShowExitWarning(Application application, boolean show) {
        if (application instanceof GenericVaadinPortlet2BpmApplication) {
            ((GenericVaadinPortlet2BpmApplication) application).setShowExitWarning(show);
        }
    }

	public ProcessDataPane getProcessDataPane() {
		return pdp;
	}

	private class SaveEventHandler implements EventHandler, Serializable {
		@Override
		public void onEvent() {
			saveAndCloseAction(false);
		}
	}

	private class SaveAndCloseEventHandler implements EventHandler, Serializable {
		@Override
		public void onEvent() {
			saveAndCloseAction(true);
		}
	}
}
