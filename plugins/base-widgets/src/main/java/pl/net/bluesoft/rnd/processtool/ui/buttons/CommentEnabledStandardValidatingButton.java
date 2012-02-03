package pl.net.bluesoft.rnd.processtool.ui.buttons;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComments;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionCallback;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */

@AliasName(name = "CommentButton")
public class CommentEnabledStandardValidatingButton extends StandardValidatingButton {

	private ProcessComment pc;

	@Override
	public void onButtonPress(final ProcessInstance processInstance,
	                          final ProcessToolContext ctx,
	                          final Set<ProcessToolDataWidget> dataWidgets,
	                          final Map<ProcessToolDataWidget,
			                          Collection<String>> validationErrors,
	                          final ProcessToolActionCallback callback) {
		if (validationErrors.isEmpty()) {
			final Window newCommentWindow = new Window(i18NSource.getMessage("processdata.comments.comment.add.title"));
			newCommentWindow.setModal(true);

			pc = new ProcessComment();
			final Form f = getCommentDetailsForm(new BeanItem<ProcessComment>(pc));
			HorizontalLayout hl = new HorizontalLayout();
			Button okButton = new Button(i18NSource.getMessage("button.ok"));
            okButton.addStyleName("default");
			okButton.addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					if (f.isValid()) {
						f.commit();
						ProcessInstance pi = ProcessToolContext.Util.getProcessToolContextFromThread().getProcessInstanceDAO()
                                .getProcessInstance(processInstance.getId());
						CommentEnabledStandardValidatingButton.super.onButtonPress(pi, ProcessToolContext.Util.getProcessToolContextFromThread(),
						                                                           dataWidgets,
						                                                           validationErrors,
						                                                           callback);
						newCommentWindow.getParent().removeWindow(newCommentWindow);
					}
                    else {
                        StringBuilder sb = new StringBuilder("<ul>");
                        for (Object propertyId : f.getItemPropertyIds()) {
                            Field field = f.getField(propertyId);
                            if (!field.isValid() && field.isRequired()) {
                                sb.append("<li>").append(field.getRequiredError()).append("</li>");
                            }
                        }
                        sb.append("</ul>");
                        VaadinUtility.validationNotification(application, i18NSource, sb.toString());
                    }

				}
			});
			hl.addComponent(okButton);
			Button cancelButton = new Button(i18NSource.getMessage("button.cancel"));
            cancelButton.addStyleName("default");
			cancelButton.addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					newCommentWindow.getParent().removeWindow(newCommentWindow);
				}
			});

			hl.addComponent(cancelButton);

			VerticalLayout vl = new VerticalLayout();
			vl.addComponent(new Label(i18NSource.getMessage("process.comments.edit.help"), Label.CONTENT_XHTML));

			vl.addComponent(f);
			vl.addComponent(hl);
			vl.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);
			vl.setMargin(true);
			hl.setSpacing(true);

			vl.setWidth(600, Sizeable.UNITS_PIXELS);
			newCommentWindow.setContent(vl);
			newCommentWindow.setResizable(true);
			application.getMainWindow().addWindow(newCommentWindow);
		} else {
			super.onButtonPress(processInstance, ctx, dataWidgets, validationErrors, callback);

		}


	}

	@Override
	public void saveData(ProcessInstance pi) {
		pc.setAuthor(ProcessToolContext.Util.getProcessToolContextFromThread().getUserDataDAO().loadOrCreateUserByLogin(loggedUser));
		pc.setCreateTime(new Date());
		pc.setProcessState(pi.getState());
		ProcessComments comments = getCommentsAttribute(pi);
		if (comments == null) {
			comments = new ProcessComments();
			comments.setProcessInstance(pi);
			comments.setKey(ProcessComments.class.getName());
			pi.getProcessAttributes().add(comments);
		}
		pc.setComments(comments);
		comments.getComments().add(pc);
	}

	private Form getCommentDetailsForm(final BeanItem<ProcessComment> bi) {
		Form f = new Form();
		f.setWriteThrough(false);
		f.setInvalidCommitted(false);

		f.setFormFieldFactory(new DefaultFieldFactory() {

			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				Field f = null;
				if ("comment".equals(propertyId)) {
					TextField tf = new TextField();
					tf.setRequired(true);
					tf.setRequiredError(i18NSource.getMessage("processdata.comments.comment.comment.required"));
					tf.setNullRepresentation("");
					tf.setInputPrompt(i18NSource.getMessage("processdata.comments.comment.comment.prompt"));
					tf.setWidth(400, Sizeable.UNITS_PIXELS);
					f = tf;
				} else if ("body".equals(propertyId)) {
					RichTextArea rta = new RichTextArea();
					rta.setRequired(true);
					rta.setNullRepresentation("");
					rta.setRequiredError(i18NSource.getMessage("processdata.comments.comment.body.required"));
					rta.setWidth(400, Sizeable.UNITS_PIXELS);
					f = rta;
				}
				if (f != null)
					f.setCaption(i18NSource.getMessage("processdata.comments.comment.form." + propertyId));

				return f;
			}
		});
		f.setItemDataSource(bi);
		f.setVisibleItemProperties(Arrays.asList("comment", "body"));

		f.setWidth(600, Sizeable.UNITS_PIXELS);
		return f;
	}

	private ProcessComments getCommentsAttribute(ProcessInstance processInstance) {
		ProcessComments comments = null;
		Set<ProcessInstanceAttribute> attributeSet = processInstance.getProcessAttributes();
		for (ProcessInstanceAttribute attr : attributeSet) {
			if (attr instanceof ProcessComments) {
				comments = (ProcessComments) attr;
				break;
			}
		}
		return comments;
	}

}
