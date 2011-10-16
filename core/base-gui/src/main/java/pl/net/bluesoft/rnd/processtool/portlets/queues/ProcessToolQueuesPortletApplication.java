package pl.net.bluesoft.rnd.processtool.portlets.queues;

import pl.net.bluesoft.rnd.processtool.ui.queues.QueuesMainPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolQueuesPortletApplication extends GenericVaadinPortlet2BpmApplication {

	QueuesMainPane tmp;

	public ProcessToolQueuesPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() {
		tmp = new QueuesMainPane(this, bpmSession, this);
		getMainWindow().setContent(tmp);
	}

	@Override
	protected void renderPortlet() {

		
	}
}
