package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import pl.net.bluesoft.rnd.poutils.DateUtil;
import pl.net.bluesoft.rnd.poutils.cquery.func.F;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import org.aperteworkflow.util.vaadin.VaadinUtility.HasRefreshButton;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.*;

import static pl.net.bluesoft.rnd.poutils.cquery.CQuery.from;
import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivityQueuesPane extends Panel implements HasRefreshButton {

	private ActivityMainPane activityMainPane;
	private VerticalLayout taskList;
	private EventListener<BpmEvent> bpmEventSubScriber = null;

	public ActivityQueuesPane(ActivityMainPane activityMainPane) {
		this.activityMainPane = activityMainPane;
		setWidth("100%");
		setCaption(getMessage("activity.queues.title"));
		addComponent(horizontalLayout(new Label(getMessage("activity.queues.help.short"), Label.CONTENT_XHTML),
                refreshIcon(activityMainPane.getApplication(), this)));
		taskList = new VerticalLayout();
		taskList.setMargin(true);
		addComponent(taskList);
		refreshData();

		if (bpmEventSubScriber == null) {
			activityMainPane.getBpmSession().getEventBusManager().subscribe(BpmEvent.class, bpmEventSubScriber = new EventListener<BpmEvent>() {
				@Override
				public void onEvent(BpmEvent e) {
					refreshData();
				}
			});
		}

	}

	public void refreshData() {
		taskList.removeAllComponents();

        final ProcessToolContext processToolContextFromThread = ProcessToolContext.Util.getThreadProcessToolContext();
		final ProcessToolBpmSession bpmSession = activityMainPane.getBpmSession();
		UserData user = bpmSession.getUser(processToolContextFromThread);

		taskList.addComponent(createUserTasksButton(
                getMessage("activity.my.tasks"),
                bpmSession,
                processToolContextFromThread,
                null
        ));
//        this feature will be replaced by process search ability
		taskList.addComponent(createRecentTasksButton(user, processToolContextFromThread));

		final List<ProcessQueue> userAvailableQueues =
                new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues(processToolContextFromThread));

        Collections.sort(userAvailableQueues, new Comparator<ProcessQueue>() {
			public int compare(ProcessQueue o1, ProcessQueue o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (ProcessQueue q : userAvailableQueues) {
			taskList.addComponent(createQueueButton(q, bpmSession, null));
		}
/*
        user substitutions require more analysis

        List<UserData> substitutedUsers = getSubstitutedUsers(user, new Date());
        Map<UserData, ProcessToolBpmSession> substitutedUserToSession = from(substitutedUsers)
                .toMap(
                    Selectors.identity(UserData.class),
                    new F<UserData, ProcessToolBpmSession>() {
                        @Override
                        public ProcessToolBpmSession invoke(UserData substitutedUser) {
                            return bpmSession.createSession(
                                substitutedUser,
                                LiferayBridge.getUserRoles(substitutedUser),
                                processToolContextFromThread
                            );
                        }
                });

        for (UserData substitutedUser : substitutedUsers) {
            taskList.addComponent(createUserTasksButton(
                    getMessage("activity.user.tasks") + " " + substitutedUser.getRealName(),
                    substitutedUserToSession.get(substitutedUser),
                    processToolContextFromThread,
                    substitutedUser
            ));
        }

        Map<String, List<QueueUserSession>> map = new HashMap<String, List<QueueUserSession>>();
        for (Map.Entry<UserData, ProcessToolBpmSession> userAndSession : substitutedUserToSession.entrySet()) {
            for (ProcessQueue q : userAndSession.getValue().getUserAvailableQueues(processToolContextFromThread)) {
                if (!map.containsKey(q.getName())) {
                    map.put(q.getName(), new ArrayList<QueueUserSession>());
                }
                QueueUserSession qus = new QueueUserSession();
                qus.queue = q;
                qus.user = userAndSession.getKey();
                qus.bpmSession = userAndSession.getValue();
                map.get(q.getName()).add(qus);
            }
        }

		for (String queueName : from(map.keySet()).orderBy(identity(String.class))) {
            taskList.addComponent(createQueueButton(
                    map.get(queueName).get(0).queue,
                    map.get(queueName).get(0).bpmSession,
                    map.get(queueName).get(0).user
            ));
		}   */
	}

    private static class QueueUserSession {
        ProcessQueue queue;
        ProcessToolBpmSession bpmSession;
        UserData user;
    }
    
    private Button createUserTasksButton(String caption, ProcessToolBpmSession bpmSession, ProcessToolContext ctx, final UserData user) {
		Collection<ProcessInstance> userProcesses = bpmSession.getUserProcesses(0, 1000, ctx);
		Button b = new Button(caption + " (" + userProcesses.size() + ")");
		b.setStyleName(BaseTheme.BUTTON_LINK);
		b.setEnabled(userProcesses.size() > 0);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                withErrorHandling(getApplication(), new Runnable() {
                    public void run() {
                        if (user == null) {
                            activityMainPane.displayMyTasksPane();
                        }
                        else {
                            activityMainPane.displayOtherUserTasksPane(user);
                        }
                    }
                });
            }
        });
        return b;
    }

    private Button createRecentTasksButton(UserData user, ProcessToolContext ctx) {
        final Calendar minDate = Calendar.getInstance();
		minDate.add(Calendar.DAY_OF_YEAR, -5);
		List<ProcessInstance> recentProcesses = ctx.getProcessInstanceDAO()
				.getRecentProcesses(user, minDate, null, 0, 100);
		Button b = new Button(getMessage("activity.recent.tasks") + " (" + recentProcesses.size() + ")");
		b.setStyleName(BaseTheme.BUTTON_LINK);
		b.setEnabled(recentProcesses.size() > 0);
		b.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				withErrorHandling(getApplication(), new Runnable() {
					public void run() {
						activityMainPane.displayRecentTasksPane(minDate);
					}
				});
			}
		});
        return b;
    }

    private Button createQueueButton(final ProcessQueue q, final ProcessToolBpmSession bpmSession, final UserData user) {
        long processCount = q.getProcessCount();
        String desc = getMessage(q.getDescription());
        Button qb = new Button(desc + " (" + processCount + ")");
        qb.setDescription(desc);
        qb.setStyleName(BaseTheme.BUTTON_LINK);
        qb.setEnabled(processCount > 0);
        qb.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final Button.ClickEvent event) {
                withErrorHandling(getApplication(), new Runnable() {
                    public void run() {
                        if (q.isBrowsable()) {
                            if (user == null) {
                                activityMainPane.displayQueue(q);
                            }
                            else {
                                activityMainPane.displayOtherUserQueue(q, user);
                            }
                        } else {
                            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                            ProcessInstance instance = bpmSession.assignTaskFromQueue(q, ctx);
                            if (instance != null) {
                                getWindow().executeJavaScript("Liferay.trigger('processtool.bpm.assignProcess', '" + instance.getInternalId() + "');");
                                getWindow().executeJavaScript("vaadin.forceSync();");//other portlets may need this
//									Window w = new Window(instance.getInternalId());
//									w.setContent(new ProcessDataPane(getApplication(), bpmSession, activityMainPane.getI18NSource(), instance, w));
//									w.center();
//									getWindow().addWindow(w);
//									getApplication().getMainWindow().showNotification(getMessage("process-tool.task.assigned"),
//									                                                  Window.Notification.TYPE_HUMANIZED_MESSAGE);
//									w.focus();
                                if (user == null) {
                                    activityMainPane.displayProcessData(instance);
                                }
                                else {
                                    activityMainPane.displayProcessData(instance, bpmSession);
                                }

                            }
                        }
                    }
                });
            }
        });
        return qb;
    }

    private static List<UserData> getSubstitutedUsers(UserData user, Date date) {
        List<UserSubstitution> substitutions = ProcessToolContext.Util.getThreadProcessToolContext()
                .getUserSubstitutionDAO()
                .getActiveSubstitutions(user, DateUtil.truncHours(new Date()));
        return from(substitutions)
                .select(new F<UserSubstitution, UserData>(){
                    @Override
                    public UserData invoke(UserSubstitution userSubstitution) {
                        return userSubstitution.getUser();
                    }
                })
                .distinct()
                .orderBy(new F<UserData, String>() {
                    @Override
                    public String invoke(UserData user) {
                        return user.getRealName();
                    }
                })
                .toList();
    }

	private String getMessage(String title) {
		return activityMainPane.getI18NSource().getMessage(title);
	}

    
}
