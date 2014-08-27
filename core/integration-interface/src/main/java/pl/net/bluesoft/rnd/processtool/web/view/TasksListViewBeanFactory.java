package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Created by Marcin Król on 2014-05-09.
 */
public interface TasksListViewBeanFactory {
    public TasksListViewBean createFrom(BpmTask task, I18NSource messageSource);

    public IBpmTaskQueryCondition getBpmTaskQueryCondition();


}
