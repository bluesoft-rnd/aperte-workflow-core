package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.TaskState;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwysocki_bls
 * Date: 8/19/11
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class TasksFilterFieldFactory extends DefaultFieldFactory {
	final private static String PREFIX = "tasks.filter.";
	private TasksFilterBox parent;

	public TasksFilterFieldFactory(TasksFilterBox parent) {
		this.parent = parent;
	}

	@Override
	public Field createField(Item item, Object propertyId, Component uiContext) {
		AbstractField field;
		if (Arrays.asList("creators", "owners", "queues", "states").contains(propertyId)) {
			ListSelect select = new ListSelect(getMessage(PREFIX + propertyId));
			select.setMultiSelect(true);
			select.setRows(5);
			select.setNullSelectionAllowed(true);
			select.setNullSelectionItemId(null);
			if ("owners".equals(propertyId) || "creators".equals(propertyId)) {
				List<UserData> users = getUsers();
				BeanItemContainer bic = new BeanItemContainer<UserData>(UserData.class, users);
				select.setContainerDataSource(bic);
				select.setItemCaptionPropertyId("filteredName");
			} else if ("queues".equals(propertyId)) {
				Collection<ProcessQueue> queues = getQueues();
				for (ProcessQueue queue : queues) {
					select.addItem(queue.getName());
					select.setItemCaption(queue.getName(), queue.getDescription());
				}
			} else if ("states".equals(propertyId)) {
				select.addItem(TaskState.OPEN);
				select.setItemCaption(TaskState.OPEN, getMessage(PREFIX + propertyId + ".open"));

				select.addItem(TaskState.CLOSED);
				select.setItemCaption(TaskState.CLOSED, getMessage(PREFIX + propertyId + ".closed"));
			}
			field = select;
		} else if (Arrays.asList("createdBefore", "createdAfter", "notUpdatedAfter", "updatedAfter").contains(propertyId)) {
			DateField dateField = new DateField(getMessage(PREFIX + propertyId));
			dateField.setResolution(DateField.RESOLUTION_HOUR);
			field = dateField;
		} else {
            field = (AbstractField) super.createField(item, propertyId, uiContext);
        }

		field.setRequiredError(getMessage("error.field.start") + field.getCaption() + getMessage("error.field.is.required.end"));
		field.setInvalidCommitted(true);
		field.setInvalidAllowed(true);
		field.setImmediate(true);
		if (field instanceof AbstractTextField && "null".equals(((AbstractTextField) field).getNullRepresentation()))
			((AbstractTextField) field).setNullRepresentation("");


		return field;
	}

	private Collection<ProcessQueue> queues;
	private List<UserData> users;

	private Collection<ProcessQueue> getQueues() {
		if (queues == null) {
			ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
			queues = parent.getSession().getUserAvailableQueues(ctx);
		}
		return queues;
	}

	private List<UserData> getUsers() {
		if (users == null) {
			ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
			users = ctx.getUserDataDAO().findAll();
		}
		return users;
	}

	public String getMessage(String key) {
		return parent.getMessage(key);
	}
}
