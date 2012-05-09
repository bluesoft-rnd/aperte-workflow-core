package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="CaptionPanel")
public class PanelWidget extends BaseProcessToolVaadinWidget implements ProcessToolVaadinRenderable {

	Panel panel = new Panel();
	
	@Override
	public Component render() {
		return panel;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

    @Override
    public void addChild(ProcessToolWidget child) {
		if (!(child instanceof ProcessToolVaadinRenderable)) {
			throw new IllegalArgumentException("child is not instance of " + ProcessToolVaadinRenderable.class.getName());
        }
		ProcessToolVaadinRenderable vChild = (ProcessToolVaadinRenderable) child;
        Component component = vChild.render();
        panel.addComponent(component);
    }

}
