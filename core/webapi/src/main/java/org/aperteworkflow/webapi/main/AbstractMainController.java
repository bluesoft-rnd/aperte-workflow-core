package org.aperteworkflow.webapi.main;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSessionHelper;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.Collection;
import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Created with IntelliJ IDEA.
 * User: mpawlak
 * Date: 30.06.13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class AbstractMainController
{
    public static final String PROCESS_START_LIST = "processStartList";
    public static final String QUEUES_PARAMETER_NAME = "queues";
    public static final String USER_PARAMETER_NAME = "aperteUser";
    public static final String IS_STANDALONE = "isStandAlone";

    /** Add user queeus to model */
    protected Collection<UserProcessQueuesSizeProvider.UsersQueuesDTO> addUserQueues(UserData user, ProcessToolContext ctx, I18NSource messageSource)
    {
        UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(ctx.getRegistry(), user.getLogin(), messageSource);
        Collection<UserProcessQueuesSizeProvider.UsersQueuesDTO> queues = userQueuesSizeProvider.getUserProcessQueueSize();

        return queues;
    }

    /** Add process start definition */
    protected List<ProcessDefinitionConfig> addProcessStartList(ProcessToolContext ctx, ProcessToolBpmSession bpmSession)
    {
        List<ProcessDefinitionConfig> orderedByProcessDescr = from(ProcessToolBpmSessionHelper.getAvailableConfigurations(bpmSession, ctx))
                .orderBy(new F<ProcessDefinitionConfig, String>() {
                    @Override
                    public String invoke(ProcessDefinitionConfig pdc) {
                        return pdc.getDescription();
                    }
                })
                .toList();

        return orderedByProcessDescr;
    }
}
