package pl.net.bluesoft.rnd.processtool.ui.basewidgets;



import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;

import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComments;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.util.lang.FormatUtil;
import pl.net.bluesoft.util.lang.Formats;
import pl.net.bluesoft.util.lang.Lang;


import java.util.*;



import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.ProcessHistoryWidget.label;
import static pl.net.bluesoft.util.lang.Formats.formatFullDate;
import static pl.net.bluesoft.util.lang.Formats.nvl;


/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "ProcessComments")
@AperteDoc(humanNameKey="widget.process_comments.name", descriptionKey="widget.process_comments.description")
@ChildrenAllowed(false)
@PermissionsUsed({
        @Permission(key="ADD", desc="widget.process_comments.permission.desc.ADD"),
        @Permission(key="EDIT", desc="widget.process_comments.permission.desc.EDIT"),
        @Permission(key="EDIT_ALL", desc="widget.process_comments.permission.desc.EDIT_ALL"),
        @Permission(key="VIEW", desc="widget.process_comments.permission.desc.VIEW")
})
@WidgetGroup("base-widgets")
public class ProcessCommentsWidget extends BaseProcessToolVaadinWidget implements ProcessToolVaadinRenderable, ProcessToolDataWidget {

	private BeanItemContainer<ProcessComment> bic = new BeanItemContainer<ProcessComment>(ProcessComment.class);

    @AutoWiredProperty(required = false)
    @AperteDoc(
            humanNameKey="widget.process_comments.property.table.name",
            descriptionKey="widget.process_comments.property.table.description"
    )
	private Boolean table;

	@AutoWiredProperty
	private Boolean mustAddComment;


	private String processState = null;
	private Panel commentsPanel;

	@Override
	public void loadData(BpmTask task) { 
        ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();
		ProcessComments comments = pi.findAttributeByClass(ProcessComments.class);
		if (comments != null) {
			List<ProcessComment> lst = new ArrayList<ProcessComment>(comments.getComments());
			Collections.sort(lst, new Comparator<ProcessComment>() {
				@Override
				public int compare(ProcessComment o1, ProcessComment o2) {
					return -o1.getCreateTime().compareTo(o2.getCreateTime());
				}
			});
			for (ProcessComment cmt : lst) {
				bic.addBean(cmt);
			}
		}
		processState = task.getTaskName();
	}

