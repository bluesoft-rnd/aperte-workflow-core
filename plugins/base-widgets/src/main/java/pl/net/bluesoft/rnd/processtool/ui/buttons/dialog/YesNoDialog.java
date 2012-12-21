package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 10:41
 */
public class YesNoDialog extends DialogWindow {
	private Button yesButton;
	private Button noButton;
	private Button cancelButton;
	private String content = "processdata.comments.yesno.help";
	private String title = "processdata.comments.yesno.title";
	
	public YesNoDialog(String title, String content) {
		this.title = title;
		this.content = content;
	}
	
	public YesNoDialog() {
	}
	
	@Override
	protected String getTitle() {
		return getMessage(title);
	}

	@Override
	protected AbstractOrderedLayout createContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(true);
		vl.setWidth(600, Sizeable.UNITS_PIXELS);
		vl.addComponent(new Label(getMessage(content), Label.CONTENT_XHTML));
		return vl;
	}

	@Override
	protected Button[] createActionButtons() {
		return new Button[] {
				yesButton = createActionButton(getMessage("processdata.comments.yesno.yes")),
				noButton = createActionButton(getMessage("processdata.comments.yesno.no")),
				cancelButton = createActionButton(getMessage("processdata.comments.yesno.cancel"))
		};
	}

	public Button getYesButton() {
		return yesButton;
	}

	public Button getNoButton() {
		return noButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}
}
