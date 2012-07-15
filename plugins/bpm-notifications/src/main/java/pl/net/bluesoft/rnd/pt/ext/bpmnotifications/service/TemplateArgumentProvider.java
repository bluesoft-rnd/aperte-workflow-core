package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-07-10
 * Time: 22:11
 */
public interface TemplateArgumentProvider {
	String getName();

	void getArguments(Map<String, String> arguments, TemplateArgumentProviderParams params);
}
