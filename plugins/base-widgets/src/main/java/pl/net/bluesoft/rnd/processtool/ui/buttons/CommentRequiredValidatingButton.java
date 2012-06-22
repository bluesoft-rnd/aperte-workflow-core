package pl.net.bluesoft.rnd.processtool.ui.buttons;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComments;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.AddCommentDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

import java.util.Date;

/**
 * @author amichalak@bluesoft.net.pl
 */

@AliasName(name = "CommentButton")
public class CommentRequiredValidatingButton extends StandardValidatingButton {    
	protected AddCommentDialog dialog;
	
	@Override
	protected void doShowValidationErrorsOrSave(PerformedActionParams params) {
		showAddCommentDialog(params);
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
        ProcessToolContext ctx = getCurrentContext();
		ProcessComment pc = dialog.getProcessComment();
        pc.setAuthor(ctx.getUserDataDAO().loadOrCreateUserByLogin(loggedUser));
        pc.setAuthorSubstitute(substitutingUser != null ? ctx.getUserDataDAO().loadOrCreateUserByLogin(substitutingUser) : null);
        pc.setCreateTime(new Date());
        pc.setProcessState(task.getTaskName());
        ProcessInstance pi = task.getRootProcessInstance();
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
