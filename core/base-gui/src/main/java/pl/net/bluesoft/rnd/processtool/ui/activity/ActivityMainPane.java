package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.newprocess.NewProcessExtendedPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Calendar;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivityMainPane extends HorizontalLayout {
	private Application application;
	private I18NSource i18NSource;
	private ProcessToolBpmSession bpmSession;
	private Component currentlyDisplayed = null;
    
    private TextField searchField = new TextField();

	public ActivityMainPane(Application application, I18NSource i18NSource, ProcessToolBpmSession bpmSession) {
		this.application = application;
		this.i18NSource = i18NSource;
		this.bpmSession = bpmSession;
//		setWidth("100%");
//		String width = application.getMainWindow().getWidth() + "px";
		setWidth("100%");
//		setHeight("100%");
//		setHeight(application.getMainWindow().getHeight() + "px");
//		setSizeFull();
				setHeight(null);

		initGui();
		displayMyTasksPane();
	}

	private void initGui() {
		removeAllComponents();
        searchField.setInputPrompt(getLocalizedMessage("activity.search"));
        searchField.setWidth("100%");
		VerticalLayout c = verticalLayout(panel(getLocalizedMessage("activity.new-process.title"),
		                                        new NewProcessExtendedPane(bpmSession, i18NSource, this)),
		                                  new ActivityQueuesPane(this),
                panel(getLocalizedMessage("activity.search.title"),
                        verticalLayout(
                                htmlLabel(getLocalizedMessage("activity.search.info")),
                                searchField,
                                button(getLocalizedMessage("activity.search.caption"), new Runnable() {

                                    @Override
                                    public void run() {
                                        String text = (String) searchField.getValue();
                                        if (text != null && !text.trim().isEmpty()) {
                                            displaySearchResults(text.trim());
                                        }
                                    }
                                }))));
		c.setWidth("300");
//		setSplitPosition(310, Sizeable.UNITS_PIXELS);
		addComponent(c);
//		setLocked(false);
	}

	public Application getApplication() {
		return application;
	}

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	public ProcessToolBpmSession getBpmSession() {
		return bpmSession;
	}

    public void displaySearchResults(String query) {
        SearchResultsPane toDisplay = new SearchResultsPane(query, this);
        toDisplay.refreshData();
        show(toDisplay);
    }
	public void displayMyTasksPane() {
		MyProcessesListPane toDisplay = new MyProcessesListPane(this, i18NSource.getMessage("activity.my.tasks"));
		toDisplay.refreshData();
		show(toDisplay);
	}

    public void displayOtherUserTasksPane(UserData user) {
        OtherUserProcessesListPane toDisplay = new OtherUserProcessesListPane(
                this,
                i18NSource.getMessage("activity.user.tasks") + " " + user.getRealName(),
                user
        );
		toDisplay.refreshData();
		show(toDisplay);
    }

	private void show(Component toDisplay) {
		if (currentlyDisplayed != null) removeComponent(currentlyDisplayed);
		currentlyDisplayed = toDisplay;
		currentlyDisplayed.setWidth("100%");
		addComponent(currentlyDisplayed);
//		setLocked(false);
		setExpandRatio(currentlyDisplayed, 1.0f);
	}


	public void displayQueue(ProcessQueue q) {
		QueueListPane toDisplay = new QueueListPane(this, q.getDescription(), q);
		toDisplay.refreshData();
		show(toDisplay);
	}

    public void displayOtherUserQueue(ProcessQueue q, UserData user) {
		QueueListPane toDisplay = new OtherUserQueueListPane(this, q.getDescription(), q, user);
		toDisplay.refreshData();
		show(toDisplay);
	}

	public void displayRecentTasksPane(Calendar minDate) {
		RecentProcessesListPane toDisplay = new RecentProcessesListPane(this, i18NSource.getMessage("activity.recent.tasks"), minDate);
		toDisplay.refreshData();
		show(toDisplay);
	}

    public void displayProcessData(ProcessInstance processInstance) {
        displayProcessData(processInstance, null);
    }

	public void displayProcessData(ProcessInstance processInstance, ProcessToolBpmSession bpmSession) {
		final Component previousComponent = currentlyDisplayed;
		final VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setWidth("100%");

		final Button button = new Button(i18NSource.getMessage("activity.back"));
		button.setStyleName(BaseTheme.BUTTON_LINK);
		button.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				show(previousComponent);
			}
		});

		final Label l = new Label();
		HorizontalLayout hl = horizontalLayout("100%", l, button);
		hl.setComponentAlignment(button, Alignment.TOP_RIGHT);
		hl.setExpandRatio(l, 1.0f);
		verticalLayout.addComponent(hl);
		verticalLayout.setSpacing(true);
		verticalLayout.setMargin(true);

		ProcessDataPane pdp = new ProcessDataPane(getApplication(),
		                                          nvl(bpmSession, this.bpmSession),
		                                          i18NSource,
		                                          processInstance,
		                                          new ProcessDataPane.DisplayProcessContext() {
			                                          @Override
			                                          public void hide() {
				                                          show(previousComponent);
			                                          }

			                                          @Override
			                                          public void setCaption(String newCaption) {
				                                          l.setValue(newCaption);
			                                          }
		                                          });
		verticalLayout.addComponent(pdp);

		show(verticalLayout);
	}
}
