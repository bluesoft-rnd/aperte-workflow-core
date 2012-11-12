package pl.net.bluesoft.rnd.processtool.ui.process;

import static com.vaadin.ui.Label.CONTENT_XHTML;
import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.util.lang.Formats.nvl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.aperteworkflow.ui.help.HelpProvider;
import org.aperteworkflow.ui.help.HelpProviderFactory;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.common.FailedProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionCallback;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolChildrenFilteringWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEventBus;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Lang;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.TaskWatch;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Główny panel widoku zawartości kroku procesu
 * 
 * @author tlipski@bluesoft.net.pl, mpawlak@bluesoft.net.pl
 */
public class ProcessDataPane extends VerticalLayout implements WidgetContextSupport {
	private Logger logger = Logger.getLogger(ProcessDataPane.class.getName());

	private ProcessToolBpmSession bpmSession;

	private I18NSource i18NSource;

	private Set<ProcessToolDataWidget> dataWidgets = new HashSet<ProcessToolDataWidget>();
	private boolean isOwner;

	private Application application;
	private ProcessDataDisplayContext displayProcessContext;

	private BpmTask task;
	private HelpProvider helpFactory;

	private ProcessToolActionCallback actionCallback;
	private GuiAction guiAction = null;

	private static enum GuiAction {
		ACTION_PERFORMED, SAVE_PERFORMED, ACTION_FAILED;
	}

	public ProcessDataPane(Application application, ProcessToolBpmSession bpmSession, I18NSource i18NSource, BpmTask bpmTask,
			ProcessDataDisplayContext hideProcessHandler) {
		this.application = application;
		this.bpmSession = bpmSession;
		this.i18NSource = i18NSource;
		displayProcessContext = hideProcessHandler;
		task = bpmTask;

		refreshTask();
		prepare();

		setMargin(new MarginInfo(false, false, true, true));
		initLayout(false);
	}

	private void prepare() {
        HelpProviderFactory helpProviderFactory =
                ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().lookupService(HelpProviderFactory.class.getName());
        if (helpProviderFactory != null)
            helpFactory = helpProviderFactory.getInstance(application, task.getProcessDefinition(), true, "step_help");

		actionCallback = new MyProcessToolActionCallback();
	}

