package org.aperteworkflow.sample.process.incident.steps;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

import java.util.Map;

@AliasName(name = "SampleCustomStep")
public class SampleCustomStep implements ProcessToolProcessStep{
    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception {
        ProcessInstance processInstance = bpmStep.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		System.out.println("Doing custom things ...");
		
        //Here you can perform automatic step operations
        
        return STATUS_OK;
    }
}
