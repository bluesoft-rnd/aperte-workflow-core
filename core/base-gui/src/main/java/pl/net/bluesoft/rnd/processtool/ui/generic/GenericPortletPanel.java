package pl.net.bluesoft.rnd.processtool.ui.generic;

import com.vaadin.Application;
import com.vaadin.ui.*;
import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.IViewRegistry;
import org.aperteworkflow.ui.view.RenderParams;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.ArrayList;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 10:27
 */
public abstract class GenericPortletPanel extends VerticalLayout {
	protected Application application;
	protected I18NSource i18NSource;
	protected ProcessToolBpmSession bpmSession;
	protected String portletKey;

	protected GenericPortletPanel(Application application, I18NSource i18NSource, ProcessToolBpmSession bpmSession,
								  String portletKey) {
		this.application = application;
		this.i18NSource = i18NSource;
		this.bpmSession = bpmSession;
		this.portletKey = portletKey;
	}

	protected void buildView() {
		List<GenericPortletViewRenderer> permittedRenderers = getPermittedRenderers();

		if (permittedRenderers.size() > 1) {
			addComponent(renderTabSheet(permittedRenderers));
		}
		else {
			addComponent(renderVerticalLayout(permittedRenderers));
		}
	}

	protected Component renderVerticalLayout(List<GenericPortletViewRenderer> permittedRenderers) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setWidth("100%");

		for (GenericPortletViewRenderer renderer : permittedRenderers) {
			RenderParams params = createParams();
			Object gui = renderer.render(params);

			if (gui instanceof Component) {
				layout.addComponent(addControls((Component)gui, renderer));
			}
		}
		return layout;
	}

	protected Component renderTabSheet(List<GenericPortletViewRenderer> permittedRenderers) {
		TabSheet tabSheet = new TabSheet();
		tabSheet.setWidth("100%");

		for (GenericPortletViewRenderer renderer : permittedRenderers) {
			RenderParams params = createParams();
			Object gui = renderer.render(params);

			if (gui instanceof Component) {
				tabSheet.addTab(addControls((Component)gui, renderer), i18NSource.getMessage(renderer.getName()));
			}
		}
		return tabSheet;
	}

	private RenderParams createParams() {
		RenderParams params = new RenderParams();
		params.setI18NSource(i18NSource);
		return params;
	}

	private Component addControls(Component gui, GenericPortletViewRenderer renderer) {
		Label titleLabel = new Label(i18NSource.getMessage(renderer.getName()));
		titleLabel.addStyleName("h1 color processtool-title");
		titleLabel.setWidth("100%");

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.setSpacing(true);

		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);

		layout.addComponent(horizontalLayout(titleLabel,
				gui instanceof VaadinUtility.Refreshable
						? getRefreshButton((VaadinUtility.Refreshable)gui)
						: new Label()));
		layout.addComponent(headerLayout);
		layout.addComponent(gui);

		return layout;
	}

	private Component getRefreshButton(VaadinUtility.Refreshable refreshable) {
		Button button = refreshIcon(application, refreshable);
		return button;
	}

	protected List<GenericPortletViewRenderer> getPermittedRenderers() {
		IViewRegistry viewRegistry = getRegistry().getRegisteredService(IViewRegistry.class);
		List<GenericPortletViewRenderer> permittedViews = new ArrayList<GenericPortletViewRenderer>();

		for (GenericPortletViewRenderer renderer : viewRegistry.getGenericPortletViews(portletKey)) {
			if (isPermitted(renderer)) {
				permittedViews.add(renderer);
			}
		}

		permittedViews = arrangeViews(permittedViews);

		return permittedViews;
	}

	protected List<GenericPortletViewRenderer> arrangeViews(List<GenericPortletViewRenderer> permittedViews) {
		return from(permittedViews).orderBy(new F<GenericPortletViewRenderer, Comparable>() {
			@Override
			public Comparable invoke(GenericPortletViewRenderer x) {
				return x.getPosition();
			}
		}).toList();
	}

	protected boolean isPermitted(GenericPortletViewRenderer renderer) {
		return hasRoles(renderer.getRequiredRoles());
	}

	protected boolean hasRoles(String[] requiredRoles) {
		if (requiredRoles != null) {
			for (String role : requiredRoles) {
				if (!((GenericVaadinPortlet2BpmApplication)application).hasMatchingRole(role)) {
					return false;
				}
			}
		}
		return true;
	}
}