	/** Odśwież odśwież widok po zmianie kroku lub procesu */
	private void initLayout(boolean autoHide) {
		final ProcessToolContext ctx = getCurrentContext();

		removeAllComponents();
		setWidth(100, Sizeable.UNITS_PERCENTAGE);
		dataWidgets.clear();

		boolean processRunning = bpmSession.isProcessRunning(task.getInternalProcessId(), ctx);
		isOwner = processRunning && !task.isFinished();
		if (!isOwner) 
		{
			//showProcessStateInformation(processRunning);
			if (autoHide)
			{
				/* Jeżeli wstrzymujemy proces glowny, albo zamykamy podproces, sprobuj wrocic 
				 * do odpowiedniego procesu
				 */
				boolean isProcessChanged = changeCurrentViewToActiveProcess();
				
				/* Nie zmienilismy procesu, tak wiec chowamy ten widok */
				if(!isProcessChanged)
				{
					guiAction = null; 
					displayProcessContext.hide();
					return;
				}
				else
				{
					/* Zacznij od nowa z nowym przypisanym taskiem */
					initLayout(false);
					return;
				}
			}
		}
		guiAction = null;

		final ProcessStateConfiguration stateConfiguration = ctx.getProcessDefinitionDAO()
                .getProcessStateConfiguration(task);

		Label stateDescription = new Label(getMessage(stateConfiguration.getDescription()));
		stateDescription.addStyleName("h1 color processtool-title");
        stateDescription.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        addComponent(stateDescription);

		if (Strings.hasText(stateConfiguration.getCommentary())) {
			addComponent(new Label(getMessage(stateConfiguration.getCommentary()), Label.CONTENT_XHTML));
		}
        if (helpFactory != null)
		    addComponent(helpFactory.helpIcon(task.getTaskName(), "step.help"));

		displayProcessContext.setCaption(task.getExternalProcessId() != null ? task.getExternalProcessId() : task.getInternalProcessId());

		final VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(stateConfiguration.getWidgets());
        Collections.sort(widgets, new WidgetPriorityComparator());
        
        
        TaskWatch watch = new TaskWatch(ProcessDataPane.class.getSimpleName() + " - generowanie interfejsu dla kroku " + stateConfiguration.getName());

		final WidgetEventBus widgetEventBus = new WidgetEventBus();

		for (final ProcessStateWidget w : widgets) {
			try {
				watch.watchTask(w.getClassName() + ": " + w.getName(), new Callable() {

					@Override
					public Object call() throws Exception {
						try {
							ProcessToolWidget realWidget = getWidget(w, stateConfiguration, ctx, null, widgetEventBus);
							if (realWidget instanceof ProcessToolVaadinRenderable && (!nvl(w.getOptional(), false) || realWidget.hasVisibleData())) {
								processWidgetChildren(w, realWidget, stateConfiguration, ctx, null, widgetEventBus);
								ProcessToolVaadinRenderable vaadinW = (ProcessToolVaadinRenderable) realWidget;
								vl.addComponent(vaadinW.render());
							}
						}
						catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
							vl.addComponent(new Label(getMessage("process.data.widget.exception-occurred")));
							vl.addComponent(new Label(e.getMessage()));
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							e.printStackTrace(new PrintWriter(baos));
							vl.addComponent(new Label("<pre>" + baos.toString() + "</pre>", CONTENT_XHTML));
						}
						// TODO Auto-generated method stub
						return null;
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		
		watch.stopAll();
		logger.log(Level.INFO, watch.printSummary());

		addComponent(vl);
		setExpandRatio(vl,1f);

		if (isOwner) {
			HorizontalLayout buttonLayout = getButtonsPanel(stateConfiguration);
			addComponentAsFirst(buttonLayout);

			buttonLayout = getButtonsPanel(stateConfiguration);
			addComponent(buttonLayout);
		}
	}
	
	/** Metoda w przypadku wstrzymywania procesu przelacza widok na podproces
	 * lub w przypadku zamkniecia podprocesu, na proces glowny
	 * 
	 * @return true jeżeli nastąpiło przełączenie
	 */
	private boolean changeCurrentViewToActiveProcess()
	{
		/* Aktualny proces */
		ProcessInstance closedProcess = task.getProcessInstance();
		
		/* Proces główny względem wstrzymywanego procesu */
		ProcessInstance parentProcess = closedProcess.getParent();
		
		boolean isSubProcess = parentProcess != null ;
		boolean isParentProcess = !closedProcess.getChildren().isEmpty();
		
		/* Zamykany proces jest podprocesem, wybierz do otwoarcia jego rodzica */
		if(isSubProcess)
		{
			/* Przełącz się na proces głowny */
			if(parentProcess.isProcessRunning())
				return changeProcess(parentProcess);
		}
		
		
		/* Zamykany proces jest procesem glownym dla innych procesow */
		if(isParentProcess)
		{
			/* Pobierz podprocesy skorelowane z zamykanym procesem */
			for(ProcessInstance childProcess: task.getProcessInstance().getChildren())
			{
				if(childProcess.isProcessRunning())
				{
					/* Tylko jeden proces powinien być aktywny, przełącz się na 
					 * niego
					 */
					return changeProcess(childProcess);
				}
			}
		}
		
		
		/* Zatrzymywany proces nie posiada ani aktywnego procesu głównego, ani
		 * aktywnych podprocesów. Zamknij więc widok
		 */
		return false;
	}

	
	private boolean changeProcess(ProcessInstance newProcess)
	{
		/* Get active task for current process */
		List<BpmTask> activeTasks = bpmSession.findProcessTasks(newProcess,  getCurrentContext());
		
		/* Check if the current process has active task. It should has at least one */
		if(activeTasks.isEmpty())
			return false;
		
		UserData user = bpmSession.getUser(getCurrentContext());
		String userLogin = user.getLogin();
		
		for(BpmTask task: activeTasks)
		{
			if(task.getAssignee() != null && task.getAssignee().equals(userLogin))
			{
				/* Change current task */
				updateTask(task);
				
				refreshTask();
				
				return true;
			}
		}
		
		/* There are no active task or the assigne is diffrent */
		return false;
		

	}

	private HorizontalLayout getButtonsPanel(ProcessStateConfiguration stateConfiguration) {

        // sort the actions to preserve the displaying order
        List<ProcessStateAction> actionList = new ArrayList<ProcessStateAction>(stateConfiguration.getActions());
        Collections.sort(actionList, new ActionPriorityComparator());


		AligningHorizontalLayout buttonLayout = new AligningHorizontalLayout(Alignment.MIDDLE_RIGHT);
		buttonLayout.setMargin(new MarginInfo(false, true, false, true));
		buttonLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);

		for (final ProcessStateAction a : actionList) {
			final ProcessToolActionButton actionButton = makeButton(a);
			actionButton.setEnabled(isOwner);
			actionButton.loadData(task);
			actionButton.setActionCallback(actionCallback);
			if (actionButton instanceof ProcessToolVaadinRenderable) {
				buttonLayout.addComponent(((ProcessToolVaadinRenderable) actionButton).render());
			}
		}

		buttonLayout.addComponentAsFirst(new Label() {{
			setWidth(100, Sizeable.UNITS_PERCENTAGE);
		}});

		buttonLayout.recalculateExpandRatios();

		return buttonLayout;
	}

    public List<Component> getToolbarButtons() {
        List<Component> buttons = new ArrayList<Component>();

        Button saveButton = createSaveButton();
        buttons.add(saveButton);

        return buttons;
    }

    public boolean canSaveProcessData() {
        return isOwner;
    }

	private Button createSaveButton() {
		Button saveButton = VaadinUtility.link(i18NSource.getMessage("button.save.process.data"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
				saveProcessDataButtonAction();
			}
		});
        saveButton.addStyleName("with_message");
        saveButton.setDescription(i18NSource.getMessage("button.save.process.desc"));
        saveButton.setIcon(VaadinUtility.imageResource(application, "save.png"));
		saveButton.setEnabled(isOwner);
		return saveButton;
	}

	public boolean saveProcessDataButtonAction() {
		final boolean[] result = { false };
		withErrorHandling(application, new Runnable() {
			@Override
			public void run() {
				if (validateWidgetsAndSaveData(task)) {
					refreshTask();
					guiAction = GuiAction.SAVE_PERFORMED;
					initLayout(false);
					result[0] = true;
				}
			}
		});
		return result[0];
	}

	private void refreshTask() {
		task = refreshTask(bpmSession, task);
	}

	@Override
	public void updateTask(BpmTask task) {
		this.task = task;
	}

	@Override
	public Set<ProcessToolDataWidget> getWidgets() {
		return Collections.unmodifiableSet(dataWidgets);
	}

	@Override
	public void displayValidationErrors(Map<ProcessToolDataWidget, Collection<String>> errorMap) {
		String errorMessage = VaadinUtility.widgetsErrorMessage(i18NSource, errorMap);
		VaadinUtility.validationNotification(application, i18NSource, errorMessage);
	}

	@Override
	public Map<ProcessToolDataWidget, Collection<String>> getWidgetsErrors(BpmTask bpmTask, boolean skipRequired) {
		Map<ProcessToolDataWidget, Collection<String>> errorMap = new HashMap();
		for (ProcessToolDataWidget w : dataWidgets) {
			Collection<String> errors = w.validateData(bpmTask, skipRequired);
			if (errors != null && !errors.isEmpty()) {
				errorMap.put(w, errors);
			}
		}
		return errorMap;
	}

	@Override
	public boolean validateWidgetsAndSaveData(BpmTask task) {
		task = refreshTask(bpmSession, task);
		Map<ProcessToolDataWidget, Collection<String>> errorMap = getWidgetsErrors(task, true);
		if (!errorMap.isEmpty()) {
			displayValidationErrors(errorMap);
			return false;
		}
		saveTaskData(task);
		return true;
	}

	@Override
	public void saveTaskData(BpmTask task, ProcessToolActionButton... actions) {
		for (ProcessToolDataWidget w : dataWidgets) {
			w.saveData(task);
		}
		for (ProcessToolActionButton action : actions) {
			action.saveData(task);
		}
		bpmSession.saveProcessInstance(task.getProcessInstance(), getCurrentContext());
	}

    @Override
	public void saveTaskWithoutData(BpmTask task, ProcessToolActionButton... actions) {
		for (ProcessToolActionButton action : actions) {
			action.saveData(task);
		}
	}

	@Override
	public ProcessToolContext getCurrentContext() {
		return ProcessToolContext.Util.getThreadProcessToolContext();
	}

	@Override
	public BpmTask refreshTask(ProcessToolBpmSession bpmSession, BpmTask bpmTask) {
		return bpmSession.refreshTaskData(bpmTask, getCurrentContext());
	}

	public String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	private ProcessToolActionButton makeButton(ProcessStateAction a) {
		try {
			ProcessToolContext ctx = getCurrentContext();
			ProcessToolActionButton actionButton = ctx.getRegistry().makeButton(a.getButtonName());
			actionButton.setContext(a, bpmSession, application, i18NSource);
			return actionButton;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void processWidgetChildren(ProcessStateWidget parentWidgetConfiguration, ProcessToolWidget parentWidgetInstance,
			ProcessStateConfiguration stateConfiguration, ProcessToolContext ctx, String generatorKey, WidgetEventBus widgetEventBus) {
		Set<ProcessStateWidget> children = parentWidgetConfiguration.getChildren();
		List<ProcessStateWidget> sortedList = new ArrayList<ProcessStateWidget>(children);
		Collections.sort(sortedList, new Comparator<ProcessStateWidget>() {
			@Override
			public int compare(ProcessStateWidget o1, ProcessStateWidget o2) {
				if (o1.getPriority().equals(o2.getPriority())) {
					return Lang.compare(o1.getId(), o2.getId());
				}
				return o1.getPriority().compareTo(o2.getPriority());
			}
		});
		if(parentWidgetInstance instanceof ProcessToolChildrenFilteringWidget){
			sortedList = ((ProcessToolChildrenFilteringWidget)parentWidgetInstance).filterChildren(task, sortedList);
		}

		for (ProcessStateWidget subW : sortedList) {
			if(StringUtils.isNotEmpty(subW.getGenerateFromCollection())){
				generateChildren(parentWidgetInstance, stateConfiguration, ctx, subW, widgetEventBus);
			} else {
				subW.setParent(parentWidgetConfiguration);
				addWidgetChild(parentWidgetInstance, stateConfiguration, ctx, subW, generatorKey, widgetEventBus);
			}
		}
	}

    /**
         * Comparator for {@link ProcessStateWidget} objects that takes intro account widget priority
         */
        private class WidgetPriorityComparator implements Comparator<ProcessStateWidget> {
            @Override
            public int compare(ProcessStateWidget w1, ProcessStateWidget w2) {
                if (w1 == null || w2 == null) {
                    throw new NullPointerException("Can not compare null ProcessStateWidgets");
                }

                if (w1 == w2) {
                    return 0;
                }

                if (w1.getPriority() != null && w2.getPriority() != null) {
                    return w1.getPriority().compareTo(w2.getPriority());
                } else if (w1.getPriority() != null && w2.getPriority() == null) {
                    return 1;
                } else if (w1.getPriority() == null && w2.getPriority() != null) {
                    return -1;
                } else {
                    return w1.getId().compareTo(w2.getId());
                }
            }
        }

        /**
         * Comparator for {@link ProcessStateAction} object that takes into account action priority
         */
        private class ActionPriorityComparator implements Comparator<ProcessStateAction> {
            @Override
            public int compare(ProcessStateAction a1, ProcessStateAction a2) {
                if (a1 == null || a2 == null) {
                    throw new NullPointerException("Can not compare null ProcessStateActions");
                }

                if (a1 == a2) {
                    return 0;
                }

                if (a1.getActionType() != null && a1.getActionType() != null && !a1.getActionType().equals(a2.getActionType())) {
                    return ProcessStateAction.SECONDARY_ACTION.equals(a1.getActionType()) ? -1 : 1;
                } else if (a1.getActionType() != null && a2.getActionType() == null) {
                    return -1;
                } else if (a1.getActionType() == null && a2.getActionType() != null) {
                    return 1;
                } else {
                    if (a1.getPriority() != null && a1.getPriority() != null) {
                        return a1.getPriority().compareTo(a2.getPriority());
                    } else if (a1.getPriority() != null && a2.getPriority() == null) {
                        return 1;
                    } else if (a1.getPriority() == null && a2.getPriority() != null) {
                        return -1;
                    } else {
                        return a1.getId().compareTo(a2.getId());
                    }
                }
            }
        }

	private void generateChildren(ProcessToolWidget parentWidgetInstance, ProcessStateConfiguration stateConfiguration, ProcessToolContext ctx,
			ProcessStateWidget subW, WidgetEventBus widgetEventBus) {
		String collection = task.getProcessInstance().getSimpleAttributeValue(subW.getGenerateFromCollection(), null);
		if(StringUtils.isEmpty(collection))
			return;
		String[] items = collection.split("[,; ]");

		for(String item : items){
			addWidgetChild(parentWidgetInstance, stateConfiguration, ctx, subW, item, widgetEventBus);
		}
	}

	private void addWidgetChild(ProcessToolWidget parentWidgetInstance, ProcessStateConfiguration stateConfiguration, ProcessToolContext ctx,
			ProcessStateWidget subW, String generatorKey, WidgetEventBus widgetEventBus) {
		ProcessToolWidget widgetInstance = getWidget(subW, stateConfiguration, ctx, generatorKey, widgetEventBus);
			if (!nvl(subW.getOptional(), false) || widgetInstance.hasVisibleData()) {
				processWidgetChildren(subW, widgetInstance, stateConfiguration, ctx, generatorKey, widgetEventBus);
				parentWidgetInstance.addChild(widgetInstance);
			}
		}

	private ProcessToolWidget getWidget(ProcessStateWidget w, ProcessStateConfiguration stateConfiguration, ProcessToolContext ctx,
										String generatorKey, WidgetEventBus widgetEventBus) {
		ProcessToolWidget processToolWidget;
		try {
			ProcessToolRegistry toolRegistry = VaadinUtility.getProcessToolContext(application.getContext()).getRegistry();
			processToolWidget = w.getClassName() == null ? toolRegistry.makeWidget(w.getName()) : toolRegistry.makeWidget(w.getClassName());
			processToolWidget.setContext(stateConfiguration, w, i18NSource, bpmSession, application,
			                             bpmSession.getPermissionsForWidget(w, ctx), isOwner);
			processToolWidget.setGeneratorKey(generatorKey);
			processToolWidget.setWidgetEventBus(widgetEventBus);
			if (processToolWidget instanceof ProcessToolDataWidget) {
				((ProcessToolDataWidget) processToolWidget).loadData(task);
				dataWidgets.add((ProcessToolDataWidget) processToolWidget);
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			FailedProcessToolWidget failedProcessToolVaadinWidget = new FailedProcessToolWidget(e);
			failedProcessToolVaadinWidget.setContext(stateConfiguration, w, i18NSource, bpmSession, application,
			                                         bpmSession.getPermissionsForWidget(w, ctx),
			                                         isOwner);
			dataWidgets.add(failedProcessToolVaadinWidget);
			processToolWidget = failedProcessToolVaadinWidget;
		}
		return processToolWidget;
	}

	private class MyProcessToolActionCallback implements ProcessToolActionCallback, Serializable {
		private void actionCompleted(GuiAction guiAction, ProcessStateAction action) {
			ProcessDataPane.this.guiAction = guiAction;
			refreshTask();
			initLayout(action.getAutohide());
		}

		@Override
		public void actionPerformed(ProcessStateAction action) {
			actionCompleted(GuiAction.ACTION_PERFORMED, action);
		}

		@Override
		public void actionFailed(ProcessStateAction action) {
			actionCompleted(GuiAction.ACTION_FAILED, action);
		}

		@Override
		public WidgetContextSupport getWidgetContextSupport() {
			return ProcessDataPane.this;
		}
	}
}
