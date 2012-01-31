package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;


public abstract class AbstractStepEditorWindow {

	protected StepEditorApplication application;
	protected String jsonConfig;
	protected String url;
	protected String stepType;
    protected String stepName;
	
	public AbstractStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		this.application = application;
		this.jsonConfig = jsonConfig;
		this.url = url;
		this.stepType = stepType;
        this.stepName = stepName;
	}
	
	public abstract ComponentContainer init();

    public abstract void save();

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

	public String getStepType() {
		return stepType;
	}

	public void setStepType(String stepType) {
		this.stepType = stepType;
	}

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
}
