package pl.net.bluesoft.rnd.processtool.ui.buttons;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSessionHelper;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.AddCommentDialog;
import pl.net.bluesoft.rnd.processtool.ui.buttons.dialog.AssignUserTaskDialog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;

/**
 * @author amichalak@bluesoft.net.pl
 */
@AliasName(name = "AssignButton")
public class AssignUserTaskButton extends CommentRequiredValidatingButton {
	@AutoWiredProperty
    protected String roleName = "";

	private AssignUserTaskDialog dialog;

    @Override
    protected void invokeBpmTransition() {   
    	
    	if(dialog==null){
    		super.invokeBpmTransition();
    	}
    	else{
        ProcessToolContext ctx = getCurrentContext();
		String login = dialog.getAssigneeBean().getAssignee().getLogin();
		ProcessToolBpmSessionHelper.assignTaskToUser(bpmSession, ctx, task.getInternalTaskId(), login);
        callback.getWidgetContextSupport().updateTask(task);
        logger.info("Assigneed user: " + login);
    	}
    }

	@Override
	protected AddCommentDialog createAddCommentDialog(ProcessComment processComment) {
		PropertyAutoWiring.autowire(this, getAutowiredProperties());
		return dialog = new AssignUserTaskDialog(new ProcessComment(), roleName);
	} 
	  
	
}
