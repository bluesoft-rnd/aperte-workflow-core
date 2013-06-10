package pl.net.bluesoft.rnd.processtool.ui.process;

import java.io.Serializable;
import java.util.List;

import org.aperteworkflow.ui.view.IViewController;
import org.aperteworkflow.ui.view.ViewEvent;
import org.aperteworkflow.ui.view.ViewEvent.Type;
import org.aperteworkflow.util.vaadin.EventHandler;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * {@link ProcessDataMultiViewComponent} with toolabar
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ToolbarProcessDataViewComponent extends ProcessDataMultiViewComponent
{
	
	public ToolbarProcessDataViewComponent(Application application,I18NSource messageSource, IViewController viewController) 
	{
		super(application, messageSource, viewController);
	}


	@Override
	protected void initLayout() 
	{
        addComponent(buildToolbar(getProcessDataPane().getToolbarButtons(), getProcessDataPane().canSaveProcessData()));
        
		super.initLayout();
	}


    private HorizontalLayout buildToolbar(List<Component> otherButtons, final boolean canSaveProcessData) {
        Button backButton = VaadinUtility.link(getMessageSource().getMessage("activity.back"), VaadinUtility.imageResource(getActivityApplication(), "go_previous.png"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (canSaveProcessData) {
                            VaadinUtility.displayConfirmationWindow(
                            		getActivityApplication(),
									getMessageSource(),
									getMessageSource().getMessage("activity.close.process.confirmation.title"),
									getMessageSource().getMessage("activity.close.process.confirmation.question"),
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

        toolbar.addComponent(getTitleLabel());
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

        toolbar.setComponentAlignment(getTitleLabel(), Alignment.MIDDLE_LEFT);
        toolbar.recalculateExpandRatios();

        return toolbar;
    }
    
	private void saveAndCloseAction(boolean save) {
		if (save) {
			if (!getProcessDataPane().saveProcessDataButtonAction()) {
				return;
			}
		}
		setShowExitWarning(getActivityApplication(), false);
		VaadinUtility.unregisterClosingWarning(getActivityApplication().getMainWindow());
		getBpmSession().getEventBusManager().post(new ViewEvent(Type.ACTION_COMPLETE));
		getViewController().displayPreviousView();
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
