package pl.net.bluesoft.lot.casemanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.lot.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.lot.casemanagement.exception.CaseManagementException;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseDTO;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
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

    @ControllerMethod(action = "getAllCases")
    public GenericResultBean getAllCases(final OsgiWebRequest invocation) {
        final GenericResultBean result = new GenericResultBean();
        final HttpServletRequest request = invocation.getRequest();
        // final long caseId = getCaseId(request);

        try {
            Collection<Case> cases = facade.getCases();
            Iterator<Case> i = cases.iterator();
            final Case case1 = i.next();
            final Case case2 = i.next();
            cases.clear();
            cases.add(case1);
            cases.add(case2);

            result.setData(createDTOList(cases));
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
