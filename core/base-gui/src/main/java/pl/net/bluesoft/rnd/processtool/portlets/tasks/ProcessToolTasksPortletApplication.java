package pl.net.bluesoft.rnd.processtool.portlets.tasks;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolTasksPortletApplication extends GenericVaadinPortlet2BpmApplication {

	TasksMainPane tmp;

	public ProcessToolTasksPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() {
		tmp = new TasksMainPane(this, bpmSession, this);
		getMainWindow().setContent(tmp);
	}

	@Override
	protected void renderPortlet() {

	}
}
