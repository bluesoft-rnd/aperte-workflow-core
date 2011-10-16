package pl.net.bluesoft.rnd.pt.ext.userdata.widget;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.pt.ext.userdata.model.ProcessInstanceUserAssignment;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class UserDataWidget
		extends BaseProcessToolWidget
		implements ProcessToolDataWidget, ProcessToolVaadinWidget, Property.ValueChangeListener {

	UserData selectedUser = null;
	private ComboBox combo;
	private Collection<UserData> users;

	@AutoWiredProperty
	private String bpmVariableName;

	@Override
	public Collection<String> validateData(ProcessInstance processInstance) {
		Collection<String> res = new HashSet<String>();
		if ("true".equalsIgnoreCase(getAttributeValue("required")) && selectedUser==null) {
			res.add("ext.userdata.validate.required."+getAttributeValue("role-name"));
		}
		return res;
	}

	@Override
	public void saveData(ProcessInstance processInstance) {
		String role = getAttributeValue("role-name");
		boolean found = false;
		for (ProcessInstanceUserAssignment assign : getAttributes(ProcessInstanceUserAssignment.class, processInstance)) {
			if ((role == null && assign.getRole() == null) || (assign.getRole() != null && assign.getRole().equals(role))) {
				found = true;
				if (selectedUser == null) {
					processInstance.removeAttribute(assign);
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
			assign.setRole(role);
			assign.setKey(role);
			processInstance.addAttribute(assign);
		}
	}

	@Override
	public void loadData(ProcessInstance processInstance) {
		String role = getAttributeValue("role-name");
		for (ProcessInstanceUserAssignment assign : getAttributes(ProcessInstanceUserAssignment.class, processInstance)) {
			if ((role == null && assign.getRole() == null) || (assign.getRole() != null && assign.getRole().equals(role))) {
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
		combo = new ComboBox(i18NSource.getMessage("ext.userdata.prompt." + getAttributeValue("role-name")));

		combo.setItemCaptionPropertyId("description");
		combo.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		combo.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		combo.setImmediate(true);
		combo.addListener(this);
		combo.setNewItemsAllowed(false);
        if ("true".equalsIgnoreCase(getAttributeValue("required")))
            combo.setDescription(i18NSource.getMessage("ext.userdata.validate.required."+getAttributeValue("role-name")));

		users = getUsers();
		BeanItemContainer bic = new BeanItemContainer(users);
		combo.setContainerDataSource(bic);
		bic.sort(new Object[] { "description" }, new boolean[] { true });
//		for (UserData ud : getUsers()) {
//			combo.addItem(ud);
//		}

		if (selectedUser != null && combo != null) {
			for (UserData ud : users) {
				if (selectedUser.getLogin().equals(ud.getLogin())) {
					combo.setValue(ud);
				}
			}
		}
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(combo);
//		combo.setReadOnly(!hasPermission("EDIT"));

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
}
