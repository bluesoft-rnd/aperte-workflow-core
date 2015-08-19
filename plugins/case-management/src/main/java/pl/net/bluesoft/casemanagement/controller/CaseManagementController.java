package pl.net.bluesoft.casemanagement.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.aperteworkflow.webapi.main.processes.action.domain.SaveResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.ValidateResultBean;
import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;
import org.aperteworkflow.webapi.main.processes.domain.NewProcessInstanceBean;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.casemanagement.controller.bean.CaseDTO;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseStage;
import pl.net.bluesoft.casemanagement.model.CaseStateWidget;
import pl.net.bluesoft.casemanagement.processor.CaseProcessor;
import pl.net.bluesoft.casemanagement.ui.CaseViewBuilder;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by pkuciapski on 2014-04-24.
 */
@OsgiController(name = "casemanagementcontroller")
public class CaseManagementController implements IOsgiWebController {
    public static final String SYSTEM_SOURCE = "System";

    protected final Logger logger = Logger.getLogger(CaseManagementController.class.getName());

    protected static final String CASE_ID_REQ_PARAM_NAME = "caseId";
    protected static final String CASE_TYPE_REQ_PARAM_NAME = "caseType";
    protected static final String PAGE_LIMIT_REQ_PARAM_NAME = "page_limit";
    protected static final String PAGE_PARAM_NAME = "page";
    protected static final String SEARCH_QUERY_REQ_PARAM_NAME = "query";
    protected static final String BPM_DEFINITION_KEY_REQ_PARAM_NAME = "bpmDefinitionKey";

    @Autowired
    protected ICaseManagementFacade facade;
    @Autowired
    protected ProcessToolRegistry processToolRegistry;

    @ControllerMethod(action = "getAllCasesPaged")
    public GenericResultBean getAllCasesPaged(final OsgiWebRequest invocation) {
        final GenericResultBean result = new GenericResultBean();
        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
        JQueryDataTableColumn sortColumn = dataTable.getFirstSortingColumn();

        sortColumn.setPropertyName(CaseDTO.getCasePropertyName(sortColumn.getPropertyName()));
        try {
            final Collection<Case> cases = facade.getAllCasesPaged(sortColumn.getPropertyName(), sortColumn.getSortedAsc(), dataTable.getPageLength(), dataTable.getPageOffset());
            final Collection<CaseDTO> dtos = createDTOList(cases, invocation.getProcessToolRequestContext().getMessageSource());
            // unfortunately, we have to count all cases for the list pagination to work properly
            Long count = facade.getAllCasesCount();
            final DataPagingBean<CaseDTO> dataPagingBean =
                    new DataPagingBean<CaseDTO>(dtos, count.intValue(), dataTable.getDraw());

            return dataPagingBean;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[CASE_MANAGEMENT] Cannot get the case list", e);
            result.addError("Cannot get the case list", e.getMessage());
        }
        return result;
    }

    private List<CaseDTO> createDTOList(final Collection<Case> cases, I18NSource messageSource) {
        List<CaseDTO> dtos = new ArrayList<CaseDTO>();
        for (Case c : cases) {
            dtos.add(CaseDTO.createFrom(c, messageSource));
        }
        return dtos;
    }

	protected Long getCaseId(final HttpServletRequest request) {
		return getCaseId(request, true);
	}

    protected Long getCaseId(final HttpServletRequest request, boolean required) {
        String param = request.getParameter(CASE_ID_REQ_PARAM_NAME);
        Long caseId = hasText(param) ? Long.valueOf(param) : null;
		if (caseId == null && required) {
			throw new RuntimeException("Case ID not provided in request!");
		}
		return caseId;
    }

