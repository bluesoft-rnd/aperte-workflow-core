package pl.net.bluesoft.rnd.pt.ext.userdata.widget;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.pt.ext.userdata.model.ProcessInstanceUserAssignment;

import java.util.Collection;
import java.util.HashSet;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class UserDataWidget
		extends BaseProcessToolVaadinWidget
		implements ProcessToolDataWidget, ProcessToolVaadinWidget, Property.ValueChangeListener {

	UserData selectedUser = null;
	private ComboBox combo;
	private Collection<UserData> users;

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.bpmVariableName",
            descriptionKey = "userdata.widget.bpmVariableName.description"
    )
	private String bpmVariableName;

    @AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "userdata.widget.roleInProcess",
            descriptionKey = "userdata.widget.roleInProcess.description"
    )
    private String roleInProcess;

    @AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.required",
            descriptionKey = "userdata.widget.required.description"
    )
    private Boolean required;

	@Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {
		Collection<String> res = new HashSet<String>();
		if (getRequired() && selectedUser==null) {
			res.add("ext.userdata.validate.required."+ roleInProcess);
		}
		return res;
	}

	@Override
	public void saveData(BpmTask task) {
		boolean found = false;
        ProcessInstance pi = task.getProcessInstance();
		for (ProcessInstanceUserAssignment assign : getAttributes(ProcessInstanceUserAssignment.class, pi)) {
			if ((roleInProcess == null && assign.getRole() == null) || (assign.getRole() != null && assign.getRole().equals(roleInProcess))) {
				found = true;
				if (selectedUser == null) {
                    pi.removeAttribute(assign);
				} else {
                    assign.setBpmLogin(selectedUser.getBpmLogin());
					assign.setUserLogin(selectedUser.getLogin());
					assign.setUserDescription(selectedUser.getDescription());
				}
			}
		}
		if (!found && selectedUser != null) {
			ProcessInstanceUserAssignment assign = new ProcessInstanceUserAssignment();
			assign.setUserLogin(selectedUser.getLogin());
			assign.setUserDescription(selectedUser.getDescription());
			assign.setBpmLogin(selectedUser.getBpmLogin());
			assign.setRole(roleInProcess);
			assign.setKey(roleInProcess);
            pi.addAttribute(assign);
		}
	}

	@Override
	public void loadData(BpmTask task) {
        ProcessInstance processInstance = task.getProcessInstance();
		for (ProcessInstanceUserAssignment assign : getAttributes(ProcessInstanceUserAssignment.class, processInstance)) {
			if ((roleInProcess == null && assign.getRole() == null) || (assign.getRole() != null && assign.getRole().equals(roleInProcess))) {
				selectedUser = new UserData();
				selectedUser.setLogin(assign.getUserLogin());
				selectedUser.setDescription(assign.getUserDescription());
				break;
			}
		}
		if (selectedUser != null && combo != null) {
			for (UserData ud : users) {
				if (selectedUser.getLogin().equals(ud.getLogin())) {
					combo.setValue(ud);
				}
			}
		}

	}

	@Override
	public Component render() {
		combo = new ComboBox(i18NSource.getMessage("ext.userdata.prompt." + roleInProcess));

		combo.setItemCaptionPropertyId("description");
		combo.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		combo.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		combo.setImmediate(true);
		combo.addListener(this);
		combo.setNewItemsAllowed(false);
        if (getRequired()) {
            combo.setRequired(true);
            combo.setDescription(i18NSource.getMessage("ext.userdata.validate.required."+ roleInProcess));
        }

		users = getUsers();
		BeanItemContainer bic = new BeanItemContainer(users);
		combo.setContainerDataSource(bic);
		bic.sort(new Object[] { "description" }, new boolean[] { true });

		if (selectedUser != null) {
			for (UserData ud : users) {
				if (selectedUser.getLogin().equals(ud.getLogin())) {
					combo.setValue(ud);
				}
			}
		}
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(combo);
		combo.setReadOnly(!hasPermission("EDIT"));

		vl.setWidth("100%");
		combo.setWidth("100%");
		return vl;
			
	}

	public void valueChange(Property.ValueChangeEvent event) {
		selectedUser = (UserData) combo.getValue();
	}

	protected abstract Collection<UserData> getUsers();

	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("Not supported!");
	}

	public String getBpmVariableName() {
		return bpmVariableName;
	}

	public void setBpmVariableName(String bpmVariableName) {
		this.bpmVariableName = bpmVariableName;
	}

    public String getRoleInProcess() {
        return roleInProcess;
    }

    public void setRoleInProcess(String roleInProcess) {
        this.roleInProcess = roleInProcess;
    }


    public Boolean getRequired() {
        return nvl(required, false);
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
