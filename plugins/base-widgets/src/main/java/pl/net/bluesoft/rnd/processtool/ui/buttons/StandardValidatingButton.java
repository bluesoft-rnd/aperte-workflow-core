package pl.net.bluesoft.rnd.processtool.ui.buttons;

import com.vaadin.ui.Button;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.DialogWindow;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.SkipSavingDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 */

@AliasName(name = "Default")
public class StandardValidatingButton extends BaseProcessToolVaadinActionButton {
    protected Logger logger = Logger.getLogger(StandardValidatingButton.class.getName());
    private ProcessStateAction psa;

    protected static class PerformedActionParams {
		private WidgetContextSupport support;
		private Map<ProcessToolDataWidget, Collection<String>> validationErrors;
		private boolean saveData;

		public PerformedActionParams(WidgetContextSupport support, Map<ProcessToolDataWidget, Collection<String>> validationErrors, boolean saveData) {
			this.support = support;
			this.validationErrors = validationErrors;
			this.saveData = saveData;
		}

		public WidgetContextSupport getSupport() {
			return support;
		}

		public Map<ProcessToolDataWidget, Collection<String>> getValidationErrors() {
			return validationErrors;
		}

		public boolean isSaveData() {
			return saveData;
		}
	}

    @Override
    protected void performAction(WidgetContextSupport support) {
        showValidationErrorsOrSave(support, support.getWidgetsErrors(task, false));
    }

    protected void showValidationErrorsOrSave(WidgetContextSupport support, Map<ProcessToolDataWidget, Collection<String>> validationErrors) {
        if (validationErrors.isEmpty()) {
			doShowValidationErrorsOrSave(new PerformedActionParams(support, validationErrors, true));
        }
        else if (skipSaving) {
			showSkipSavingDialog(new PerformedActionParams(support, validationErrors, false));
        }
        else {
			support.displayValidationErrors(validationErrors);
        }
    }

	protected void doShowValidationErrorsOrSave(PerformedActionParams params) {
		finalizeAction(params.isSaveData());
	}

	protected void finalizeAction(boolean saveData) {
		if (saveData) {
			invokeSaveTask();
		}
		else {
			invokeSaveTaskWithoutData();
		}
		invokeBpmTransition();
		callback.actionPerformed(definition);
	}

    @Override
    public boolean isVisible(BpmTask task) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEnabled(BpmTask task) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void changeButton(Button button) {

    }

    @Override
    public String getLabel(BpmTask task) {
        return label;
    }

    @Override
    public String getDescription(BpmTask task) {
        return description;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLoggedUser(UserData userData) {
       //nothing
    }

    @Override
    public boolean isAutoHide() {
        return autoHide;
    }

    @Override
    public void setDefinition(ProcessStateAction psa) {
        this.psa = psa;
    }

    @Override
	public void saveData(BpmTask task) {
		super.saveData(task);
		ProcessInstance pi = task.getProcessInstance();
		
		pi.setSimpleAttribute("commentAdded", "false");
	}

	private void showSkipSavingDialog(final PerformedActionParams params) {
		SkipSavingDialog dialog = showDialog(new SkipSavingDialog(params.getValidationErrors()));
		dialog.getSaveIgnoringErrorsButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				handleSaveIgnoringErrorsButtonClick(params);
			}
		});
	}
	
	protected <DialogType extends DialogWindow> DialogType showDialog(DialogType dialog) {
		dialog.setI18NSource(messageSource);
		dialog.buildLayout();
		application.getMainWindow().addWindow(dialog);
		return dialog;
	}

	protected void handleSaveIgnoringErrorsButtonClick(PerformedActionParams params) {
		doShowValidationErrorsOrSave(params);
	}
}
