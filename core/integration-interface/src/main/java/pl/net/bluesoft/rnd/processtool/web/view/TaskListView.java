package pl.net.bluesoft.rnd.processtool.web.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Marcin Król on 2014-05-09.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskListView {
    String queueId();
    String queueDisplayedName();
    String queueDisplayedDescription();
    String fileName();
    AbstractTaskListView.QueueTypes queueType();
    int priority() default 1;

    Class<? extends ITasksListViewBeanFactory> mainFactory();
    TaskListViewProcessFactory[] processFactories();
}
