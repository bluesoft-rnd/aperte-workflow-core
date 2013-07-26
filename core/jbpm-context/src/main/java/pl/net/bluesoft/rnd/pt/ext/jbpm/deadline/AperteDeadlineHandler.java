package pl.net.bluesoft.rnd.pt.ext.jbpm.deadline;

import org.jbpm.task.Content;
import org.jbpm.task.Deadline;
import org.jbpm.task.Task;
import org.jbpm.task.service.EscalatedDeadlineHandler;
import org.jbpm.task.service.TaskService;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class AperteDeadlineHandler implements EscalatedDeadlineHandler
{
    @Override
    public void executeEscalatedDeadline(Task task, Deadline deadline, Content content, TaskService taskService)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
