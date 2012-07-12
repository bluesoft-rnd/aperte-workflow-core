package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.HashMap;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Class packaging all returned process instancess with fast access by 
 * processId
 * 
 * @author Maciej Pawlak
 *
 */
public class ProcessInstancePackage 
{
	/** Key = processId */
	private Map<String, ProcessInstance> instances = new HashMap<String, ProcessInstance>();
	
	public ProcessInstance getByProcessId(String processId)
	{
		return instances.get(processId);
	}
	
	public void addProcessInstance(ProcessInstance processInstace)
	{
		instances.put(processInstace.getExternalKey(), processInstace);
	}
}
