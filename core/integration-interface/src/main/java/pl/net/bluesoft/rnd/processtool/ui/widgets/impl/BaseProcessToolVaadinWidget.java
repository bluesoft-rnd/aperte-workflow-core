package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;


import com.vaadin.ui.RichTextArea;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredPropertyConfigurator;

/**
 * This is a basic widget for all the widgets that want to use Vaadin as their RIA library. It is
 * recommended to always extend this class when developing custom widgets.
 */
public abstract class BaseProcessToolVaadinWidget extends BaseProcessToolWidget implements ProcessToolVaadinWidget {

    /**
     * Widget caption text
     */
    @AutoWiredProperty
    @AperteDoc(
        humanNameKey = "widget.attribute.caption.humanName",
        descriptionKey = "widget.attribute.caption.description"
    )
    protected String caption;

    /**
     * Widget comment text
     */
    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = RichTextArea.class)
    @AperteDoc(
        humanNameKey = "widget.attribute.comment.humanName",
        descriptionKey = "widget.attribute.comment.description"
    )
    protected String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

}
