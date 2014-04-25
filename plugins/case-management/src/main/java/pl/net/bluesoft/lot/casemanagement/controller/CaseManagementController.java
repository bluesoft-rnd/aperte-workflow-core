package pl.net.bluesoft.lot.casemanagement.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.lot.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.lot.casemanagement.exception.CaseManagementException;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseDTO;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-24.
 */
@OsgiController(name = "casemanagementcontroller")
public class CaseManagementController implements IOsgiWebController {
    protected final Logger logger = Logger.getLogger(CaseManagementController.class.getName());

    private static final String CASE_ID_REQ_PARAM_NAME = "caseId";
    @Autowired
    private ICaseManagementFacade facade;

    @ControllerMethod(action = "getAllCasesPaged")
    public GenericResultBean getAllCasesPaged(final OsgiWebRequest invocation) {
        final GenericResultBean result = new GenericResultBean();
        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
        JQueryDataTableColumn sortColumn = dataTable.getFirstSortingColumn();

        // sortColumn.setPropertyName(CaseDTO.getCasePropertyName(sortColumn.getPropertyName()));
        try {
            final Collection<Case> cases = facade.getAllCasesPaged(sortColumn.getPropertyName(), sortColumn.getSortedAsc(), dataTable.getPageLength(), dataTable.getPageOffset());
            final Collection<CaseDTO> dtos = createDTOList(cases);
            // unfortunately, we have to count all cases for the list pagination to work properly
            Long count = facade.getAllCasesCount();
            final DataPagingBean<CaseDTO> dataPagingBean =
                    new DataPagingBean<CaseDTO>(dtos, count.intValue(), dataTable.getEcho());

            return dataPagingBean;
        } catch (CaseManagementException e) {
            logger.log(Level.SEVERE, "[CASE_MANAGEMENT] Cannot get the case list", e);
            result.addError("Cannot get the case list", e.getMessage());
        }
        return result;
    }

    private Collection<CaseDTO> createDTOList(final Collection<Case> cases) {
        Collection<CaseDTO> dtos = new ArrayList<CaseDTO>();
        for (Case c : cases) {
            dtos.add(new CaseDTO(c));
        }
        return dtos;
    }

    private Long getCaseId(final HttpServletRequest request) {
        final String param = request.getParameter(CASE_ID_REQ_PARAM_NAME);
        final Long caseId = param != null ? Long.valueOf(param) : null;
        if (caseId == null) {
            throw new RuntimeException("Case ID not provided in request!");
        }
        return caseId;
    }
}
