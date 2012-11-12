package pl.net.bluesoft.rnd.processtool.ui.buttons;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComments;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.AddCommentDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Date;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * @author amichalak@bluesoft.net.pl
 */

@AliasName(name = "CommentButton")
public class CommentRequiredValidatingButton extends StandardValidatingButton {
	@AutoWiredProperty
	private String askForCommentKey;

	protected AddCommentDialog dialog;

	private boolean skipAddingComment = false;
	
	@Override
	protected void doShowValidationErrorsOrSave(PerformedActionParams params) {
		if (hasText(askForCommentKey)) {
			task = params.getSupport().refreshTask(bpmSession, task);
			if ("true".equals(task.getProcessInstance().getSimpleAttributeValue(askForCommentKey, "false"))) {
				showAddCommentDialog(params);
			}
			else {
				skipAddingComment = true;
				super.doShowValidationErrorsOrSave(params);
			}
		}
		else {
			showAddCommentDialog(params);
		}
	}

	protected void showAddCommentDialog(final PerformedActionParams params) {
		dialog = showDialog(createAddCommentDialog(new ProcessComment()));
		dialog.addListener(new AddCommentDialog.AddCommentListener() {
			@Override
			public void onCommentAdded() {
				handleAddComment(params);
			}
		});
	}

	protected boolean canSaveComment() {
		return true;
	}

	protected void handleAddComment(PerformedActionParams params) {
		task = params.getSupport().refreshTask(bpmSession, task);
		CommentRequiredValidatingButton.super.doShowValidationErrorsOrSave(params);
	}

	protected AddCommentDialog createAddCommentDialog(ProcessComment processComment) {
		return new AddCommentDialog(processComment);
	}

	@Override
    public void saveData(BpmTask task) {
        super.saveData(task);
		if (canSaveComment()) {
        	saveComment();
		}
    }

    private void saveComment() {
		if (skipAddingComment) {
			return;
		}
        ProcessToolContext ctx = getCurrentContext();
		ProcessComment pc = dialog.getProcessComment();
        pc.setAuthor(ctx.getUserDataDAO().loadOrCreateUserByLogin(loggedUser));
        pc.setAuthorSubstitute(substitutingUser != null ? ctx.getUserDataDAO().loadOrCreateUserByLogin(substitutingUser) : null);
        pc.setCreateTime(new Date());
        pc.setProcessState(task.getTaskName());
        ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();
        ProcessComments comments = pi.findAttributeByClass(ProcessComments.class);
        if (comments == null) {
            comments = new ProcessComments();
            comments.setProcessInstance(pi);
            comments.setKey(ProcessComments.class.getName());
            pi.getProcessAttributes().add(comments);
        }
        comments.getComments().add(pc);
        pc.setComments(comments);
        pi.setSimpleAttribute("commentAdded", "true");
    }
}
