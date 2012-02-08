package pl.net.bluesoft.rnd.processtool.steps;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolProcessStep {
    String STATUS_OK = "OK";
    String STATUS_ERROR = "ERROR";
	String invoke(ProcessInstance processInstance, Map params) throws Exception;
}
