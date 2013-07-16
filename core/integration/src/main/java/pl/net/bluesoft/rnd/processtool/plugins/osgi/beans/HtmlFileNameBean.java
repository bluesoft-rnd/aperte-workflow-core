package pl.net.bluesoft.rnd.processtool.plugins.osgi.beans;

import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.MockWidgetValidator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;

public class HtmlFileNameBean 
{
	public static final String FILE_PROVIDER_CLASS = FileWidgetContentProvider.class.getName();
	public static final String DATA_HANDLER_CLASS = SimpleWidgetDataHandler.class.getName();
	public static final String VALIDATOR_CLASS = MockWidgetValidator.class.getName();
	
	private static final String PROVIDER_CLASS_PARAMETER = "providerclass";
	private static final String FILE_NAME_PARAMETER = "fileName";
	private static final String WIDGET_NAME_PARAMETER = "widgetName";
	private static final String DATA_HANDLER_CLASS_PARAMETER = "dataHandlerClass";
	private static final String VALIDATOR_CLASS_PARAMETER = "validatorClass";
	
	private String fileNameToProcess;
	private String fileName;
	private String widgetName;
	private String providerClass = FILE_PROVIDER_CLASS;
	private String dataHandlerClass = DATA_HANDLER_CLASS;
	private String validatorClass = VALIDATOR_CLASS;
	
	public HtmlFileNameBean(String fileNameToProcess)
	{
		this.fileNameToProcess = fileNameToProcess;
		init();
	}
	
	private void init()
	{
		String[] configuration = this.fileNameToProcess.split(";");
		for(String parameter: configuration)
		{
			String[] paramterConfig = parameter.split(":=");

			String paramterName = paramterConfig[0];
			String paramterValue = paramterConfig[1];
			
			if(PROVIDER_CLASS_PARAMETER.equals(paramterName))
			{
				if(paramterValue == null || paramterValue.isEmpty() || "file".equals(paramterValue))
					this.setProviderClass(FILE_PROVIDER_CLASS);
				else
					this.setProviderClass(paramterValue);
			}
			else if(DATA_HANDLER_CLASS_PARAMETER.equals(paramterName))
			{
				if(paramterValue == null || paramterValue.isEmpty() || "simple".equals(paramterValue))
					this.setDataHandlerClass(DATA_HANDLER_CLASS);
				else
					this.setDataHandlerClass(paramterValue);
			}
			else if(VALIDATOR_CLASS_PARAMETER.equals(paramterName))
			{
				if(paramterValue == null || paramterValue.isEmpty() || "simple".equals(paramterValue))
					this.setValidatorClass(VALIDATOR_CLASS);
				else
					this.setValidatorClass(paramterValue);
			}
			else if(FILE_NAME_PARAMETER.equals(paramterName))
			{
				this.setFileName(paramterValue);
			}
			else if(WIDGET_NAME_PARAMETER.equals(paramterName))
			{
				this.setWidgetName(paramterValue);
			}

		}
	}


	public String getDataHandlerClass() {
		return dataHandlerClass;
	}

	private void setDataHandlerClass(String dataHandlerClass) {
		this.dataHandlerClass = dataHandlerClass;
	}

	public String getFileName() {
		return fileName;
	}

	private void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getWidgetName() {
		return widgetName;
	}

	private void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	public String getProviderClass() {
		return providerClass;
	}

	private void setProviderClass(String providerClass) {
		this.providerClass = providerClass;
	}

	public String getValidatorClass() {
		return validatorClass;
	}

	private void setValidatorClass(String validatorClass) {
		this.validatorClass = validatorClass;
	}



}
