package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.aperteworkflow.util.liferay.LiferayBridge;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.util.lang.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 11:24
 */
public class AssignUserTaskDialog extends AddCommentDialog {
	private AssigneeBean assigneeBean;
	private String roleName;

	public class AssigneeBean {
		private UserData assignee;
		private String comment;

		public void setAssignee(UserData assignee) {
			this.assignee = assignee;
		}
		public UserData getAssignee() {
			return assignee;
		}
		public void setComment(String comment) {
			this.comment = comment;
		}
		public String getComment() {
			return comment;
		}
	}
	
	public AssignUserTaskDialog(ProcessComment processComment, String roleName) {
		super(processComment);
		this.roleName = roleName;
	}

	@Override
	protected String getTitle() {
		return getMessage("processdata.comments.assignee.title");
	}

	@Override
	protected String getHelpContents() {
		return getMessage("processdata.comments.assignee.help");
	}

	protected Form getCommentDetailsForm() {
		BeanItem<AssigneeBean> bi = new BeanItem<AssigneeBean>(assigneeBean = new AssigneeBean());

		Form f = new Form() {
			@Override
			public void commit() throws SourceException, Validator.InvalidValueException {
				super.commit();
				processComment.setBody(assigneeBean.getComment());
			}
		};
		f.setWriteThrough(false);
		f.setInvalidCommitted(false);
		f.setFormFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				Field f = null;
				if ("comment".equals(propertyId)) {
					RichTextArea rta = new RichTextArea();
					rta.setRequired(true);
					rta.setNullRepresentation("");
					rta.setWidth(400, Sizeable.UNITS_PIXELS);
                    rta.focus();
					f = rta;
				}
				else if ("assignee".equals(propertyId)) {
					Select users = new Select();
					List<UserData> allUsers = Strings.hasText(roleName) ? LiferayBridge.getUsersByRole(roleName) : LiferayBridge.getAllUsers();
					BeanItemContainer<UserData> ds = new BeanItemContainer<UserData>(UserData.class, allUsers);
					ds.sort(new Object[] { "realName" }, new boolean[] { true });
					users.setNullSelectionAllowed(false);
					users.setContainerDataSource(ds);
					users.setItemCaptionPropertyId("filteredName");
					users.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
					users.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
					users.setImmediate(true);
					users.setWidth(400, Sizeable.UNITS_PIXELS);
					users.setRequired(true);
					f = users;
				}
				if (f != null) {
					f.setCaption(getMessage("processdata.comments.assignee.form." + propertyId));
					f.setRequiredError(getMessage("processdata.comments.assignee.form." + propertyId + ".required"));
				}

				return f;
			}
		});
		f.setItemDataSource(bi);
		f.setVisibleItemProperties(Arrays.asList("assignee", "comment"));
		f.setWidth(600, Sizeable.UNITS_PIXELS);

		return f;
	}

	public AssigneeBean getAssigneeBean() {
		return assigneeBean;
	}
}
