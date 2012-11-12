package pl.net.bluesoft.rnd.pt.ext.report.step;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.collection.PersistentSet;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.report.model.ReportDAO;
import pl.net.bluesoft.rnd.pt.ext.report.model.ReportTemplate;
import pl.net.bluesoft.rnd.pt.ext.report.util.dict.DictionaryHelperImpl;
import pl.net.bluesoft.rnd.pt.utils.cmis.CmisAtomSessionFacade;
import pl.net.bluesoft.util.lang.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

@AliasName(name = "GenerateDocumentStep")
public class GenerateDocument implements ProcessToolProcessStep {

	private static final String DATETIME_PATTERN = "dd-MM-yyyy HH:mm";
	private static final String PROCESS_INSTANCE_KEY = "processInstance";
	private static final String PROCESS_INSTANCE_ID_KEY = "processInstanceId";
	private static final String PROCESS_CONTEXT_KEY = "processContext";
	private static final String DICTIONARY_HELPER = "dictionaryHelper";
	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	private static final String DEFAULT_ENCODING = "Cp1250";

	private static class MyPersistentSetConverter extends CollectionConverter {
		public MyPersistentSetConverter(Mapper mapper) {
			super(mapper);
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			PersistentSet ps = (PersistentSet) source;
			super.marshal(new HashSet(ps), writer, context);
		}

		@Override
		public boolean canConvert(Class type) {
			return type.isAssignableFrom(PersistentSet.class);
		}
	}

	public class ReportException extends Exception {
		private static final long serialVersionUID = 4702732796115095701L;

		public ReportException(String message) {
			super("Report '" + reportName + "': " + message);
		}

		public ReportException(String message, Throwable t) {
			super("Report '" + reportName + "': " + message, t);
		}

	}

	private Logger logger = Logger.getLogger(GenerateDocument.class.getName());

	@AutoWiredProperty
	private String reportName;

	@AutoWiredProperty
	private String defaultLocaleName;
	@AutoWiredProperty
	private String localeAttributeKey = "java.util.Locale";


	@AutoWiredProperty
	private String format = "PDF";
	@AutoWiredProperty
	private Locale locale = DEFAULT_LOCALE;
	@AutoWiredProperty
	private String encoding = DEFAULT_ENCODING;
	@AutoWiredProperty
	private String outputFileName = "report.pdf";
	@AutoWiredProperty
	private String mimeType = "application/pdf";

	@AutoWiredProperty
	private String repositoryAtomUrl = "http://dreihund:8080/alfresco/service/cmis";
	@AutoWiredProperty
	private String repositoryId = "default";
	@AutoWiredProperty
	private String repositoryUser = "awf";
	@AutoWiredProperty
	private String repositoryPassword = "awf";

	@AutoWiredProperty
	private String rootFolderPath = "/processtool/docs";
	@AutoWiredProperty
	private String subFolder = "test1";
	@AutoWiredProperty
	private String newFolderPrefix = "pt_";
	private CmisAtomSessionFacade sessionFacade;
	private Folder mainFolder;

	@AutoWiredProperty
	private String popup = "";
	
	private static final String POPUP_ONCE = "ONCE";
	private static final String POPUP_ALWAYS = "ALWAYS";

	@Override
	public String invoke(BpmStep step, Map<String, String> params) throws Exception {
		try {
			// GET PARAMETERS FOR REPORT
			reportName = (String) params.get("reportName");
			subFolder = (String) params.get("subFolder");
			outputFileName = (String) params.get("outputFileName");
			rootFolderPath = (String) params.get("rootFolderPath");
			repositoryAtomUrl = (String) params.get("repositoryAtomUrl");
			repositoryId = (String) params.get("repositoryId");
			repositoryUser = (String) params.get("repositoryUser");
			repositoryPassword = (String) params.get("repositoryPassword");
			if (params.get("localeAttributeKey") != null) {
				localeAttributeKey = (String) params.get("localeAttributeKey");
			}
			defaultLocaleName = (String) params.get("defaultLocaleName");

			ProcessInstance processInstance = step.getProcessInstance();

			initLocale(processInstance);
			// BUILD REPORT
			logger.warning("GenerateDocument start, building report");
			byte[] report = buildReport(processInstance);
			logger.warning("report built");

			saveReport(processInstance, report);
			logger.warning("report saved");

			return "OK";
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return "ERROR";
		}
	}

