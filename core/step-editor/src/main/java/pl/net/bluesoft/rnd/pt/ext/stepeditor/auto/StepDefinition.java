package pl.net.bluesoft.rnd.pt.ext.stepeditor.auto;

import java.util.ArrayList;
import java.util.List;

public class StepDefinition {
	private String name;
	private List<StepParameter> parameters = new ArrayList<StepParameter>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<StepParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<StepParameter> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(StepParameter p) {
		parameters.add(p);
	}
}
