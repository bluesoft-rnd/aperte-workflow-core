package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RichTextArea;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredPropertyConfigurator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="CaptionPanel")
@WidgetGroup("base-widgets")
public class PanelWidget extends BaseProcessToolWidget implements ProcessToolVaadinWidget {

	@AutoWiredProperty
	private String caption;

    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = RichTextArea.class)
    private String comment;

	Panel panel = new Panel();
	
	@Override
	public Component render() {
		return panel;
	}

    @Override
    public void addChild(ProcessToolWidget child) {
        if (!(child instanceof ProcessToolVaadinWidget)) {
            throw new IllegalArgumentException("child is not instance of " + ProcessToolVaadinWidget.class.getName());
        }
        ProcessToolVaadinWidget vChild = (ProcessToolVaadinWidget) child;
        Component component = vChild.render();
        panel.addComponent(component);
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