	public static Locale getLocaleFromString(String localeString) {
		if (localeString == null) {
			return null;
		}
		localeString = localeString.trim();
		if (localeString.toLowerCase().equals("default")) {
			return Locale.getDefault();
		}

		int languageIndex = localeString.indexOf('_');
		String language = null;
		if (languageIndex == -1) {
			return new Locale(localeString, "");
		} else {
			language = localeString.substring(0, languageIndex);
		}

		int countryIndex = localeString.indexOf('_', languageIndex + 1);
		String country = null;
		if (countryIndex == -1) {
			country = localeString.substring(languageIndex + 1);
			return new Locale(language, country);
		} else {
			country = localeString.substring(languageIndex + 1, countryIndex);
			String variant = localeString.substring(countryIndex + 1);
			return new Locale(language, country, variant);
		}
	}

	private void initLocale(ProcessInstance processInstance) {
		if (hasText(localeAttributeKey)) {
			ProcessInstanceAttribute attr = processInstance.findAttributeByKey(localeAttributeKey);
			if (attr != null && attr instanceof ProcessInstanceSimpleAttribute) {
				locale = getLocaleFromString(((ProcessInstanceSimpleAttribute)attr).getValue());
			}
		}
	}

	private void saveReport(ProcessInstance processInstance, byte[] report) {
		Thread t = Thread.currentThread();
		ClassLoader previousLoader = t.getContextClassLoader();
		try {
			ClassLoader newClassLoader = ProcessToolContext.Util.getThreadProcessToolContext()
					.getRegistry().getModelAwareClassLoader(getClass().getClassLoader());
			t.setContextClassLoader(newClassLoader);
			sessionFacade = new CmisAtomSessionFacade(repositoryUser, repositoryPassword, repositoryAtomUrl,
			                                          repositoryId);
			mainFolder = sessionFacade.createFolderIfNecessary(newFolderPrefix + processInstance.getInternalId(),
			                                                   rootFolderPath);
			if (StringUtil.hasText(subFolder))
				mainFolder = sessionFacade.createFolderIfNecessary(subFolder, mainFolder.getPath());

			outputFileName = outputFileName.replace("#{processId}", String.valueOf(processInstance.getInternalId()));
			outputFileName = outputFileName.replace("#{date}", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
			outputFileName = outputFileName.replace("#{datetime}", new SimpleDateFormat("dd.MM.yyyy HH.mm").format(new Date()));
			
			if(POPUP_ONCE.equalsIgnoreCase(popup))
				outputFileName += "__POPUP_" + POPUP_ONCE + "__";
			
			if(POPUP_ALWAYS.equalsIgnoreCase(popup))
				outputFileName += "__POPUP_" + POPUP_ALWAYS + "__";
			
			sessionFacade.uploadDocument(outputFileName, mainFolder, report, mimeType, null);
			logger.info("MAIN_FOLDER_PATH: " + mainFolder.getPath());
		} finally {
			t.setContextClassLoader(previousLoader);
		}
	}

	protected byte[] buildReport(ProcessInstance processInstance) throws ReportException {
		// LOAD TEMPLATE
		ReportTemplate template = new ReportDAO().loadByName(reportName);
		if (template == null)
			throw new ReportException("Report template does not exist!");
		// COMPILE REPORT
		JasperReport jasperReport = null;
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		try {
			ByteArrayInputStream contentInputStream = getContentInputStream(template.getContent());
			Thread t = Thread.currentThread();
			ClassLoader previousLoader = t.getContextClassLoader();
			try {
				ClassLoader newClassLoader = ctx.getRegistry().getModelAwareClassLoader(getClass().getClassLoader());
				t.setContextClassLoader(newClassLoader);
				jasperReport = JasperCompileManager.compileReport(contentInputStream);
			} finally {
				t.setContextClassLoader(previousLoader);
			}
		} catch (Exception e) {
			throw new ReportException("Report compilation failed!", e);
		}


		// PREPARE INPUT
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, DATETIME_PATTERN);
		parameters.put(JRParameter.REPORT_LOCALE, locale);
		parameters.put(PROCESS_CONTEXT_KEY, ctx);

		// FILL REPORT
		JasperPrint jasperPrint = null;
		try {
			Thread t = Thread.currentThread();
			ClassLoader previousLoader = t.getContextClassLoader();
			try {
				ClassLoader newClassLoader = ctx
						.getRegistry().getModelAwareClassLoader(getClass().getClassLoader());
				t.setContextClassLoader(newClassLoader);
				parameters.put(DICTIONARY_HELPER, new DictionaryHelperImpl(processInstance));
				if (jasperReport.getQuery() != null && jasperReport.getQuery().getLanguage().equals("xPath")) {
					XStream xs = new XStream();
					xs.registerConverter(new MyPersistentSetConverter(xs.getMapper()), XStream.PRIORITY_VERY_HIGH);
					xs.omitField(ProcessInstance.class, "definition");
					xs.omitField(ProcessInstance.class, "processLogs");
					String s = xs.toXML(processInstance);

					jasperPrint = JasperFillManager.fillReport(jasperReport,
					                                           parameters,
					                                           new JRXmlDataSource(
							                                           new ByteArrayInputStream(s.getBytes())
					                                           ));
				} else if (jasperReport.getQuery() != null && hasText(jasperReport.getQuery().getText())) {
					parameters.put(PROCESS_INSTANCE_ID_KEY, String.valueOf(processInstance.getId()));
					jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ctx.getHibernateSession().connection());
				} else {
					parameters.put(PROCESS_INSTANCE_KEY, processInstance);
					jasperPrint = JasperFillManager.fillReport(jasperReport,
					                                           parameters,
					                                           new JREmptyDataSource());

				}
			} finally {
				t.setContextClassLoader(previousLoader);
			}
		} catch (Exception e) {
			throw new ReportException("Report filling failed!", e);
		}