	@Override
	public Component render() {


		VerticalLayout vl = new VerticalLayout();

		if (hasPermission("ADD", "EDIT", "EDIT_ALL", "VIEW")) {
			if (nvl(table, false)) {
				final Table table = new Table();
				table.setContainerDataSource(bic);
				table.setVisibleColumns(new Object[]{"author", "createTime"});
				table.setWidth("100%");

				table.addGeneratedColumn("author", new Table.ColumnGenerator() {
					@Override
					public Component generateCell(Table source, Object itemId, Object columnId) {
						BeanItem<ProcessComment> item = (BeanItem<ProcessComment>) source.getItem(itemId);
						return new Label(item.getBean().getAuthor().getRealName());
					}
				});

				table.addGeneratedColumn("createTime", new Table.ColumnGenerator() {
					@Override
					public Component generateCell(Table source, Object itemId, Object columnId) {
						BeanItem<ProcessComment> item = (BeanItem<ProcessComment>) source.getItem(itemId);
						return new Label(FormatUtil.formatFullDate(item.getBean().getCreateTime()));
					}

				});

				table.setImmediate(true);
				table.setSelectable(true);

				for (Object o : table.getVisibleColumns()) {
					table.setColumnHeader(o, getMessage("processdata.comments.comment.table." + o));
				}
				vl.addComponent(table);

				table.addListener(
						new ItemClickEvent.ItemClickListener() {
				            @Override
							public void itemClick(ItemClickEvent event) {
								if (event.isDoubleClick()) {
									Component component = event.getComponent();
									final BeanItem<ProcessComment> bi = bic.getItem(event.getItemId());
									displayCommentDetails(component, bi);
								}
							}
						});
				table.setHeight("200px");
			} else {
				commentsPanel = new Panel();
				commentsPanel.setStyleName(Reindeer.PANEL_LIGHT);
				commentsPanel.setWidth("100%");
				commentsPanel.setHeight("240px");

				refreshData();
				vl.addComponent(commentsPanel);
			}
		}

		final Button addCommentButton = new Button(getMessage("processdata.comments.comment.add"));
        addCommentButton.addStyleName("default");
		addCommentButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				ProcessComment pc = new ProcessComment();
                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
				pc.setAuthor(bpmSession.getUser(ctx));
                pc.setAuthorSubstitute(bpmSession.getSubstitutingUser(ctx));
				pc.setCreateTime(new Date());
				pc.setProcessState(processState);

				final BeanItem<ProcessComment> bi = new BeanItem<ProcessComment>(pc);
				displayCommentDetails(addCommentButton, bi);
			}
		});
		vl.addComponent(addCommentButton);
		addCommentButton.setEnabled(isOwner && hasPermission("ADD"));

		return vl;
	}

	private void refreshData() {
		if (commentsPanel == null) return;
		VerticalLayout layout = (VerticalLayout) commentsPanel.getContent();
		layout.removeAllComponents();
		layout.setSpacing(true);

		for (ProcessComment pc : bic.getItemIds()) {
			HorizontalLayout hl;
			hl = new HorizontalLayout();
            hl.addStyleName("comment-header");
			hl.setSpacing(true);
            hl.setWidth("100%");
            String authorLabel = pc.getAuthor() != null ? pc.getAuthor().getRealName() : "System";
            if (pc.getAuthorSubstitute() != null) {
                authorLabel = (pc.getAuthorSubstitute() != null ? pc.getAuthorSubstitute().getRealName() : "System")
                            + " ( " + getMessage("processdata.comments.substituting") + " "
                            + authorLabel
                            + " )";
            }
            hl.addComponent(label("<b class=\"header-author\">" + authorLabel + "</b>", 150));
			hl.addComponent(label("<b class=\"header-time\">" + FormatUtil.formatFullDate(pc.getCreateTime()) + "</b>", 150));
			//			hl.addComponent(label(pc.getComment(), 450));
            Label spacer = new Label("");
            hl.addComponent(spacer);
            hl.setExpandRatio(spacer, 1);
			layout.addComponent(hl);

			hl = new HorizontalLayout();
            hl.addStyleName("comment-body");
            hl.setWidth("100%");
			hl.setSpacing(true);
			hl.setMargin(new Layout.MarginInfo(false, false, true, true));
			Label l = new Label(pc.getBody(), Label.CONTENT_XHTML);
			l.setWidth("100%");
			hl.addComponent(l);
            hl.setExpandRatio(l, 1.0f);
			layout.addComponent(hl);
		}
	}

	private void displayCommentDetails(Component component, final BeanItem<ProcessComment> bi) {
		final Form f = getCommentDetailsForm(bi,
		                                     isOwner &&
											 (hasPermission("EDIT") && Lang.equals(
													 bi.getBean().getAuthor().getId(),
													 bpmSession.getUser(ProcessToolContext.Util.getThreadProcessToolContext()).getId()))
											 || bi.getBean().getId() == null
											 || hasPermission("EDIT_ALL"));

		final Window newCommentWindow = new Window(getMessage("processdata.comments.comment.edit.title"));
		newCommentWindow.setModal(true);

		HorizontalLayout hl = new HorizontalLayout();
		Button okButton = new Button(getMessage("button.ok"));
        okButton.addStyleName("default");
		okButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (f.isValid()) {
					f.commit();
					bic.addBean(bi.getBean());
					refreshData();
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
                    VaadinUtility.validationNotification(getApplication(), i18NSource, sb.toString());
                }
			}
		});
		hl.addComponent(okButton);
		okButton.setEnabled(!f.isReadOnly());
		Button cancelButton = new Button(getMessage("button.cancel"));
        cancelButton.addStyleName("default");
		cancelButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				newCommentWindow.getParent().removeWindow(newCommentWindow);
			}
		});

		hl.addComponent(cancelButton);

		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(new Label(getMessage("process.comments.edit.help"), Label.CONTENT_XHTML));

