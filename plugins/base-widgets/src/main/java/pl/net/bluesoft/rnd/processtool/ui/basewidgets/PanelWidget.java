package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="CaptionPanel")
@WidgetGroup("base-widgets")
public class PanelWidget extends BaseProcessToolVaadinWidget {

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

}
