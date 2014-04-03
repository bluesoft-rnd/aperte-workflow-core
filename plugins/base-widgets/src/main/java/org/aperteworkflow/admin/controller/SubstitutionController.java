package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import java.util.List;

/**
 *
 * Substitution operations controller for admin portlet
 *
 * @author: mpawlak@bluesoft.net.pl
 */
@OsgiController(name="substitutionController")
public class SubstitutionController implements IOsgiWebController
{
    @Autowired
    protected IPortalUserSource portalUserSource;

    @Autowired
    protected ProcessToolRegistry processToolRegistry;

    @ControllerMethod(action="loadSubstitutions")
    public GenericResultBean loadSubstitutions(final OsgiWebRequest invocation)
    {

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        ProcessToolContext ctx = invocation.getProcessToolContext();

        List<UserSubstitution> substitutionList = ctx.getUserSubstitutionDAO().findAll();


        final JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());

        DataPagingBean<UserSubstitution> dataPagingBean =
                new DataPagingBean<UserSubstitution>(substitutionList, substitutionList.size(), dataTable.getEcho());



        return dataPagingBean;
    }

    @ControllerMethod(action="deleteSubtitution")
    public GenericResultBean deleteSubtitution(final OsgiWebRequest invocation)
    {

        GenericResultBean result = new GenericResultBean();

        String substitutionId = invocation.getRequest().getParameter("substitutionId");

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        ProcessToolContext ctx = invocation.getProcessToolContext();

        ctx.getUserSubstitutionDAO().deleteById(Long.parseLong(substitutionId));

        return result;
    }
}
