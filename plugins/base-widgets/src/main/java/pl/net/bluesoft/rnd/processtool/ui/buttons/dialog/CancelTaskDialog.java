package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 09:15
 */
public class CancelTaskDialog extends DialogWindow {
	private static final String CAPTION_PREFIX = "cancel.task.button.";

	private Button cancelTaskButton;
	private Button cancelButton;

	@Override
	protected String getTitle() {
		return getMessage(CAPTION_PREFIX + "header");
	}

	@Override
	protected AbstractOrderedLayout createContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(true);
		vl.setWidth(600, Sizeable.UNITS_PIXELS);
		vl.addComponent(new Label(getMessage(CAPTION_PREFIX + "text"), Label.CONTENT_XHTML));
		return vl;
	}

	@Override
	protected Button[] createActionButtons() {
		return new Button[] {
				cancelTaskButton = createActionButton(getMessage(CAPTION_PREFIX + "yes")),
				cancelButton = createActionButton(getMessage(CAPTION_PREFIX + "no"))
		};
	}

	public Button getCancelTaskButton() {
		return cancelTaskButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}
}
