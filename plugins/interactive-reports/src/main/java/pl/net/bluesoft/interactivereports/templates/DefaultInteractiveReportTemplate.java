package pl.net.bluesoft.interactivereports.templates;

import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * User: POlszewski
 * Date: 2014-06-25
 */
public abstract class DefaultInteractiveReportTemplate implements InteractiveReportTemplate {
	private final String name;

	protected DefaultInteractiveReportTemplate(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isAvailable(UserData user) {
		return true;
	}
}
