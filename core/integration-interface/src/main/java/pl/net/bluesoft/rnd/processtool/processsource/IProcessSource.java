package pl.net.bluesoft.rnd.processtool.processsource;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Process source api
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IProcessSource 
{
	Collection<ProcessInstance> getUserProcessSoruces(String userLogin);

}
