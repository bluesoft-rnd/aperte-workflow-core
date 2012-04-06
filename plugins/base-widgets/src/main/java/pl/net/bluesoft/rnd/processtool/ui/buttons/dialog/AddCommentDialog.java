package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 09:25
 */
public class AddCommentDialog extends DialogWindow {
	protected ProcessComment processComment;
	
	protected Form form;
	protected Button addButton;
	protected Button cancelButton;
	
	public interface AddCommentListener {
		void onCommentAdded();			
	}
	
	private Set<AddCommentListener> addCommentListeners = new HashSet<AddCommentListener>();
		
	public AddCommentDialog(ProcessComment processComment) {
		this.processComment = processComment;
	}
	
	public void addListener(AddCommentListener listener) {
		addCommentListeners.add(listener);
	}
	
	@Override
	protected String getTitle() {
		return getMessage("processdata.comments.comment.add.title");
	}

	@Override
	protected AbstractOrderedLayout createContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(true);
		vl.setWidth(600, Sizeable.UNITS_PIXELS);
		vl.addComponent(new Label(getHelpContents(), Label.CONTENT_XHTML));
		vl.addComponent(form = getCommentDetailsForm());
		return vl;
	}

	@Override
	protected Button[] createActionButtons() {
		return new Button[] {
				addButton = createConfirmButton(),
				cancelButton = createActionButton(getCancelButtonCaption())
		};
	}

	protected Form getCommentDetailsForm() {
		BeanItem<ProcessComment> bi = new BeanItem<ProcessComment>(processComment = new ProcessComment());

		Form f = new Form();
		f.setWriteThrough(false);
		f.setInvalidCommitted(false);
		f.setFormFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				if ("body".equals(propertyId)) {
					RichTextArea rta = new RichTextArea();
					rta.setRequired(true);
					rta.setNullRepresentation("");
					rta.setWidth(400, Sizeable.UNITS_PIXELS);
					setupCommentField(propertyId, rta);
					return rta;
				}
				return null;
			}
		});
		f.setItemDataSource(bi);
		f.setVisibleItemProperties(Arrays.asList("body"));
		f.setWidth(600, Sizeable.UNITS_PIXELS);

		return f;
	}

	protected Button createConfirmButton() {
		return VaadinUtility.button(getConfirmButtonCaption(), null, "default", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (form.isValid()) {
                    form.commit();
                    closeWindow();
                    handleAddComment();
                } else {
                    StringBuilder sb = new StringBuilder("<ul>");
                    for (Object propertyId : form.getItemPropertyIds()) {
                        Field field = form.getField(propertyId);
                        if (!field.isValid() && field.isRequired()) {
                            sb.append("<li>").append(field.getRequiredError()).append("</li>");
                        }
                    }
                    sb.append("</ul>");
                    VaadinUtility.validationNotification(getApplication(), i18NSource, sb.toString());
                }
            }
        });
	}

	protected void handleAddComment() {
		for (AddCommentListener listener : addCommentListeners) {
			listener.onCommentAdded();
		}
	}	
			
	protected void setupCommentField(Object propertyId, RichTextArea rta) {
		rta.setRequiredError(getMessage("processdata.comments.comment.body.required"));
		rta.setCaption(getMessage("processdata.comments.comment.form." + propertyId));
        rta.focus();
	}

	protected String getHelpContents() {
		return getMessage("process.comments.edit.help");
	}

	protected String getConfirmButtonCaption() {
		return getMessage("button.ok");
	}

	protected String getCancelButtonCaption() {
		return getMessage("button.cancel");
	}

	public ProcessComment getProcessComment() {
		return processComment;
	}

	public Button getAddButton() {
		return addButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}
}
