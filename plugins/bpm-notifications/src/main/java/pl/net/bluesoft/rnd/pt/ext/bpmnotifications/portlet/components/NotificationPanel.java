package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.*;
import org.hibernate.Hibernate;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationConfigDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationMailPropertiesDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationTemplateDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Pair;
import pl.net.bluesoft.util.lang.Tuple;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.Strings.hasText;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:23
 */
public class NotificationPanel extends ItemEditorLayout<BpmNotificationConfig> {
	private Select profileName;
	private Select templateName;
	private Select templateArgumentProvider;
	private CheckBox active;
	private CheckBox sendHtml;
	private TextField localeField;
	private TextField processTypeRegex;
	private TextField stateRegex;
	private TextField lastActionRegex;
	private CheckBox notifyOnProcessStart;
	private CheckBox notifyOnProcessEnd;
	private CheckBox onEnteringStep;
	private CheckBox skipNotificationWhenTriggeredByAssignee;
	private CheckBox notifyTaskAssignee;
	private TextField notifyEmailAddresses;
	private TextField notifyUserAttributes;
	private Button selectProcess;
	private Button selectState;
	private Button selectAction;

	private List<BpmNotificationMailProperties> mailProperties;
	private List<BpmNotificationTemplate> mailTemplates;
	private Collection<ProcessDefinitionConfig> processDefinitions;
	private Collection<TemplateArgumentProvider> templateArgumentProviders;

