package pl.net.bluesoft.rnd.processtool.portlets.activity;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityMainPane;

import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.REQUEST_PARAMETER_TASK_ID;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivityPortletApplication extends GenericVaadinPortlet2BpmApplication {

	ActivityMainPane amp;

	public ActivityPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() {
        amp = new ActivityMainPane(ActivityPortletApplication.this, ActivityPortletApplication.this, bpmSession);
        addListener(new RequestParameterListener(REQUEST_PARAMETER_TASK_ID) {
            @Override
            public void handleRequestParameters(Map<String, String[]> parameters) {
                String[] values = parameters.get(REQUEST_PARAMETER_TASK_ID);
                String bpmTaskId = values[0];
                amp.displayTaskById(bpmTaskId);
            }
        });
		getMainWindow().setContent(amp);
	}

	@Override
	protected void renderPortlet() {

	}
}
