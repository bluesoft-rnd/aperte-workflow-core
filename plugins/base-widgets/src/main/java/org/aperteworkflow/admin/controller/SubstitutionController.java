package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.hibernate.criterion.Order;
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

import static pl.net.bluesoft.util.lang.DateUtil.beginOfDay;
import static pl.net.bluesoft.util.lang.DateUtil.endOfDay;
import static pl.net.bluesoft.util.lang.Formats.parseShortDate;

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

        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
        JQueryDataTableColumn sortingColumn = dataTable.getFirstSortingColumn();

        List<UserSubstitution> substitutionList =
                (List<UserSubstitution>)ctx.getHibernateSession()
                        .createCriteria(UserSubstitution.class)
                        .addOrder(                        sortingColumn.getSortedAsc() ?
                                Order.asc(sortingColumn.getPropertyName()) :
                                Order.desc(sortingColumn.getPropertyName()))
                        .setMaxResults(dataTable.getPageLength())
                        .setFirstResult(dataTable.getPageOffset())
                        .list();


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

    @ControllerMethod(action="addNewSubstitution")
    public GenericResultBean addNewSubstitution(final OsgiWebRequest invocation)
    {

        GenericResultBean result = new GenericResultBean();

        String userLogin = invocation.getRequest().getParameter("userLogin");
        String userSubstituteLogin = invocation.getRequest().getParameter("userSubstituteLogin");
        String dateFrom = invocation.getRequest().getParameter("dateFrom");
        String dateTo = invocation.getRequest().getParameter("dateTo");

        UserSubstitution userSubstitution = new UserSubstitution();

        userSubstitution.setUserLogin(userLogin);
        userSubstitution.setDateFrom(beginOfDay(parseShortDate(dateFrom)));
        userSubstitution.setDateTo(endOfDay(parseShortDate(dateTo)));
        userSubstitution.setUserSubstituteLogin(userSubstituteLogin);

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        ProcessToolContext ctx = invocation.getProcessToolContext();

        ctx.getUserSubstitutionDAO().saveOrUpdate(userSubstitution);

        return result;
    }
}
