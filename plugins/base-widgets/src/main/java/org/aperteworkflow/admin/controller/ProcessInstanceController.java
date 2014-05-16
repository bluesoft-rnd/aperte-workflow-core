package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Process Instance operations controller for admin portlet.
 *
 * @author: lgajowy@bluesoft.net.pl
 */

@OsgiController(name = "processInstanceController")
public class ProcessInstanceController implements IOsgiWebController {

    @Autowired
    protected IPortalUserSource portalUserSource;
    @Autowired
    protected ProcessToolRegistry processToolRegistry;
    @Autowired
    protected ProcessToolSessionFactory processToolSessionFactory;

    @ControllerMethod(action = "findProcessInstances")
    public GenericResultBean findProcessInstances(final OsgiWebRequest invocation) {

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        I18NSource messageSource = requestContext.getMessageSource();
        ProcessToolBpmSession bpmSession = requestContext.getBpmSession();

        Map<String, String[]> parameterMap = invocation.getRequest().getParameterMap();
        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(parameterMap);

        String searchCriteria = invocation.getRequest().getParameter("filter");
        Boolean isOnlyActive = "true".equals(invocation.getRequest().getParameter("onlyActive"));

        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(invocation.getProcessToolContext().getProcessInstanceDAO()
                .searchProcesses(searchCriteria, dataTable.getPageOffset(), dataTable.getPageLength(), isOnlyActive, null, null));

        final List<ProcessInstanceBean> processInstanceBeans = createProcessInstanceBeansList(processInstances, messageSource, bpmSession);
        DataPagingBean<ProcessInstanceBean> dataPagingBean = new DataPagingBean<ProcessInstanceBean>(processInstanceBeans, processInstanceBeans.size(), dataTable.getEcho());
        return dataPagingBean;
    }

    private List<ProcessInstanceBean> createProcessInstanceBeansList(List<ProcessInstance> processInstances, I18NSource messageSource, ProcessToolBpmSession bpmSession) {
        final List<ProcessInstanceBean> processInstanceBeans = new ArrayList<ProcessInstanceBean>();
        for (ProcessInstance instance : processInstances) {
            List<ProcessInstanceBean> beans = ProcessInstanceBean.createBeans(instance, messageSource, bpmSession);
            processInstanceBeans.addAll(beans);
        }
        return processInstanceBeans;
    }

    @ControllerMethod(action = "cancelProcessInstance")
    public GenericResultBean cancelProcessInstance(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        ProcessToolBpmSession bpmSession = requestContext.getBpmSession();
        bpmSession.adminCancelProcessInstance(invocation.getRequest().getParameter("processInstanceId"));
        return result;
    }

    @ControllerMethod(action = "performAction")
    public GenericResultBean performAction(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        ProcessToolBpmSession bpmSession = requestContext.getBpmSession();

        String taskId = invocation.getRequest().getParameter("taskInternalId");

        // todo: make it dynamic!!!
        bpmSession.adminCompleteTask(taskId, "acceptance_reject");
        return result;
    }

    @ControllerMethod(action = "modifyTaskAssignee")
    public GenericResultBean modifyTaskAsignee(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();

        HttpServletRequest request = invocation.getRequest();
        final String taskInternalId = request.getParameter("taskInternalId");
        final String oldUserLogin = request.getParameter("oldUserLogin");
        final String newUserLogin = request.getParameter("newUserLogin");

        //albo własciciel taska albo potencjalni wlasciciele (potential owners)
        final UserData taskOwner = portalUserSource.getUserByLogin(oldUserLogin);

        ProcessToolBpmSession bpmSession = processToolSessionFactory.createSession(taskOwner);

        //kto może domagać się (claim) tasków? tylko wlasciciel i potencjalni właścicele?
        //Jezeli tak, to jak reassignować ten task za pomocą poniższej metody?

        //czy jezeli jest single potential owner to nie powinno byc tak, że nie da sie reassignowac?
        //w takiej sytuacji jak zrobić, w aktualnym liferayu zeby było wiele potential ownerów w tasku?
        bpmSession.adminReassignProcessTask(taskInternalId, newUserLogin);

        return result;
    }
}
