package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "AddNewCommentStep")
public class AddNewCommentStep implements ProcessToolProcessStep
{
    @AutoWiredProperty(required = true)
    private String authorLogin;

    @AutoWiredProperty(required = true)
    private String commentBody;

    @AutoWiredProperty(required = true)
    private String commentDate;

    @AutoWiredProperty
    private String commentType;

    @Autowired
    private IUserSource userSource;

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception
    {
        ProcessInstance processInstance = step.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        String login = StepUtil.extractVariable(authorLogin, ctx, processInstance);
        UserData author = userSource.getUserByLogin(login);

        if(author == null)
            throw new RuntimeException("No user found with login: "+login);

        String body = StepUtil.extractVariable(commentBody, ctx, processInstance);
        String type = StepUtil.extractVariable(commentType, ctx, processInstance);


        ProcessComment comment = new ProcessComment();
        comment.setAuthorLogin(login);
        comment.setAuthorFullName(author.getRealName());
        comment.setBody(body);
        comment.setCreateTime(new Date());
        comment.setProcessInstance(processInstance);
        comment.setCommentType(type);

        processInstance.addComment(comment);

        return STATUS_OK;
    }
}
