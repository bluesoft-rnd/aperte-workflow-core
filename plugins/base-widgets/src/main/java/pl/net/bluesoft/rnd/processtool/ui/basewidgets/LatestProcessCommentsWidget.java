package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
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

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.ProcessHistoryWidget.label;

@AliasName(name = "LatestComments")
@AperteDoc(humanNameKey="widget.latest_process_comments.name", descriptionKey="widget.latest_process_comments.description")
@ChildrenAllowed(false)
public class LatestProcessCommentsWidget extends BaseProcessToolVaadinWidget implements ProcessToolVaadinRenderable, ProcessToolDataWidget {
	@AutoWiredProperty(required = false)
	@AperteDoc(humanNameKey="widget.latest_process_comments.property.displayed_comments.name", descriptionKey="widget.latest_process_comments.property.displayed_comments.description")
	private int displayedComments = 1;

    public static final String MODE_DEFAULT = "DEFAULT";
    public static final String MODE_SMART = "SMART";

    @AutoWiredProperty(required = false)
    private String mode = MODE_DEFAULT;

    private boolean showContent = true;

    private BeanItemContainer<ProcessComment> bic = new BeanItemContainer<ProcessComment>(ProcessComment.class);

    @Override
	public void loadData(BpmTask task) {
        ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();

        if (mode.equalsIgnoreCase(MODE_SMART)) {
            if (!"true".equalsIgnoreCase(pi.getSimpleAttributeValue("commentAdded", "false"))) {
                showContent = false;
            }
        }

        bic.removeAllItems();
		ProcessComments comments = pi.findAttributeByClass(ProcessComments.class);
        if (comments != null) {
            List<ProcessComment> lst = new ArrayList<ProcessComment>(comments.getComments());
            Collections.sort(lst, new Comparator<ProcessComment>() {
                @Override
                public int compare(ProcessComment o1, ProcessComment o2) {
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
            });
            for (int i = 0; i < displayedComments && i < lst.size(); ++i) {
                bic.addBean(lst.get(i));
            }
        }
    }

    @Override
    public Component render() {
        if (!showContent) {
            return null;
        }

        Panel commentsPanel = new Panel();
        commentsPanel.setStyleName("borderless light");
        commentsPanel.setWidth("100%");

        VerticalLayout layout = new VerticalLayout();
        layout.removeAllComponents();
        layout.setSpacing(true);

        if (bic.getItemIds().isEmpty()) {
            Label l = new Label(getMessage("processdata.comments.empty"));
            l.setWidth("100%");
            layout.addComponent(l);
        }
        else {
			boolean first = true;
            for (ProcessComment pc : bic.getItemIds()) {
				VerticalLayout vl = new VerticalLayout();

                HorizontalLayout hl;
                hl = new HorizontalLayout();
                hl.setSpacing(true);
                String authorLabel = pc.getAuthor() != null ? pc.getAuthor().getRealName() : "System";
                if (pc.getAuthorSubstitute() != null) {
                    authorLabel = (pc.getAuthorSubstitute() != null ? pc.getAuthorSubstitute().getRealName() : "System")
                                + " ( " + getMessage("processdata.comments.substituting") + " "
                                + authorLabel
                                + " )";
                }
                hl.addComponent(label("<b>" + authorLabel + "</b>", 150));
                hl.addComponent(label("<b>" + FormatUtil.formatFullDate(pc.getCreateTime()) + "</b>", 150));
				//                hl.addComponent(label(pc.getComment(), 450));
                vl.addComponent(hl);

                hl = new HorizontalLayout();
                hl.setWidth("100%");
                hl.setSpacing(true);
                hl.setMargin(new Layout.MarginInfo(false, false, true, true));
                Label l = new Label(pc.getBody(), Label.CONTENT_XHTML);
                l.setWidth("100%");
                hl.addComponent(l);
                hl.setExpandRatio(l, 1.0f);
                vl.addComponent(hl);

				if (first) {
					vl.addStyleName("latest-comment-indication");
					first = false;
				}
				layout.addComponent(vl);
            }
        }

        commentsPanel.setContent(layout);

        return commentsPanel;
    }

    @Override
    public boolean hasVisibleData() {
        return !bic.getItemIds().isEmpty();
    }

    @Override
	public void saveData(BpmTask task) {
    }

    @Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {
        return null;
    }

    @Override
    public void addChild(ProcessToolWidget child) {
        throw new IllegalArgumentException("children are not supported in this widget");
    }

    public Integer getDisplayedComments() {
        return displayedComments;
    }

    public void setDisplayedComments(Integer displayedComments) {
        this.displayedComments = displayedComments;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
