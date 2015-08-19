package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.plugins.util.IWriterTextDecorator;
import pl.net.bluesoft.rnd.processtool.plugins.util.ProcessActionManager;
import pl.net.bluesoft.rnd.processtool.token.TokenWrapper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet which provides logic to get all avaiable user process queues
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class PerformActionServlet extends TokenAuthenticationServlet 
{
	public static final String SERVLET_ADDRESS = "/osgiex/token_access";

	@Override
	protected void processRequest(HttpServletRequest req, ProcessToolContext ctx, TokenWrapper tokenWrapper) 
	{
		PrintWriter out = (PrintWriter)req.getAttribute(PRINT_WRITER);
		I18NSource i18NSource = (I18NSource)req.getAttribute(I18NSOURCE);
		IWriterTextDecorator textDecorator = (IWriterTextDecorator)req.getAttribute(TEXT_DECORATOR);
		
		/* Get current process State */
		ProcessStateConfiguration currentState = tokenWrapper.getTask().getCurrentProcessStateConfiguration();
		
		/* Invalid process state */
		if(currentState == null)
		{
			String exceptionMessage = i18NSource.getMessage("token.servlet.notoken.assositedwithtask",
					tokenWrapper.getTask().getInternalTaskId(), tokenWrapper.getUser().getLogin());
			
			textDecorator.addText(exceptionMessage);
			
			/* Write to user output page */
			out.append(textDecorator.getOutput());
			
			log(exceptionMessage);
			throw new IllegalStateException(exceptionMessage);
		}
		
		ProcessStateAction tokenAction = currentState.getProcessStateActionByName(tokenWrapper.getTokenAction());
		
		/* There is no action correlated to this token, abort */
		if(tokenAction == null)
		{
			String exceptionMessage = i18NSource.getMessage("token.servlet.notoken.associatedwithaction",
					tokenWrapper.getTask().getInternalTaskId(), tokenWrapper.getUser().getLogin(), tokenWrapper.getTokenAction());
			
			textDecorator.addText(exceptionMessage);
			
			/* Write to user output page */
			out.append(textDecorator.getOutput());
			
			log(exceptionMessage);
			throw new IllegalStateException(exceptionMessage);
		}
		
		/* Create bpm session */
		
		ProcessInstance process = tokenWrapper.getTask().getProcessInstance();

		ProcessActionManager processActionManager = new ProcessActionManager(ctx, tokenWrapper.getUser());
		List<BpmTask> newTasks = processActionManager.perfomAction(tokenAction, tokenWrapper.getTask());

		String taskName = tokenAction.getLabel();
		String localizedTaskName = i18NSource.getMessage(taskName);
		
		String processName = tokenWrapper.getTask().getExternalProcessId() != null ? tokenWrapper.getTask().getExternalProcessId() : "";
		
		textDecorator.addText(i18NSource.getMessage("token.servlet.action.performed",
				localizedTaskName, processName, tokenWrapper.getTask().getInternalProcessId()));
		
		ctx.getProcessInstanceDAO().refresh(process);

		if (!newTasks.isEmpty())
		{
			ProcessStateConfiguration newState = newTasks.get(0).getCurrentProcessStateConfiguration();
			
			String localizedNewTaskName = i18NSource.getMessage(newState.getDescription());
			textDecorator.addText(i18NSource.getMessage("token.servlet.action.newaction", localizedNewTaskName));
		}
		
		/* Write to user output page */
		out.append(textDecorator.getOutput());
	}
}