//        vl.addComponent(new HorizontalLayout() {{
//            addComponent(f);
//            setMargin(true);
//        }});
		vl.addComponent(f);
		vl.addComponent(hl);
		vl.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);
		vl.setMargin(true);
//        hl.setMargin(true);
		hl.setSpacing(true);
//        vl.setExpandRatio(f, 1f);
//        vl.setExpandRatio(hl, 1f);

		vl.setWidth(600, Sizeable.UNITS_PIXELS);
		newCommentWindow.setContent(vl);
		newCommentWindow.setResizable(true);
//        newCommentWindow.setImmediate(true);
		component.getApplication().getMainWindow().addWindow(newCommentWindow);
	}

	private Form getCommentDetailsForm(final BeanItem<ProcessComment> bi, boolean editable) {
		Form f = new Form();
		f.setReadOnly(!editable);
		f.setWriteThrough(false);
		f.setInvalidCommitted(false);

		f.setFormFieldFactory(new DefaultFieldFactory() {

			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
//						Field f = super.createField(item, propertyId, uiContext);
				Field f = null;
				//				if ("comment".equals(propertyId)) {
				//					TextField tf = new TextField();
				//					tf.setRequired(true);
				//					tf.setRequiredError(getMessage("processdata.comments.comment.comment.required"));
				//					tf.setNullRepresentation("");
				//					tf.setInputPrompt(getMessage("processdata.comments.comment.comment.prompt"));
				//					tf.setWidth(400, Sizeable.UNITS_PIXELS);
				//					f = tf;
				//				} else
				if ("body".equals(propertyId)) {
					RichTextArea rta = new RichTextArea();
					rta.setRequired(true);
					rta.setNullRepresentation("");
					rta.setRequiredError(getMessage("processdata.comments.comment.body.required"));
					rta.setWidth(400, Sizeable.UNITS_PIXELS);
                    rta.focus();
					f = rta;
				}
				if (f != null)
					f.setCaption(getMessage("processdata.comments.comment.form." + propertyId));

				return f;
			}
		});
		f.setItemDataSource(bi);
		f.setVisibleItemProperties(Arrays.asList("body"));

		TextField field = new TextField();
		field.setValue(formatFullDate(bi.getBean().getCreateTime()));
		field.setCaption(getMessage("processdata.comments.comment.form.createTime"));
		field.setReadOnly(true);
		f.addField("createTime", field);


		field = new TextField();
		field.setValue(bi.getBean().getAuthor().getRealName());
		field.setCaption(getMessage("processdata.comments.comment.form.author"));
		field.setReadOnly(true);

		f.addField("author", field);
		f.setWidth(600, Sizeable.UNITS_PIXELS);
		if (!editable) {
			for (Object o : f.getItemPropertyIds()) {
				Field tmpField = f.getField(o);
				tmpField.setReadOnly(true);
			}
		}
		return f;
	}

	@Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {
		if ("true".equals(getAttributeValue("mustAddComment"))) {
			//look for a fresh comment or added in this state
			for (ProcessComment pc : bic.getItemIds()) {
				if (pc.getId() == null)
					return null;
				if (task.getTaskName().equals(pc.getProcessState())) {
					return null;
				}
			}
			return Arrays.asList(getMessage("please.add.comment"));
		} else {
			return null;
		}

	}

	@Override
	public void saveData(BpmTask task) {
        ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();
		ProcessComments comments = pi.findAttributeByClass(ProcessComments.class);
		if (comments == null) {
			comments = new ProcessComments();
			comments.setProcessInstance(pi);
			comments.setKey(ProcessComments.class.getName());
			pi.getProcessAttributes().add(comments);
		}
		comments.setComments(new HashSet());
		for (ProcessComment pc : bic.getItemIds()) {
			comments.getComments().add(pc);
			pc.setComments(comments);
		}
	}

	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("children are not supported in this widget");
	}

	public String getCaption() {
		return caption;
    }

	public void setCaption(String caption) {
		this.caption = caption;
    }

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getMustAddComment() {
		return mustAddComment;
	}

	public void setMustAddComment(Boolean mustAddComment) {
		this.mustAddComment = mustAddComment;
	}


}