		// EXPORT REPORT
		byte[] report = null;
		try {
			Thread t = Thread.currentThread();
			ClassLoader previousLoader = t.getContextClassLoader();
			try {
				ClassLoader newClassLoader = ctx
						.getRegistry().getModelAwareClassLoader(getClass().getClassLoader());
				t.setContextClassLoader(newClassLoader);
				report = exportReport(jasperPrint, format, encoding);
			} finally {
				t.setContextClassLoader(previousLoader);
			}
		} catch (Exception e) {
			throw new ReportException("Report export failed!", e);
		}
		return report;
	}

	private ByteArrayInputStream getContentInputStream(String content) {
		try {
			return new ByteArrayInputStream(Base64.decodeBase64(content.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private final String FIELD_DELIMITER = ";";

	private final String RECORD_DELIMITER = "\n\r";

	public byte[] exportReport(JasperPrint jasperPrint, String outputFormat, String characterEncoding)
			throws ReportException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JRExporter exporter;

			if ("PDF".equalsIgnoreCase(outputFormat)) {
				exporter = new JRPdfExporter();
				exporter.setParameter(JRPdfExporterParameter.CHARACTER_ENCODING, characterEncoding);
//				exporter.setParameter(JRPdfExporterParameter.CLASS_LOADER, getClass().getClassLoader());
			} else if ("HTML".equalsIgnoreCase(outputFormat)) {
				exporter = new JRHtmlExporter();
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRHtmlExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
			} else if ("XLS".equalsIgnoreCase(outputFormat)) {
				exporter = new JRXlsExporter();
				exporter.setParameter(JRXlsExporterParameter.CHARACTER_ENCODING, characterEncoding);
				exporter.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
				exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
				exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
			} else if ("CSV".equalsIgnoreCase(outputFormat)) {
				exporter = new JRCsvExporter();
				exporter.setParameter(JRCsvExporterParameter.CHARACTER_ENCODING, characterEncoding);
				exporter.setParameter(JRCsvExporterParameter.RECORD_DELIMITER, RECORD_DELIMITER);
				exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, FIELD_DELIMITER);
			} else {
				String message = "Invalid report type. Permitted types are: HTML, PDF, XLS, CSV";
				throw new ReportException(message);
			}

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bos);
			exporter.exportReport();

			return bos.toByteArray();
		} catch (JRException e) {
			throw new ReportException(e.getMessage(), e);
		}
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setRepositoryAtomUrl(String repositoryAtomUrl) {
		this.repositoryAtomUrl = repositoryAtomUrl;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void setRepositoryUser(String repositoryUser) {
		this.repositoryUser = repositoryUser;
	}

	public void setRepositoryPassword(String repositoryPassword) {
		this.repositoryPassword = repositoryPassword;
	}

	public void setRootFolderPath(String rootFolderPath) {
		this.rootFolderPath = rootFolderPath;
	}

	public void setSubFolder(String subFolder) {
		this.subFolder = subFolder;
	}

	public void setNewFolderPrefix(String newFolderPrefix) {
		this.newFolderPrefix = newFolderPrefix;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setPopup(String popup) {
		this.popup = popup;
	}

}
