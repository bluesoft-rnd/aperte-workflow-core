package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-07-10
 * Time: 22:11
 */
public interface TemplateArgumentProvider {
	String getName();

	void getArguments(Map<String, Object> arguments, TemplateArgumentProviderParams params);

	List<TemplateArgumentDescription> getArgumentDescriptions(I18NSource i18NSource);
}
