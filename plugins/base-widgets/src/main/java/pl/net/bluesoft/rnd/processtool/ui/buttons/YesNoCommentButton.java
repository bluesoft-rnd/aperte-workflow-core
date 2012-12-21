package pl.net.bluesoft.rnd.processtool.ui.buttons;

import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.AddCommentDialog;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.YesNoCommentDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

/**
 * @author amichalak@bluesoft.net.pl
 */
@AliasName(name = "YesNoCommentButton")
public class YesNoCommentButton extends CommentRequiredValidatingButton {
	@Override
	protected AddCommentDialog createAddCommentDialog(ProcessComment processComment) {
		return new YesNoCommentDialog(processComment);
	}
}
