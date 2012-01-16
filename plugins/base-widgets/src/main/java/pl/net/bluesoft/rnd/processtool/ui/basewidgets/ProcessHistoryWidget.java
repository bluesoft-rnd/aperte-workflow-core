package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "ProcessHistory")
@AperteDoc(humanNameKey="widget.process_history.name", descriptionKey="widget.process_history.description")
@ChildrenAllowed(false)
public class ProcessHistoryWidget extends BaseProcessToolWidget implements ProcessToolDataWidget, ProcessToolVaadinWidget {

    @AutoWiredProperty(required = false)
    @AperteDoc(humanNameKey="widget.process_history.property.table.name", descriptionKey="widget.process_history.property.table.description")
	private Boolean table;

	private List<ProcessLogInfo> logInfos = new ArrayList<ProcessLogInfo>();

	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("Not supported!");
	}

	@Override
	public Collection<String> validateData(ProcessInstance processInstance) {
		return null;
	}

	@Override
	public void saveData(ProcessInstance processInstance) {
		//nothing
	}

	@Override
	public void loadData(ProcessInstance processInstance) {
		List<ProcessInstanceLog> processLogs = new ArrayList(processInstance.getProcessLogs());
		Collections.sort(processLogs);
		for (ProcessInstanceLog pl : processLogs) {
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
			logInfos.add(plInfo);
		}
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
//			for (int i = 0; i < widths.length; i++) {
//				table.setColumnWidth(cols[i], widths[i]);
//			}

			return table;
		} else {
			Panel p = new Panel();
			p.setStyleName(Reindeer.PANEL_LIGHT);
			p.setWidth("100%");
			p.setHeight("240px");

			VerticalLayout layout = (VerticalLayout) p.getContent();
//			layout.setMargin(true);
			layout.setSpacing(true);

			for (ProcessLogInfo pli : logInfos) {
				HorizontalLayout hl;
				hl = new HorizontalLayout();
				hl.setSpacing(true);
				if (hasText(pli.getUserDescription()))
					hl.addComponent(label("<b>" + pli.getUserDescription() + "</b>", 150));
				else
					hl.addComponent(label("<b>System</b>", 150));

				hl.addComponent(label("<b>" + pli.getPerformDate() + "</b>", 130));
				hl.addComponent(new Label("<b>" + getMessage("awf.basewidgets.process-history.stateDescription") + "</b>", Label.CONTENT_XHTML));
				hl.addComponent(label(getMessage(pli.getStateDescription()), 350));
				layout.addComponent(hl);
				hl = new HorizontalLayout();
				hl.setSpacing(true);
				hl.setMargin(new Layout.MarginInfo(false, false, true, true));
				Label l = new Label(pli.getActionDescription(), Label.CONTENT_XHTML);
				l.setWidth("730px");
				hl.addComponent(l);
				layout.addComponent(hl);
			}
			return p;
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
