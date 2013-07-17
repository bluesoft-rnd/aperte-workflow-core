package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * 
 * Comment dialog with css layout for form
 * 
 * @author polszewski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class AddCommentDialog extends DialogWindow implements ClickListener
{
	private static final String PROCESS_COMENT_LAYOUT = "process-comment-layout";
	private static final String PROCESS_COMENT_FORM_LAYOUT = "process-comment-form-layout";
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
	protected AbstractLayout createContent() {
		CssLayout vl = new CssLayout();
		vl.addStyleName(PROCESS_COMENT_LAYOUT);
		vl.setMargin(true);
		//vl.setWidth(600, Sizeable.UNITS_PIXELS);
		vl.addComponent(new Label(getHelpContents(), Label.CONTENT_XHTML));
		vl.addComponent(form = getCommentDetailsForm());
		return vl;
	}

	@Override
	protected Button[] createActionButtons() 
	{
		addButton = new Button(getConfirmButtonCaption());
		addButton.addListener((ClickListener)this);
		
		cancelButton = new Button(getCancelButtonCaption());
		cancelButton.addListener((ClickListener)this);
		
		
		return new Button[] {addButton, cancelButton};
	}

	protected Form getCommentDetailsForm() {
		BeanItem<ProcessComment> bi = new BeanItem<ProcessComment>(processComment = new ProcessComment());

		CommentForm f = new CommentForm();
		f.setFormFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				if ("body".equals(propertyId)) {
					RichTextArea rta = new RichTextArea();
					rta.setRequired(true);
					rta.setNullRepresentation("");
					//rta.setWidth(100, Sizeable.UNITS_PERCENTAGE);
					setupCommentField(propertyId, rta);
					return rta;
				}
				return null;
			}
		});
		f.setItemDataSource(bi);
		return f;
	}
	
	private class CommentForm extends Form
	{
		private CssLayout layout;
		
		public CommentForm()
		{
			layout = new CssLayout();
			layout.addStyleName(PROCESS_COMENT_FORM_LAYOUT);
			
			setLayout(layout);
			
			setWriteThrough(false);
			setInvalidCommitted(false);

			setVisibleItemProperties(Arrays.asList("body"));
		}
		
		@Override
		protected void attachField(Object propertyId, Field field) 
		{
			layout.addComponent(field);
		}
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

	@Override
	public void buttonClick(ClickEvent event) 
	{
		if(event.getButton().equals(addButton))
		{
			getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
				@Override
				public void withContext(ProcessToolContext ctx) {
					if (form.isValid()) {
						form.commit();
						closeWindow();
						handleAddComment();
					}
					else {
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
		else if(event.getButton().equals(cancelButton))
		{
			closeWindow();
		}
		
	}
}
