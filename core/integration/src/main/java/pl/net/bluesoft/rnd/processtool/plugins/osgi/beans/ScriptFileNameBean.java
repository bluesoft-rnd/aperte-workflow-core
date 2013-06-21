package pl.net.bluesoft.rnd.processtool.plugins.osgi.beans;

import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetJavaScriptProvider;

public class ScriptFileNameBean 
{
	public static final String FILE_PROVIDER_CLASS = FileWidgetJavaScriptProvider.class.getName();
	private static final String PROVIDER_CLASS_PARAMETER = "providerclass";
	
	private String fileNameToProcess;
	private String fileName;
	private String providerClass = FILE_PROVIDER_CLASS;
	
	public ScriptFileNameBean(String fileNameToProcess)
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
			/* filename */
			if(paramterConfig.length == 1)
			{
				if(this.fileName == null)
					this.fileName = paramterConfig[0];
			}
			/* Other paramters */
			else
			{
				String paramterName = paramterConfig[0];
				String paramterValue = paramterConfig[1];
				
				if(PROVIDER_CLASS_PARAMETER.equals(paramterName.toLowerCase()))
				{
					if(FILE_PROVIDER_CLASS.equals(paramterValue))
						this.providerClass = FILE_PROVIDER_CLASS;
					else
						this.providerClass = paramterValue;
				}
			}
		}
	}

	public String getProviderClass() {
		return providerClass;
	}

	public String getFileName() {
		return fileName;
	}

}
