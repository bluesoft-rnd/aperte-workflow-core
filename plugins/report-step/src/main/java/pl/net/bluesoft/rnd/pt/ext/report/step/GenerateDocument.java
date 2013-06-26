package pl.net.bluesoft.rnd.pt.ext.report.step;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.aperteworkflow.cmis.widget.CmisAtomSessionFacade;
import org.hibernate.Hibernate;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.ReportBuilder;



@AliasName(name = "GenerateDocumentStep")
/**
 * Step for generating reports using aperte reports and cmis interface
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class GenerateDocument implements ProcessToolProcessStep 
{ 
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

	/** Report name */
	@AutoWiredProperty
	private String reportName;
	
	/** Should step store output in repository in process folder? */
	@AutoWiredProperty(required=false)
	private Boolean storeInRepository;
	
	/** Should step print output to user? */
	@AutoWiredProperty(required=false)
	private Boolean showPrintedFile;

	/** Output format of the report */
	@AutoWiredProperty(required=false)
	private String format = "pdf";

	@AutoWiredProperty(required=false)
	private String outputFileName = "report";

	/** Use parent process attributes? */
	@AutoWiredProperty(required=false)
	private Boolean useParentProcessAttributes = true;


	public String invoke(BpmStep step, Map<String, String> params) throws Exception 
	{
		ProcessInstance processInstance = getProcessInstance(step);
		
		processReportName(processInstance);
		
		
		UserData userData = step.getProcessInstance().getCreator();

		byte[] report = generateReport(processInstance, userData);

		saveReport(processInstance, report);
		

		return STATUS_OK;

	}
	
	/** Get process from step instance */
	private ProcessInstance getProcessInstance(BpmStep step)
	{
		ProcessInstance processInstance = step.getProcessInstance();
		
		boolean useParentProcess = useParentProcessAttributes && processInstance.getParent() != null;
		
		if(useParentProcess)
			processInstance = processInstance.getParent();
		
		if(!Hibernate.isInitialized(processInstance))
				Hibernate.initialize(processInstance);
		
		return processInstance;
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



	private void saveReport(ProcessInstance processInstance, byte[] report) 
	{
		CmisAtomSessionFacade sessionFacade = new CmisAtomSessionFacade();
		
		Folder mainFolder = sessionFacade.getFolderForProcessInstance(processInstance);
		
		outputFileName += "." + format;
		outputFileName = outputFileName.replace("#{processId}", String.valueOf(processInstance.getInternalId()));
		outputFileName = outputFileName.replace("#{date}", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		outputFileName = outputFileName.replace("#{datetime}", new SimpleDateFormat("dd.MM.yyyy HH.mm").format(new Date()));
		

		
		String mimeType = "application/" + format.toLowerCase();

		sessionFacade.uploadDocument(outputFileName, mainFolder, report, mimeType, null);
		logger.info("MAIN_FOLDER_PATH: " + mainFolder.getPath());

	}
	
	private byte[] generateReport(ProcessInstance processInstance, UserData userData) 
	{
			byte[] data = new ReportBuilder(reportName)
				.setFormat(format.toString())
				.addParameter("processid", new BigDecimal(processInstance.getId()))
				.addParameter("userlogin", userData.getLogin())
				.getReportBytes();

			return data;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	
	/** Process report name as plain text or variable */
	private void processReportName(ProcessInstance processInstance)
	{
		if (reportName == null) 
			return;
		
		reportName = reportName.trim();
		
		if(reportName.matches("#\\{.*\\}"))
		{
        	String reportNameKey = reportName.replaceAll("#\\{(.*)\\}", "$1");
        	String newReportName = processInstance.getSimpleAttributeValue(reportNameKey);
        	
        	/* Try to evaluate variable from parent process */
        	if (newReportName == null && processInstance.getParent() != null) 
        		newReportName = processInstance.getParent().getSimpleAttributeValue(reportNameKey);
        	
    		if (newReportName != null) 
    			reportName = newReportName;
    		
        }
	}

}
