package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;

import java.util.Collection;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 08:39
 */
public class SkipSavingDialog extends DialogWindow {
	private Button saveIgnoringErrorsButton;
	private Button cancelButton;
	
	private Map<ProcessToolDataWidget, Collection<String>> validationErrors;

	public SkipSavingDialog(Map<ProcessToolDataWidget, Collection<String>> validationErrors) {
		this.validationErrors = validationErrors;
	}

	@Override
	protected String getTitle() {
		return getMessage("process.action.validation.skip.save");
	}

	@Override
	protected AbstractOrderedLayout createContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(true);
		vl.addComponent(new Label(getMessage("process.action.validation.skip.save.description"), Label.CONTENT_XHTML));
		vl.addComponent(new Label(VaadinUtility.widgetsErrorMessage(i18NSource, validationErrors), Label.CONTENT_XHTML));
		vl.addComponent(new Label(getMessage("process.action.validation.skip.save.continue"), Label.CONTENT_XHTML));
		return vl;
	}

	@Override
	protected Button[] createActionButtons() {
		return new Button[] {
				saveIgnoringErrorsButton = createActionButton(getMessage("process.action.yes")),
				cancelButton = createActionButton(getMessage("process.action.no"))
		};
	}

	public Button getSaveIgnoringErrorsButton() {
		return saveIgnoringErrorsButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}
}