	@ControllerMethod(action = "loadCase")
    public GenericResultBean loadCase(final OsgiWebRequest invocation) {
        final GenericResultBean result = new GenericResultBean();
        final Long caseId = getCaseId(invocation.getRequest());
        final Case caseInstance = getCaseById(caseId);

        processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(final ProcessToolContext ctx) {
                final IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
                final String data = buildCaseView(caseInstance, ctx, requestContext);
                result.setData(data);
            }
        });

        return result;
    }

    protected String buildCaseView(final Case caseInstance, final ProcessToolContext ctx, final IProcessToolRequestContext requestContext) {
        CaseStage currentStage = caseInstance.getCurrentStage();
        final List<CaseStateWidget> widgets = new ArrayList<CaseStateWidget>();

        if (currentStage != null) {
            widgets.addAll(currentStage.getCaseStateDefinition().getWidgets());
        }
        // sort widgets by priority
        Collections.sort(widgets, new Comparator<CaseStateWidget>() {
            @Override
            public int compare(CaseStateWidget caseStateWidget, CaseStateWidget caseStateWidget2) {
                return caseStateWidget.getPriority().compareTo(caseStateWidget2.getPriority());
            }
        });
        CaseViewBuilder viewBuilder = CaseViewBuilder.create(caseInstance)
                .setWidgets(widgets)
                .setI18Source(requestContext.getMessageSource())
                .setUser(requestContext.getUser())
                .setCtx(ctx)
                .setUserQueues(requestContext.getUserQueues())
                .setBpmSession(requestContext.getBpmSession());
        try {
            String data = viewBuilder.build().toString();
            return data;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem during a case view generation. CaseId=" + caseInstance.getId(), e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @ControllerMethod(action = "saveAction")
    public GenericResultBean saveAction(final OsgiWebRequest invocation) {
        logger.info("CaseManagementController.saveAction");
        final GenericResultBean result = new GenericResultBean();

        final IProcessToolRequestContext context = invocation.getProcessToolRequestContext();
        if (!context.isUserAuthorized()) {
            result.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
            return result;
        }

        final HttpServletRequest request = invocation.getRequest();
        final String caseId = request.getParameter("caseId");
        final String widgetDataJson = request.getParameter("widgetData");
        final Collection<HtmlWidget> widgets;

        if (isNull(caseId)) {
            result.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.nocaseid"));
            return result;
        }
        widgets = mapWidgets(widgetDataJson, result, context);
        if (result.hasErrors())
            return result;
        final Case caseInstance = getCaseById(Long.valueOf(caseId));
        final CaseProcessor processor = new CaseProcessor(caseInstance, caseInstance, context.getMessageSource(), widgets, context.getUser());

        ValidateResultBean validateResult = processor.validateWidgets();
        if (validateResult.hasErrors()) {
            result.copyErrors(validateResult);
        } else {
            // save widgets data
            SaveResultBean saveResult = processor.saveWidgets();
            result.copyErrors(saveResult);
            if (!result.hasErrors()) {
                facade.updateCase(caseInstance);
                String data = buildCaseView(caseInstance, invocation.getProcessToolContext(), invocation.getProcessToolRequestContext());
                result.setData(data);
            }
        }
        return result;
    }

    private Collection<HtmlWidget> mapWidgets(final String widgetDataJson, AbstractResultBean result, IProcessToolRequestContext context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, HtmlWidget.class);
            return mapper.readValue(widgetDataJson, type);
        } catch (Throwable e) {
            result.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.jsonparseerror") + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    protected static boolean isNull(String value) {
        return value == null || value.isEmpty() || "null".equals(value);
    }

    @ControllerMethod(action = "startProcessInstance")
    public NewProcessInstanceBean startProcessInstance(final OsgiWebRequest invocation) {
        NewProcessInstanceBean result = new NewProcessInstanceBean();
        try {
            String bpmDefinitionKey = invocation.getRequest().getParameter(BPM_DEFINITION_KEY_REQ_PARAM_NAME);
            if (bpmDefinitionKey == null)
                throw new RuntimeException("bpmDefinitionKey cannot be null!");

			final Long caseId = getCaseId(invocation.getRequest(), isCaseProcess(bpmDefinitionKey));

            Case caseInstance = getCaseById(caseId);
            StartProcessResult startProcessResult = facade.startProcessInstance(caseInstance, bpmDefinitionKey, invocation.getProcessToolRequestContext());
            List<BpmTask> tasks = startProcessResult.getTasksAssignedToCreator();
            if (!tasks.isEmpty()) {
                BpmTask task = tasks.get(0);
                result.setTaskId(task.getInternalTaskId());
                result.setProcessStateConfigurationId(task.getCurrentProcessStateConfiguration().getId().toString());
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "[CASE_MANAGEMENT] Cannot start the new process", e);
            result.addError("Cannot start the new process", e.getMessage());
        }
        return result;
    }

	protected boolean isCaseProcess(String bpmDefinitionKey) {
		return true;
	}

	protected Case getCaseById(Long caseId) {
        Case caseInstance = facade.getCaseById(caseId);
        if (caseInstance == null)
            throw new RuntimeException(String.format("Case with id=%d not found!", caseId));
        return caseInstance;
    }
}
