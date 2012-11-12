package pl.net.bluesoft.rnd.processtool.portlets.generic;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.ViewRegistry;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.generic.GenericUserPortletPanel;
import pl.net.bluesoft.rnd.processtool.ui.generic.GenericUserPortletSettingsPanel;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.rnd.util.i18n.impl.DefaultI18NSource;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import java.util.Collection;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 09:22
 */
public class GenericUserPortletApplication extends GenericVaadinPortlet2BpmApplication {
	private static final String SELECTED_VIEWS = "selected.views";

	private PortletPreferences preferences = null;
	private String[] viewKeys;

	@Override
	protected void initializePortlet() {
	}

	@Override
	protected void renderPortlet() {
		getMainWindow().setContent(new GenericUserPortletPanel(this, this, bpmSession, this, PortletKeys.USER, viewKeys));
	}

	@Override
	public void handleRenderRequest(RenderRequest renderRequest, RenderResponse response, Window window) {
		this.preferences = renderRequest.getPreferences();
		PortletMode portletMode = renderRequest.getPortletMode();

		if (portletMode.equals(PortletMode.VIEW)) {
			String[] viewKeys = getSelectedViewKeys();
			if (viewKeys.length == 0) {
				getMainWindow().removeAllComponents();
				getMainWindow().addComponent(new Label("Please configure this portlet."));
			}
			else {
				this.viewKeys = viewKeys;
				super.handleRenderRequest(renderRequest, response, window);
			}
		}
		else if (portletMode.equals(PortletMode.EDIT)) {
			locale = renderRequest.getLocale();
			I18NSource i18NSource = I18NSourceFactory.createI18NSource(locale);
			final GenericUserPortletSettingsPanel editPane = new GenericUserPortletSettingsPanel(
					i18NSource, getSelectedViewKeys(), getRegisteredViews());
			editPane.addListener(new GenericUserPortletSettingsPanel.SaveListener() {
				@Override
				public void onSave() {
					saveSelectedViewKeys(editPane.getSelectedViewKeys());
				}
			});
			getMainWindow().setContent(new VerticalLayout());
			getMainWindow().addComponent(editPane);
		}
	}

	private void saveSelectedViewKeys(String[] viewKeys) {
		try {
			if (viewKeys == null || viewKeys.length == 0) {
				preferences.reset(SELECTED_VIEWS);
			}
			else {
				preferences.setValue(SELECTED_VIEWS, from(viewKeys).toString(","));
			}
			preferences.store();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String[] getSelectedViewKeys() {
		String val = preferences.getValue(SELECTED_VIEWS, null);
		if (val != null) {
			return val.split(",");
		}
		return new String[]{};
	}

	private Collection<GenericPortletViewRenderer> getRegisteredViews() {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ViewRegistry viewRegistry = ctx.getRegistry().getRegisteredService(ViewRegistry.class);
		return viewRegistry.getGenericPortletViews(PortletKeys.USER);
	}
}
