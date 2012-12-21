package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vaadin.ui.Label.CONTENT_XHTML;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "TabSheet")
@AperteDoc(humanNameKey="widget.tab_sheet.name", descriptionKey="widget.tab_sheet.description")
@ChildrenAllowed(true)
@WidgetGroup("base-widgets")
public class TabSheetWidget extends BaseProcessToolVaadinWidget implements ProcessToolVaadinRenderable {

	private static final Logger logger = Logger.getLogger(TabSheetWidget.class.getName());
	private TabSheet ts = new TabSheet();


	@Override
	public Component render() {
		ts.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		return ts;
	}

	@Override
	public void addChild(ProcessToolWidget child) {
		if (!(child instanceof ProcessToolVaadinWidget)) {
			throw new IllegalArgumentException("child is not instance of " + ProcessToolVaadinWidget.class.getName());
		}

		Component component;
		ProcessToolVaadinWidget vChild = (ProcessToolVaadinWidget) child;
		try {
			component = vChild.render();
		} catch (Throwable e) {
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
		ts.addTab(component, getMessage(child.getAttributeValue("caption")), null);
		if (ts.getSelectedTab() == null)
			ts.setSelectedTab(component);

	}

}
