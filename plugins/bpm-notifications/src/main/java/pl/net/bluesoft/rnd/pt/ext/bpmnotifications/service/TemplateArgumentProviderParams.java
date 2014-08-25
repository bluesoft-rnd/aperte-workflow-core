package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * User: POlszewski
 * Date: 2012-07-10
 * Time: 22:21
 */
public class TemplateArgumentProviderParams {
	private ProcessInstance processInstance;

    private IAttributesProvider attributesProvider;

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

    public IAttributesProvider getAttributesProvider() {
        return attributesProvider;
    }

    public void setAttributesProvider(IAttributesProvider attributesProvider) {
        this.attributesProvider = attributesProvider;
    }
}