	public NotificationPanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		super(BpmNotificationConfig.class, i18NSource, registry);
		buildLayout();
	}

	@Override
	protected Component createItemDetailsLayout() {
		FormLayout formLayout = new FormLayout();

		formLayout.addComponent(profileName = select("Profil", 400));
		formLayout.addComponent(templateName = select("Szablon", 400));
		formLayout.addComponent(templateArgumentProvider = select("Dostawca parametrów", 400));
		formLayout.addComponent(active = checkBox("Aktywny"));
		formLayout.addComponent(sendHtml = checkBox("Wyślij jako HTML"));
		formLayout.addComponent(localeField = textField("Locale"));

		processTypeRegex = textField(null, 400);
		stateRegex = textField(null, 400);
		lastActionRegex = textField(null, 400);

		selectProcess = new Button("Wybierz");
		selectState = new Button("Wybierz");
		selectAction = new Button("Wybierz");

		selectProcess.addListener((Button.ClickListener)this);
		selectState.addListener((Button.ClickListener)this);
		selectAction.addListener((Button.ClickListener)this);

		formLayout.addComponent(hl("Proces", processTypeRegex, selectProcess));
		formLayout.addComponent(hl("Stan", stateRegex, selectState));
		formLayout.addComponent(hl("Ostatnia akcja", lastActionRegex, selectAction));

		formLayout.addComponent(notifyOnProcessStart = checkBox("Wyślij przy utworzeniu procesu"));
		formLayout.addComponent(notifyOnProcessEnd = checkBox("Wyślij przy zakończeniu procesu"));
		formLayout.addComponent(onEnteringStep = checkBox("Wyślij przy wejściu w krok (???)"));
		formLayout.addComponent(skipNotificationWhenTriggeredByAssignee = checkBox("Pomiń, gdy wyzwolone przez osobę przypisaną"));

		formLayout.addComponent(notifyTaskAssignee = checkBox("Powiadom osobę przypisaną"));
		formLayout.addComponent(notifyEmailAddresses = textField("Wyślij na adresy", 400));
		formLayout.addComponent(notifyUserAttributes = textField("Wyślij do użytkowników", 400));
		formLayout.addComponent(new Label(
				getMessage("Loginy użytkowników/atrybuty procesu ujęte w #{ } należy rozdzielić przecinkami.<br/>Np. <i>pracownik,#{owner}</i>"), Label.CONTENT_XHTML));

		return formLayout;
	}

	@Override
	protected void clearDetails() {
		profileName.setValue(null);
		templateName.setValue(null);
		templateArgumentProvider.setValue(null);
		active.setValue(null);
		sendHtml.setValue(null);
		localeField.setValue(null);
		processTypeRegex.setValue(null);
		stateRegex.setValue(null);
		lastActionRegex.setValue(null);
		notifyOnProcessStart.setValue(null);
		notifyOnProcessEnd.setValue(null);
		onEnteringStep.setValue(null);
		skipNotificationWhenTriggeredByAssignee.setValue(null);
		notifyTaskAssignee.setValue(null);
		notifyEmailAddresses.setValue(null);
		notifyUserAttributes.setValue(null);
	}

	@Override
	protected void loadDetails(BpmNotificationConfig item) {
		profileName.setValue(item.getProfileName());
		templateName.setValue(item.getTemplateName());
		templateArgumentProvider.setValue(item.getTemplateArgumentProvider());
		active.setValue(item.isActive());
		sendHtml.setValue(item.isSendHtml());
		localeField.setValue(item.getLocale());
		processTypeRegex.setValue(item.getProcessTypeRegex());
		stateRegex.setValue(item.getStateRegex());
		lastActionRegex.setValue(item.getLastActionRegex());
		notifyOnProcessStart.setValue(item.isNotifyOnProcessStart());
		notifyOnProcessEnd.setValue(item.isNotifyOnProcessEnd());
		onEnteringStep.setValue(item.isOnEnteringStep());
		skipNotificationWhenTriggeredByAssignee.setValue(item.isSkipNotificationWhenTriggeredByAssignee());
		notifyTaskAssignee.setValue(item.isNotifyTaskAssignee());
		notifyEmailAddresses.setValue(item.getNotifyEmailAddresses());
		notifyUserAttributes.setValue(item.getNotifyUserAttributes());
	}

	@Override
	protected void saveDetails(BpmNotificationConfig item) {
		item.setProfileName(getString(profileName));
		item.setTemplateName(getString(templateName));
		item.setTemplateArgumentProvider(getString(templateArgumentProvider));
		item.setActive(getBoolean(active));
		item.setSendHtml(getBoolean(sendHtml));
		item.setLocale(getString(localeField));
		item.setProcessTypeRegex(getString(processTypeRegex));
		item.setStateRegex(getString(stateRegex));
		item.setLastActionRegex(getString(lastActionRegex));
		item.setNotifyOnProcessStart(getBoolean(notifyOnProcessStart));
		item.setNotifyOnProcessEnd(getBoolean(notifyOnProcessEnd));
		item.setOnEnteringStep(getBoolean(onEnteringStep));
		item.setSkipNotificationWhenTriggeredByAssignee(getBoolean(skipNotificationWhenTriggeredByAssignee));
		item.setNotifyTaskAssignee(getBoolean(notifyTaskAssignee));
		item.setNotifyEmailAddresses(getString(notifyEmailAddresses));
		item.setNotifyUserAttributes(getString(notifyUserAttributes));
	}

	@Override
	protected void prepareData() {
		mailProperties = new BpmNotificationMailPropertiesDAO().findAll();

		mailTemplates = new BpmNotificationTemplateDAO().findAll();

		processDefinitions = getThreadProcessToolContext()
				.getProcessDefinitionDAO().getActiveConfigurations();
		for (ProcessDefinitionConfig processDefinition : processDefinitions) {
			for (ProcessStateConfiguration processStateConfiguration : processDefinition.getStates()) {
				Hibernate.initialize(processStateConfiguration.getActions());
			}
		}

		templateArgumentProviders = getService().getTemplateArgumentProviders();

		bindValues(profileName, from(mailProperties).select(new F<BpmNotificationMailProperties, String>() {
			@Override
			public String invoke(BpmNotificationMailProperties x) {
				return x.getProfileName();
			}
		}).ordered().toList());
		bindValues(templateName, from(mailTemplates).select(new F<BpmNotificationTemplate, String>() {
			@Override
			public String invoke(BpmNotificationTemplate x) {
				return x.getTemplateName();
			}
		}));
		bindValues(templateArgumentProvider, from(templateArgumentProviders).select(new F<TemplateArgumentProvider, String>() {
			@Override
			public String invoke(TemplateArgumentProvider x) {
				return x.getName();
			}
		}));
	}

	@Override
	protected List<BpmNotificationConfig> getAllItems() {
		return new BpmNotificationConfigDAO().findAll();
	}

	@Override
	protected String getItemCaption(BpmNotificationConfig item) {
		return item.getTemplateName() + " (" + item.getId() + ")";
	}

	@Override
	protected BpmNotificationConfig createItem() {
		BpmNotificationConfig item = new BpmNotificationConfig();
		item.setActive(true);
		item.setLocale(I18NSource.ThreadUtil.getThreadI18nSource().getLocale().toString());
		if (mailProperties.size() == 1) {
			item.setProfileName(mailProperties.get(0).getProfileName());
		}
		return item;
	}

	@Override
	protected BpmNotificationConfig refreshItem(Long id) {
		return new BpmNotificationConfigDAO().loadById(id);
	}

	@Override
	protected void saveItem(BpmNotificationConfig item) {
		new BpmNotificationConfigDAO().saveOrUpdate(item);
	}

	@Override
	public void buttonClick(Button.ClickEvent event) {
		if (event.getSource() == selectProcess) {
			selectProcesses();
		}
		else if (event.getSource() == selectState) {
			selectStates();
		}
		else if (event.getSource() == selectAction) {
			selectActions();
		}
		else {
			super.buttonClick(event);
		}
	}

	private void selectProcesses() {
		SelectValuesDialog<String> dialog = new SelectValuesDialog<String>(String.class, getMessage("Wybierz procesy"), getI18NSource()) {
			@Override
			protected void valuesSelected(Set<String> items) {
				processTypeRegex.setValue(listToRegex(items));
			}
		};
		List<Pair<String, String>> items = new ArrayList<Pair<String, String>>();
		for (ProcessDefinitionConfig processDefinition : processDefinitions) {
			items.add(new Pair<String, String>(
					getMessage(processDefinition.getDescription()),
					processDefinition.getProcessName()
			));
		}
		items = orderByFirst(items);
		dialog.setAvailableItems(items);
		dialog.setItems(from(getCurrentlySelectedProcesses()).select(new F<ProcessDefinitionConfig, String>() {
			@Override
			public String invoke(ProcessDefinitionConfig x) {
				return x.getProcessName();
			}
		}).toSet());
		dialog.show(getApplication());
	}

	private Collection<ProcessDefinitionConfig> getCurrentlySelectedProcesses() {
		List<ProcessDefinitionConfig> selectedDefinitions = new ArrayList<ProcessDefinitionConfig>();

		for (ProcessDefinitionConfig processDefinition : processDefinitions) {
			if (hasMatchingProcessName(getString(processTypeRegex), processDefinition.getProcessName())) {
				selectedDefinitions.add(processDefinition);
			}
		}
		return selectedDefinitions;
	}


	private boolean hasMatchingProcessName(String processTypeRegex, String definitionName) {
		return !(hasText(processTypeRegex) && !definitionName.toLowerCase().matches(processTypeRegex.toLowerCase()));
	}

	private void selectStates() {
		SelectValuesDialog<Tuple> dialog = new SelectValuesDialog<Tuple>(Tuple.class, getMessage("Wybierz stany"), getI18NSource()) {
			@Override
			protected void valuesSelected(Set<Tuple> items) {
				stateRegex.setValue(listToRegex(items, 1));
			}
		};

		List<Pair<String, Tuple>> items = new ArrayList<Pair<String, Tuple>>();
		Collection<ProcessDefinitionConfig> currentlySelectedProcesses = getCurrentlySelectedProcesses();

		for (ProcessDefinitionConfig processDefinition : currentlySelectedProcesses) {
			for (ProcessStateConfiguration state : processDefinition.getStates()) {
				String descr;
				if (currentlySelectedProcesses.size() == 1) {
					descr = getMessage(state.getDescription());
				}
				else {
					descr = getMessage(processDefinition.getDescription()) + " > " + getMessage(state.getDescription());
				}
				items.add(new Pair<String, Tuple>(
						descr,
						new Tuple(processDefinition.getProcessName(), state.getName())
				));
			}
		}
		items = orderByFirst(items);
		dialog.setAvailableItems(items);
		dialog.setItems(from(getCurrentlySelectedStates()).select(new F<ProcessStateConfiguration, Tuple>() {
			@Override
			public Tuple invoke(ProcessStateConfiguration x) {
				return new Tuple(x.getDefinition().getProcessName(), x.getName());
			}
		}).toSet());
		dialog.show(getApplication());
	}

	private Collection<ProcessStateConfiguration> getCurrentlySelectedStates() {
		List<ProcessStateConfiguration> states = new ArrayList<ProcessStateConfiguration>();

		for (ProcessDefinitionConfig processDefinition : getCurrentlySelectedProcesses()) {
			for (ProcessStateConfiguration state : processDefinition.getStates()) {
				if (hasMatchingStateName(getString(stateRegex), state.getName())) {
					states.add(state);
				}
			}
		}
		return states;
	}

	private boolean hasMatchingStateName(String stateRegex, String stateName) {
		return !hasText(stateRegex) || (stateName != null && stateName.toLowerCase().matches(stateRegex.toLowerCase()));
	}

	private void selectActions() {
		SelectValuesDialog<Tuple> dialog = new SelectValuesDialog<Tuple>(Tuple.class, getMessage("Wybierz akcje"), getI18NSource()) {
			@Override
			protected void valuesSelected(Set<Tuple> items) {
				lastActionRegex.setValue(listToRegex(items, 1));
			}
		};

		List<Pair<String, Tuple>> items = new ArrayList<Pair<String, Tuple>>();
		Collection<ProcessDefinitionConfig> currentlySelectedProcesses = getCurrentlySelectedProcesses();
		Collection<ProcessStateConfiguration> currentlySelectedStates = getCurrentlySelectedStates();

		for (ProcessStateConfiguration state : currentlySelectedStates) {
			for (ProcessStateAction action : state.getActions()) {
				String descr;
				if (currentlySelectedProcesses.size() > 1) {
					descr = getMessage(state.getDefinition().getDescription()) + " > " +
							getMessage(state.getDescription()) + " > " +
							getMessage(action.getLabel());
				}
				else if (currentlySelectedStates.size() > 1) {
					descr = getMessage(state.getDescription()) + " > " +
							getMessage(action.getLabel());
				}
				else {
					descr = getMessage(action.getLabel());
				}

				items.add(new Pair<String, Tuple>(
						descr,
						new Tuple(state.getDefinition().getProcessName(), state.getName(), action.getBpmName())
				));
			}
		}
		items = orderByFirst(items);
		dialog.setAvailableItems(items);
		dialog.setItems(from(getCurrentlySelectedActions()).select(new F<ProcessStateAction, Tuple>() {
			@Override
			public Tuple invoke(ProcessStateAction x) {
				return new Tuple(x.getConfig().getDefinition().getProcessName(), x.getConfig().getName(), x.getBpmName());
			}
		}).toSet());
		dialog.show(getApplication());
	}

	private Collection<ProcessStateAction> getCurrentlySelectedActions() {
		List<ProcessStateAction> actions = new ArrayList<ProcessStateAction>();

		for (ProcessStateConfiguration state : getCurrentlySelectedStates()) {
			for (ProcessStateAction action : state.getActions()) {
				if (hasMatchingActionName(getString(lastActionRegex), action.getBpmName())) {
					actions.add(action);
				}
			}
		}
		return actions;
	}

	private boolean hasMatchingActionName(String actionRegex, String actionBpmName) {
		if (hasText(actionRegex)) {
			if (actionBpmName == null || !actionBpmName.toLowerCase().matches(actionRegex.toLowerCase())) {
				return false;
			}
		}
		return true;
	}

	private <T1 extends Comparable<T1>, T2> List<Pair<T1, T2>> orderByFirst(List<Pair<T1, T2>> items) {
		return from(items).orderBy(new F<Pair<T1, T2>, Comparable>() {
			@Override
			public T1 invoke(Pair<T1, T2> x) {
				return x.getFirst();
			}
		}).toList();
	}

	private String listToRegex(Set<String> items) {
		return items.isEmpty() ? null : from(items).distinct().ordered().toString("|");
	}

	private String listToRegex(Set<Tuple> items, final int idx) {
		return items.isEmpty() ? null : from(items).select(new F<Tuple, String>() {
			@Override
			public String invoke(Tuple x) {
				return (String)x.getParts()[idx];
			}
		}).distinct().ordered().toString("|");
	}
}
