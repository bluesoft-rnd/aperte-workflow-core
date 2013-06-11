package pl.net.bluesoft.rnd.processtool.plugins.osgi.beans;

import pl.net.bluesoft.rnd.processtool.ui.impl.FileWidgetContentProvider;

public class HtmlFileNameBean 
{
	public static final String FILE_PROVIDER_CLASS = FileWidgetContentProvider.class.getName();
	private static final String PROVIDER_CLASS_PARAMETER = "providerclass";
	private static final String FILE_NAME_PARAMETER = "fileName";
	private static final String WIDGET_NAME_PARAMETER = "widgetName";
	
	private String fileNameToProcess;
	private String fileName;
	private String widgetName;
	private String providerClass = FILE_PROVIDER_CLASS;
	
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
					this.providerClass = FILE_PROVIDER_CLASS;
				else
					this.providerClass = paramterValue;
			}
			else if(FILE_NAME_PARAMETER.equals(paramterName))
			{
				this.fileName = paramterValue;
			}
			else if(WIDGET_NAME_PARAMETER.equals(paramterName))
			{
				this.widgetName = paramterValue;
			}

		}
	}

	public String getProviderClass() {
		return providerClass;
	}

	public String getFileName() {
		return fileName;
	}

	public String getWidgetName() {
		return widgetName;
	}



}
