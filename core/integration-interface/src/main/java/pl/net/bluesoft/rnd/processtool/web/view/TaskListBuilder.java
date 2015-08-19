package pl.net.bluesoft.rnd.processtool.web.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.dict.IDictionaryFacade;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.GuiRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mpawlak@bluesoft.net.pl on 2014-08-28.
 */
public class TaskListBuilder
{
    private String queueId;
    private String ownerLogin;

    @Autowired
    private ProcessToolRegistry registry;

    @Autowired
    protected IUserSource userSource;

    @Autowired
    protected ISettingsProvider settingsProvider;

    @Autowired
    protected IHtmlTemplateProvider templateProvider;

    @Autowired
    protected IDictionaryFacade dictionaryFacade;

    protected I18NSource i18Source;
    protected UserData user;



    public TaskListBuilder()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public StringBuilder build()
    {
        final StringBuilder stringBuilder = new StringBuilder(8 * 1024);

        Map<String, Object> viewData = new HashMap<String, Object>();

        viewData.put(IHtmlTemplateProvider.USER_PARAMTER, user);
        viewData.put(IHtmlTemplateProvider.QUEUE_ID_PARAMTER, queueId);
        viewData.put(IHtmlTemplateProvider.USER_SOURCE_PARAMTER, userSource);
        viewData.put(IHtmlTemplateProvider.MESSAGE_SOURCE_PARAMETER, i18Source);
        viewData.put(IHtmlTemplateProvider.DICTIONARIES_FACADE, dictionaryFacade);
        viewData.put(IHtmlTemplateProvider.SETTINGS_PROVIDER, settingsProvider);
        viewData.put(IHtmlTemplateProvider.OWNER_LOGIN_PARAMTER, ownerLogin);

        AbstractTaskListView taskListView = registry.getGuiRegistry().getTasksListView(queueId);
        if(taskListView == null)
        {
            String processedView = templateProvider.processTemplate(GuiRegistry.STANDARD_PROCESS_QUEUE_ID, viewData);

            stringBuilder.append(processedView);
        }
        else
        {
            String processedView = templateProvider.processTemplate(queueId, viewData);

            stringBuilder.append(processedView);
        }



        return stringBuilder;
    }


    public TaskListBuilder setQueueId(String queueId) {
        this.queueId = queueId;
        return this;
    }

    public TaskListBuilder setI18Source(I18NSource i18Source) {
        this.i18Source = i18Source;

        return this;
    }


    public TaskListBuilder setUser(UserData user) {
        this.user = user;

        return this;
    }

    public TaskListBuilder setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;

        return this;
    }
}
