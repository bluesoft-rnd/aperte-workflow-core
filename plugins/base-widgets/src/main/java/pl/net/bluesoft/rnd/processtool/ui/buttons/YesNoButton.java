package pl.net.bluesoft.rnd.processtool.ui.buttons;

import com.vaadin.ui.Button;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.YesNoDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

import java.util.Collection;
import java.util.Map;

/**
 * @author marcin
 */
@AliasName(name = "YesNoButton")
public class YesNoButton extends StandardValidatingButton {
    @Override
    protected void showValidationErrorsOrSave(final WidgetContextSupport support, final Map<ProcessToolDataWidget, Collection<String>> validationErrors) {
        if (validationErrors.isEmpty()) {
			YesNoDialog dialog = showDialog(new YesNoDialog());
			dialog.getYesButton().addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					YesNoButton.super.showValidationErrorsOrSave(support, validationErrors);
				}
			});
        }
        else {
            super.showValidationErrorsOrSave(support, validationErrors);
        }
    }
}
