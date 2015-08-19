package pl.net.bluesoft.interactivereports.controller;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.interactivereports.service.InteractiveReportService;
import pl.net.bluesoft.interactivereports.templates.InteractiveReportTemplate;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2014-06-24
 */
@OsgiController(name = "interactivereportscontroller")
public class InteractiveReportsController implements IOsgiWebController {
	private final Logger logger = Logger.getLogger(InteractiveReportsController.class.getName());

	@Autowired
	private ProcessToolRegistry processToolRegistry;

	@Autowired
	private InteractiveReportService reportService;

	@ControllerMethod(action = "getAvailableReports")
	public GenericResultBean getAvailableReports(OsgiWebRequest invocation) {
		UserData user = invocation.getProcessToolRequestContext().getUser();
		Map<String, InteractiveReportTemplate> availableReportTemplates = reportService.getAvailableReportTemplates(user);

		GenericResultBean result = new GenericResultBean();

		try {
			I18NSource messageSource = invocation.getProcessToolRequestContext().getMessageSource();
			result.setData(toDTO(availableReportTemplates, messageSource));
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "[INTERACTIVE_REPORTS] Cannot get report list", e);
			result.addError("Cannot get report list", e.getMessage());
		}
		return result;
	}

	private List<ReportTemplateDTO> toDTO(Map<String, InteractiveReportTemplate> availableReportTemplates, I18NSource messageSource) {
		List<ReportTemplateDTO> result = new ArrayList<ReportTemplateDTO>();

		for (Map.Entry<String, InteractiveReportTemplate> entry : availableReportTemplates.entrySet()) {
			String key = entry.getKey();
			String name = messageSource.getMessage(entry.getValue().getName());

			result.add(new ReportTemplateDTO(key, name));
		}
		Collections.sort(result, new Comparator<ReportTemplateDTO>() {
			@Override
			public int compare(ReportTemplateDTO template1, ReportTemplateDTO template2) {
				return template1.getName().compareTo(template2.getName());
			}
		});
		return result;
	}

	@ControllerMethod(action = "renderReportParams")
	public GenericResultBean renderReportParams(OsgiWebRequest invocation) {
		String reportTemplateKey = invocation.getRequest().getParameter("reportTemplate");

		InteractiveReportTemplate reportTemplate = reportService.getReportTemplate(reportTemplateKey);

		GenericResultBean result = new GenericResultBean();

		try {
			result.setData(reportTemplate.renderReportParams(getRenderParams(invocation, null)));
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "[INTERACTIVE_REPORTS] Cannot render report params: " +  e.getMessage(), e);
			result.addError("Cannot render report params", e.getMessage());
		}
		return result;
	}

	@ControllerMethod(action = "generateReport")
	public GenericResultBean generateReport(OsgiWebRequest invocation) {
		String reportTemplateKey = invocation.getRequest().getParameter("reportTemplate");
		String reportParamsStr = invocation.getRequest().getParameter("reportParams");

		long start = System.currentTimeMillis();

		InteractiveReportTemplate reportTemplate = reportService.getReportTemplate(reportTemplateKey);
		GenericResultBean result = new GenericResultBean();

		try {
			Map<String, Object> reportParams = parseParams(reportParamsStr);
			result.setData(reportTemplate.renderReport(getRenderParams(invocation, reportParams)));
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "[INTERACTIVE_REPORTS] Cannot render report: " +  e.getMessage(), e);
			result.addError("Cannot render report", e.getMessage());
		}

		logger.info("Render " + reportTemplateKey + " time = " + (System.currentTimeMillis() - start));

		return result;
	}

	private Map<String, Object> parseParams(String paramStr) {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		TypeReference<ArrayList<ParamEntryDTO>> typeRef = new TypeReference<ArrayList<ParamEntryDTO>>() {};

		try {
			List<ParamEntryDTO> paramList = mapper.readValue(paramStr, typeRef);
			Map<String, Object> result = new HashMap<String, Object>();

			for (ParamEntryDTO param : paramList) {
				result.put(param.getName(), formatParam(param.getValue()));
			}
			return result;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Object formatParam(Object value) {
		if (value instanceof String) {
			String strValue = (String)value;
			return hasText(strValue) ? strValue.trim() : null;
		}
		return value;
	}

	private InteractiveReportTemplate.ExportParams getExportParams(OsgiWebRequest invocation, Map<String, Object> reportParams) {
		InteractiveReportTemplate.ExportParams result = getRenderParams(new InteractiveReportTemplate.ExportParams(), invocation, reportParams);
		String desiredFormat = invocation.getRequest().getParameter("desiredFormat");
		result.setDesiredFormat(desiredFormat);
		return result;
	}

	private InteractiveReportTemplate.RenderParams getRenderParams(OsgiWebRequest invocation, Map<String, Object> reportParams) {
		return getRenderParams(new InteractiveReportTemplate.RenderParams(), invocation, reportParams);
	}

	private <T extends InteractiveReportTemplate.RenderParams> T getRenderParams(T renderParams,
																   OsgiWebRequest invocation,
																   Map<String, Object> reportParams) {
		renderParams.setUser(invocation.getProcessToolRequestContext().getUser());
		renderParams.setMessageSource(invocation.getProcessToolRequestContext().getMessageSource());
		renderParams.setReportParams(reportParams);
		return renderParams;
	}

	@ControllerMethod(action = "exportReport")
	public GenericResultBean exportReport(OsgiWebRequest invocation) {
		String reportTemplateKey = invocation.getRequest().getParameter("reportTemplate");
		String reportParamsStr = invocation.getRequest().getParameter("reportParams");

		InteractiveReportTemplate reportTemplate = reportService.getReportTemplate(reportTemplateKey);

		GenericResultBean result = new GenericResultBean();

		try {
			Map<String, Object> reportParams = parseParams(reportParamsStr);

			InteractiveReportTemplate.ExportParams exportParams = getExportParams(invocation, reportParams);
			InteractiveReportTemplate.ExportResult exportResult = reportTemplate.export(exportParams);

			if (exportResult != null) {
				HttpServletResponse response = invocation.getResponse();

				sendInResponseOutputStream(response, exportResult.getFileName(), exportResult.getContentType(),
						exportResult.getContent());
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "[INTERACTIVE_REPORTS] Cannot export report", e);
			result.addError("Cannot export report", e.getMessage());
		}
		return result;
	}

	private void sendInResponseOutputStream(HttpServletResponse response,
											String fileName, String contentType, byte[] data) throws IOException {
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + '"');
		response.setContentType(contentType);
		ServletOutputStream soutStream = response.getOutputStream();
		IOUtils.write(data, soutStream);
		IOUtils.closeQuietly(soutStream);
	}
}
