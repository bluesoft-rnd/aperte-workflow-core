package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
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
        DataPagingBean<ProcessInstanceBean> dataPagingBean = new DataPagingBean<ProcessInstanceBean>(processInstanceBeans, processInstanceBeans.size(), dataTable.getDraw());
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

        String taskId = invocation.getRequest().getParameter("taskInternalId");
        String action = invocation.getRequest().getParameter("actionToPerform");
        ProcessToolBpmSession session = processToolSessionFactory.createSession(requestContext.getBpmSession().getTaskData(taskId).getAssignee());
        session.adminCompleteTask(taskId, action);
        return result;
    }

    @ControllerMethod(action = "modifyTaskAssignee")
    public GenericResultBean modifyTaskAsignee(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();

        HttpServletRequest request = invocation.getRequest();
        final String taskInternalId = request.getParameter("taskInternalId");
        final String newUserLogin = request.getParameter("newUserLogin");
        String oldUserLogin = request.getParameter("oldUserLogin");
        ProcessToolBpmSession bpmSession = invocation.getProcessToolRequestContext().getBpmSession();

        // FIXME: if old user is null then we cannot reassign this task.
        // Forwarding changes status to Ready and leaves the task unassigned.
        bpmSession.adminForwardProcessTask(taskInternalId, oldUserLogin, newUserLogin);
        if (newUserLogin != null) {
            bpmSession.adminReassignProcessTask(taskInternalId, newUserLogin);
        }
        return result;
    }
}
