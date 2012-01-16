package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;


public abstract class AbstractStepEditorWindow {

	protected StepEditorApplication application;
	protected String jsonConfig;
	protected String url;
	protected String stepName;
	
	public AbstractStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName) {
		this.application = application;
		this.jsonConfig = jsonConfig;
		this.url = url;
		this.stepName = stepName;
	}
	
	public abstract ComponentContainer init();
	public abstract Label getHeaderLabel();

	public String getJsonConfig() {
		return jsonConfig;
	}

	public void setJsonConfig(String jsonConfig) {
		this.jsonConfig = jsonConfig;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}
	
	
	
}
