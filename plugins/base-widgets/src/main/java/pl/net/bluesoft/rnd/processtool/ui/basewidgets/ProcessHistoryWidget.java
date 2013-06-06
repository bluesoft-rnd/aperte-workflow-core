package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.util.lang.Strings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * 
 * History process widget. 
 * 
 * Refactored for css layout
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
@AliasName(name = "ProcessHistory")
@AperteDoc(humanNameKey="widget.process_history.name", descriptionKey="widget.process_history.description")
@ChildrenAllowed(false)
public class ProcessHistoryWidget extends BaseProcessToolVaadinWidget implements ProcessToolDataWidget, ProcessToolVaadinRenderable {

    @AutoWiredProperty(required = false)
    @AperteDoc(
            humanNameKey="widget.process_history.property.table.name",
            descriptionKey="widget.process_history.property.table.description"
    )
	private Boolean table;

	private List<ProcessLogInfo> logInfos = new ArrayList<ProcessLogInfo>();

	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("Not supported!");
	}

	@Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {
		return null;
	}

	@Override
	public void saveData(BpmTask task) {
		//nothing
	}

	@Override
	public void loadData(BpmTask task) {
        ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();
		List<ProcessInstanceLog> processLogs = new ArrayList(pi.getProcessLogs());
		Collections.sort(processLogs, ProcessInstanceLog.DEFAULT_COMPARATOR);
		for (ProcessInstanceLog pl : processLogs) {
			logInfos.add(getProcessLogInfo(pl));
		}
	}

    //TODO refactor & reuse common code with ProcessInstanceAdminManagerPane
    private ProcessLogInfo getProcessLogInfo(ProcessInstanceLog pl) {
        ProcessLogInfo plInfo = new ProcessLogInfo();
        String userDescription = pl.getUser() != null ? nvl(pl.getUser().getRealName(), pl.getUser().getLogin()) : "";
        if (pl.getUserSubstitute() != null) {
            String substituteDescription = nvl(pl.getUserSubstitute().getRealName(), pl.getUserSubstitute().getLogin());
            plInfo.userDescription = substituteDescription + "(" + getMessage("awf.basewidgets.process-history.substituting") + " " + userDescription  + ")";
        }
        else {
            plInfo.userDescription = userDescription;
        }
        plInfo.entryDescription = nvl(pl.getAdditionalInfo(), pl.getLogValue());
        plInfo.actionDescription = i18NSource.getMessage(pl.getEventI18NKey());
        if (hasText(plInfo.getEntryDescription())) {
            plInfo.actionDescription = plInfo.actionDescription + " - " + getMessage(plInfo.entryDescription);
        }
        plInfo.performDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pl.getEntryDate().getTime());
        plInfo.stateDescription = pl.getState() != null ? nvl(pl.getState().getDescription(), pl.getState().getName()) : "";
        return plInfo;

	}

    public class ProcessLogInfo {
		public String userDescription;
		public String actionDescription;
		public String entryDescription;
		public String stateDescription;
		public String performDate;

		public String getUserDescription() {
			return userDescription;
		}

		public void setUserDescription(String userDescription) {
			this.userDescription = userDescription;
		}

		public String getActionDescription() {
			return actionDescription;
		}

		public void setActionDescription(String actionDescription) {
			this.actionDescription = actionDescription;
		}

		public String getEntryDescription() {
			return entryDescription;
		}

		public void setEntryDescription(String entryDescription) {
			this.entryDescription = entryDescription;
		}

		public String getStateDescription() {
			return stateDescription;
		}

		public void setStateDescription(String stateDescription) {
			this.stateDescription = stateDescription;
		}

		public String getPerformDate() {
			return performDate;
		}

		public void setPerformDate(String performDate) {
			this.performDate = performDate;
		}
	}

	@Override
	public Component render() {
		if (nvl(table, false)) {
			Table table = new Table();
			table.setWidth("100%");
			table.setHeight("180px");

			table.setImmediate(true); // react at once when something is selected
			table.setSelectable(true);
			table.addStyleName("components-inside");
			table.addStyleName("small");

			BeanItemContainer<ProcessLogInfo> bic = new BeanItemContainer<ProcessLogInfo>(ProcessLogInfo.class);
			bic.addAll(logInfos);
			table.setContainerDataSource(bic);
			Object[] cols = {
					"userDescription",
					"performDate",
					"stateDescription",
					"actionDescription"};
			table.setVisibleColumns(cols);

//			int[] widths = new int[]{100, 100, -1, -1, -1};

			for (Object o : table.getVisibleColumns()) {
				table.setColumnHeader(o, getMessage("awf.basewidgets.process-history." + o));
			}

			return table;
		}
		else
		{


			CssLayout layout = new CssLayout();
			layout.setSizeFull();
			layout.addStyleName("history-panel");

			for (ProcessLogInfo pli : logInfos) 
			{
				Label labelAuthor = new Label(hasText(pli.getUserDescription()) ? pli.getUserDescription() : "System");
				labelAuthor.addStyleName("history-header-author");
				layout.addComponent(labelAuthor);
				
				Label labelDate = new Label(pli.getPerformDate());
				labelDate.addStyleName("history-header-time");
				layout.addComponent(labelDate);
				
                if (Strings.hasText(pli.getStateDescription())) 
                {
    				Label labelState = new Label(getMessage(pli.getStateDescription()));
    				labelState.addStyleName("history-header-state");
    				layout.addComponent(labelState);
                }
                
				Label labelBody = new Label(getMessage(pli.getActionDescription()));
				labelBody.addStyleName("history-item-body");
				layout.addComponent(labelBody);
			}
			return layout;
		}
	}

	public static Label label(String message, int width) {
		Label l = new Label(message, Label.CONTENT_XHTML);
		l.setWidth(width +"px");
		return l;
	}

	public Boolean getTable() {
		return table;
	}

	public void setTable(Boolean table) {
		this.table = table;
	}

}
