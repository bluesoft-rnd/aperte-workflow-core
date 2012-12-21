package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.RichTextArea;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 11:13
 */
public class YesNoCommentDialog extends AddCommentDialog {
	private Button noButton;

	public YesNoCommentDialog(ProcessComment processComment) {
		super(processComment);
	}

	@Override
	protected String getTitle() {
		return getMessage("processdata.comments.yesnocomment.title");
	}

	@Override
	protected String getHelpContents() {
		return getMessage("processdata.comments.yesnocomment.help");
	}

	@Override
	protected String getConfirmButtonCaption() {
		return getMessage("processdata.comments.yesnocomment.yes");
	}

	@Override
	protected String getCancelButtonCaption() {
		return getMessage("processdata.comments.yesnocomment.cancel");
	}

	protected String getDeclineButtonCaption() {
		return getMessage("processdata.comments.yesnocomment.no");
	}

	@Override
	protected void setupCommentField(Object propertyId, RichTextArea rta) {
		rta.setCaption(getMessage("processdata.comments.yesnocomment.form." + propertyId));
		rta.setRequiredError(getMessage("processdata.comments.yesnocomment.form." + propertyId + ".required"));
        rta.focus();
	}

	@Override
	protected Button[] createActionButtons() {
		return new Button[] {
				addButton = createConfirmButton(),
				noButton = createActionButton(getDeclineButtonCaption()),
				cancelButton = createActionButton(getCancelButtonCaption())
		};
	}

	public Button getYesButton() {
		return addButton;
	}

	public Button getNoButton() {
		return noButton;
	}
}
