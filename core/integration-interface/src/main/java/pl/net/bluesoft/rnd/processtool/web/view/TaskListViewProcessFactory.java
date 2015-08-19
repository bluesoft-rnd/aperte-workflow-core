package pl.net.bluesoft.rnd.processtool.web.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: Maciej
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskListViewProcessFactory
{
    String processName();
    Class<? extends ITasksListViewBeanFactory> factoryClass();
}
