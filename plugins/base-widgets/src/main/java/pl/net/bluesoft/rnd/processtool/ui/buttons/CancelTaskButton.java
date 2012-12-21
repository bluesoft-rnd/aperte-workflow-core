package pl.net.bluesoft.rnd.processtool.ui.buttons;

import com.vaadin.ui.Button;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.CancelTaskDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Collection;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-01-10
 * Time: 11:23:49
 */
@AliasName(name = "CancelTaskButton")
public class CancelTaskButton extends CommentRequiredValidatingButton {
	@AutoWiredProperty
	private boolean mustAddComment = false;

	@Override
    protected void showValidationErrorsOrSave(final WidgetContextSupport support, final Map<ProcessToolDataWidget, Collection<String>> validationErrors) {
		showCancelTaskDialog(new PerformedActionParams(support, validationErrors, false));
    }

	private void showCancelTaskDialog(final PerformedActionParams params) {
		CancelTaskDialog dialog = showDialog(new CancelTaskDialog());
		dialog.getCancelTaskButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				handleCancelTaskButtonClick(params);
			}
		});
	}

	protected void handleCancelTaskButtonClick(PerformedActionParams params) {
		if (mustAddComment) {
			showAddCommentDialog(params);		
		}
		else {
			finalizeAction(params.isSaveData());
		}
	}

	@Override
	protected boolean canSaveComment() {
		return mustAddComment;
	}
}
