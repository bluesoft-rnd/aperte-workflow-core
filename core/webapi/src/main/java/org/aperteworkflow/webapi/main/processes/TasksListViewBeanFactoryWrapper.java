package org.aperteworkflow.webapi.main.processes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.view.BpmTaskBeanFactory;
import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBean;
import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBeanFactory;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.logging.Logger;

/**
 * Created by Marcin Król on 2014-05-09.
 */
public class TasksListViewBeanFactoryWrapper {

    private static Logger logger = Logger.getLogger(TasksListViewBeanFactoryWrapper.class.getName());

    @Autowired(required = false)
    private ProcessToolRegistry processToolRegistry;

    public TasksListViewBean createFrom(BpmTask task, I18NSource messageSource, String viewName)
    {
        if(viewName==null || viewName.isEmpty())
            return new BpmTaskBeanFactory().createFrom(task, messageSource);

        TasksListViewBeanFactory taskViewBean = getProcessToolRegistry().getGuiRegistry().getTasksListView(viewName);
        if(taskViewBean == null)
            logger.severe("There is no task view bean factory registered with given name: "+viewName);

        return taskViewBean.createFrom(task, messageSource);
    }


    protected ProcessToolRegistry getProcessToolRegistry()
    {
        if(processToolRegistry == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return this.processToolRegistry;
    }

}
