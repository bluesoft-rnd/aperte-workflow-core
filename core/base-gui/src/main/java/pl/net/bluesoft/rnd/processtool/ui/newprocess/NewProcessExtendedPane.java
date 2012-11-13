package pl.net.bluesoft.rnd.processtool.ui.newprocess;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ChameleonTheme;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityMainPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.processtool.ui.process.WindowProcessDataDisplayContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class NewProcessExtendedPane extends VerticalLayout implements Refreshable, ClickListener, ValueChangeListener {
	public class ProcessStartListener implements ClickListener {
		@Override
		public void buttonClick(ClickEvent event) {
			if(processesSelect.getValue() == null)
				return;
		}
	}

	private ProcessToolBpmSession session;
	private ActivityMainPane activityMainPane;
	private I18NSource i18NSource;

	private ListSelect processesSelect;
	private Label title;
	private IndexedContainer processesContainer;
	private Window processesPopup;
	private Button firstButton;
	private Button secondButton;
	private VerticalLayout descriptionPanel;
	private Map<ProcessDefinitionConfig, Embedded> logoEmbeddedCache = new HashMap<ProcessDefinitionConfig, Embedded>();
	private Map<ProcessDefinitionConfig, Resource> logoResourceCache = new HashMap<ProcessDefinitionConfig, Resource>();
	private VerticalLayout processesPopupLayout;
	private Panel processesPopupPanel;
	private Embedded defaultLogoEmbedded;
	private Resource defaultLogoResource;
	private Label selectedTitleLabel;
	private HorizontalLayout logoWrapper;
	private Label descriptionLabel;
	private Embedded previousLogo;
	private Label attachListener;
	private Label attachHandler;
	private ProgressIndicator progressBar;

	public NewProcessExtendedPane(final ProcessToolBpmSession session,
			final I18NSource i18NSource,
			final ActivityMainPane activityMainPane) {
		this.activityMainPane = activityMainPane;
		this.session = session;
		this.i18NSource = i18NSource;

		firstButton = new Button(getMessage("newProcess.start-simple"), this);
		firstButton.setWidth("100%");
		// firstButton.addStyleName("default");
		//		button.addStyleName("default");
		addComponent(firstButton);
		setComponentAlignment(firstButton, Alignment.MIDDLE_CENTER);

		processesPopup = new Window();
		processesPopup.setModal(true);
		processesPopup.setBorder(0);
		processesPopup.setClosable(true);
		processesPopup.setWidth(600, Sizeable.UNITS_PIXELS);
		processesPopup.setCloseShortcut(ShortcutAction.KeyCode.ESCAPE);
		processesPopup.setImmediate(true);

		processesSelect = new ListSelect();
		processesSelect.setRows(7);
		//		processesSelect.setHeight(40, Sizeable.UNITS_PIXELS);
		processesSelect.setNullSelectionAllowed(false);
		processesSelect.setWidth("100%");
		processesSelect.setImmediate(true);
		processesSelect.addListener(this);

		descriptionPanel = new VerticalLayout();

		processesPopupPanel = new Panel();
		//		processesPopupPanel.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
		processesPopupPanel.addStyleName(ChameleonTheme.PANEL_LIGHT);
		processesPopup.addComponent(processesPopupPanel);

		processesPopupLayout = new VerticalLayout();
		processesPopupLayout.setSpacing(true);
		processesPopupLayout.addComponent(VaadinUtility.horizontalLayout(new Label(getMessage("newProcess.caption-simple"), Label.CONTENT_XHTML), refreshIcon(activityMainPane.getApplication(), this)));
		processesPopupLayout.addComponent(processesSelect);
		processesPopupLayout.addComponent(descriptionPanel);
		processesPopupPanel.addComponent(processesPopupLayout);

		processesContainer = new IndexedContainer();
		processesContainer.addContainerProperty("name", String.class, "");
		processesContainer.addContainerProperty("logo", Resource.class, defaultLogoResource);
		processesSelect.setContainerDataSource(processesContainer);
		processesSelect.setItemIconPropertyId("logo");

		processesSelect.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
		processesSelect.setItemCaptionPropertyId("name");

		defaultLogoResource = VaadinUtility.imageResource(activityMainPane.getApplication(), "aperte-logo.png");
		defaultLogoEmbedded = new Embedded(null,  defaultLogoResource);

		setSpacing(true);
		setMargin(new MarginInfo(true, false, true, false));

		refreshData();
	}

	@Override
	public void refreshData() {
		processesContainer.removeAllItems();
		processesSelect.setVisible(true);
		processesSelect.setValue(null);
		logoEmbeddedCache.clear();
		//		title.setValue(getMessage("newProcess.caption-simple"));
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		List<ProcessDefinitionConfig> orderedByProcessDescr = from(session.getAvailableConfigurations(ctx))
				.orderBy(new F<ProcessDefinitionConfig, String>() {
					@Override
					public String invoke(ProcessDefinitionConfig pdc) {
						return getMessage(pdc.getDescription()).toLowerCase(i18NSource.getLocale());
					}
				})
				.toList();

		for (final ProcessDefinitionConfig definition : orderedByProcessDescr) {
			if (session.hasPermissionsForDefinitionConfig(definition)) {
				if(!logoResourceCache.containsKey(definition)){
					logoResourceCache.put(definition, definition.getProcessLogo() == null ? defaultLogoResource
							: new StreamResource(new StreamSource() {
								@Override
								public InputStream getStream() {
									return new ByteArrayInputStream(definition.getProcessLogo());
								}
							}, definition.getBpmDefinitionKey() + "_logo.png", activityMainPane.getApplication())
							);
				}

				processesContainer.addItem(definition);
				processesContainer.getItem(definition).getItemProperty("name").setValue(getMessage(definition.getDescription()));
				processesContainer.getItem(definition).getItemProperty("logo").setValue(logoResourceCache.get(definition));

				if(processesSelect.getValue() == null){
					processesSelect.setValue(definition);
				}
			}
		}
	}

	private String getMessage(String s) {
		return i18NSource.getMessage(s);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == firstButton){

			if(!getApplication().getMainWindow().getChildWindows().contains(processesPopup))
				getApplication().getMainWindow().addWindow(processesPopup);

			if(progressBar != null)
				processesPopup.removeComponent(progressBar);
			processesSelect.setVisible(true);
			processesPopup.setVisible(true);
			processesPopup.setEnabled(true);
			processesPopup.focus();
			processesSelect.focus();

		} else if (event.getButton() == secondButton){
			if(processesSelect.getValue() == null)
				return;

			progressBar = new ProgressIndicator();
			
			processesSelect.setVisible(false);

			progressBar.setCaption(i18NSource.getMessage("activity.starting"));
			progressBar.setIndeterminate(true);
			progressBar.setPollingInterval(500);
			processesPopup.addComponent(progressBar);
			
			getApplication().getContext().addTransactionListener(new TransactionListener() {
				private boolean started = false;
				private int counter = 2;

				@Override
				public void transactionStart(Application application, Object transactionData) {
				}

				@Override
				synchronized public void transactionEnd(Application application, Object transactionData) {
					if (!started) {
						counter--;
						if(counter == 0){
							processesPopup.removeComponent(progressBar);
							processesPopup.setVisible(false);
							processesPopup.setEnabled(false);
							runProcess(getSelectedDefinition().getBpmDefinitionKey());
							started = true;
							getApplication().getContext().removeTransactionListener(this);
						}
					}

				}
			});
		}

	}

	protected ProcessDefinitionConfig getSelectedDefinition() {
		return (ProcessDefinitionConfig)processesSelect.getValue();
	}

	private void runProcess(final String bpmDefinitionId) {
		withErrorHandling(getApplication(), new Runnable() {
			@Override
			public void run() {
				ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
				ProcessDefinitionConfig cfg = ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(bpmDefinitionId);
				ProcessInstance instance = session.createProcessInstance(cfg, null, ctx, null, null, "portlet", null);
				VaadinUtility.informationNotification(activityMainPane.getApplication(), getMessage("newProcess.started"), 1000);
				getWindow().executeJavaScript("Liferay.trigger('processtool.bpm.newProcess', '" + instance.getInternalId() + "');");
				getWindow().executeJavaScript("vaadin.forceSync();");

				List<BpmTask> tasks = session.findUserTasks(instance, ctx);
				if (!tasks.isEmpty()) {
					BpmTask task = tasks.get(0);
					if (activityMainPane != null) {
						activityMainPane.displayProcessData(task, session);
					}
					else {
						Window w = new Window(instance.getInternalId());
						w.setContent(new ProcessDataPane(getApplication(), session, i18NSource, task, new WindowProcessDataDisplayContext(w)));
						w.center();
						getWindow().addWindow(w);
						w.focus();
					}
				}

				else if (activityMainPane != null) {
					activityMainPane.reloadCurrentViewData();
				}
			}
		});
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if(processesSelect.getValue() == null)
			return;
		final ProcessDefinitionConfig definition = getSelectedDefinition();

		if(!logoEmbeddedCache.containsKey(definition)){
			logoEmbeddedCache.put(definition, new Embedded(null, logoResourceCache.get(definition)));
		}
		Embedded logo = logoEmbeddedCache.get(definition);

		if(descriptionPanel.getComponentCount() == 0){
			secondButton = VaadinUtility.link(getMessage("newProcess.start-task"), this);
			secondButton.setImmediate(true);
			secondButton.addListener(new ProcessStartListener());

			selectedTitleLabel = new Label(getMessage(definition.getDescription()));
			selectedTitleLabel.addStyleName("h3 color");

			descriptionPanel.addComponent(logoWrapper = VaadinUtility.horizontalLayout(Alignment.MIDDLE_LEFT, logo, selectedTitleLabel));
			logoWrapper.setHeight(36, Sizeable.UNITS_PIXELS);

			HorizontalLayout hl = new HorizontalLayout();
			hl.addComponent(descriptionLabel = new Label(nvl(getMessage(definition.getComment()), ""), Label.CONTENT_XHTML) {{
				setWidth("100%");
			}});
			hl.addComponent(secondButton);
			hl.setExpandRatio(hl.getComponent(0), 1.0f);
			hl.setSpacing(true);
			hl.setWidth("100%");
			hl.setComponentAlignment(secondButton, Alignment.BOTTOM_RIGHT);
			descriptionPanel.addComponent(hl);
		} else {
			logoWrapper.addComponentAsFirst(logo);
			logoWrapper.removeComponent(previousLogo);
			selectedTitleLabel.setValue(getMessage(definition.getDescription()));
			//			selectedTitleLabel.setIcon(logoResourceCache.get(definition));
			descriptionLabel.setValue(nvl(getMessage(definition.getComment()), ""));
		}
		previousLogo = logo;
	}
}
