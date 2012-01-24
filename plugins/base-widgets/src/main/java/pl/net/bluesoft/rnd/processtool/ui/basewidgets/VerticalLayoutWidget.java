package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vaadin.ui.Label.CONTENT_XHTML;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "VerticalLayout")
@AperteDoc(humanNameKey="widget.vertical_layout.name", descriptionKey="widget.vertical_layout.description")
@ChildrenAllowed(true)
@WidgetGroup("base-widgets")
public class VerticalLayoutWidget extends BaseProcessToolWidget implements ProcessToolVaadinWidget {
    private static final Logger logger = Logger.getLogger(VerticalLayout.class.getName());

    @AutoWiredProperty
    private String caption;

    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = RichTextArea.class)
    private String comment;

    VerticalLayout vl = new VerticalLayout();

    List<ProcessToolVaadinWidget> widgets = new ArrayList();

    public VerticalLayoutWidget() {
        vl.setMargin(true);
        vl.setSpacing(true);
        vl.setWidth(100, AbstractComponent.UNITS_PERCENTAGE);
    }

    @Override
    public Component render() {
        vl.removeAllComponents();
        for (ProcessToolVaadinWidget vChild : widgets) {
            Component component;
            try {
                component = vChild.render();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                Panel p = new Panel();
                VerticalLayout vl = new VerticalLayout();
                vl.addComponent(new Label(getMessage("process.data.widget.exception-occurred")));
                vl.addComponent(new Label(e.getMessage()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                e.printStackTrace(new PrintWriter(baos));
                vl.addComponent(new Label("<pre>" + baos.toString() + "</pre>", CONTENT_XHTML));
                vl.addStyleName("error");
                p.addComponent(vl);
                p.setHeight("150px");
                component = p;
            }
            String comment = vChild.getAttributeValue("comment");
            if (comment != null) {
                VerticalLayout vl = new VerticalLayout();
                vl.addComponent(new Label(getMessage(comment), Label.CONTENT_XHTML));
                vl.addComponent(component);
                component = vl;
            }
            String caption = vChild.getAttributeValue("caption");
            if (caption != null) {
                Panel p = new Panel(getMessage(caption));
                p.addComponent(component);
                vl.addComponent(p);
                vl.setExpandRatio(p, 1.0f);
                p.setWidth(100, AbstractComponent.UNITS_PERCENTAGE);
            } else {
                vl.addComponent(component);
            }
        }
        return vl;
    }

    @Override
    public void addChild(ProcessToolWidget child) {
        if (!(child instanceof ProcessToolVaadinWidget)) {
            throw new IllegalArgumentException("child is not instance of " + ProcessToolVaadinWidget.class.getName());
        }
        ProcessToolVaadinWidget vChild = (ProcessToolVaadinWidget) child;
        widgets.add(vChild);
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
}
