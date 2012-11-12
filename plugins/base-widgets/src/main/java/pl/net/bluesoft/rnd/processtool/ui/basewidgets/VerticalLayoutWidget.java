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
public class VerticalLayoutWidget extends BaseProcessToolVaadinWidget implements ProcessToolVaadinRenderable {
    private static final Logger logger = Logger.getLogger(VerticalLayout.class.getName());

    VerticalLayout vl = new VerticalLayout();

    List<ProcessToolVaadinRenderable> widgets = new ArrayList();

    public VerticalLayoutWidget() {
        vl.setMargin(true);
        vl.setSpacing(true);
        vl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
    }

    @Override
    public Component render() {
        vl.removeAllComponents();
        for (ProcessToolVaadinRenderable vChild : widgets) {
            Component component;
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
                p.setHeight(150, Sizeable.UNITS_PIXELS);
                component = p;
            }
			if (component != null) {
				if (vChild instanceof BaseProcessToolVaadinWidget) {
					String comment = ((BaseProcessToolVaadinWidget)vChild).getAttributeValue("comment");
					if (comment != null) {
						VerticalLayout vl = new VerticalLayout();
						vl.addComponent(new Label(getMessage(comment), Label.CONTENT_XHTML));
						vl.addComponent(component);
						component = vl;
					}
					String caption = ((BaseProcessToolVaadinWidget)vChild).getAttributeValue("caption");
					if (caption != null) {
						Panel p = new Panel(getMessage(caption));
						p.addComponent(component);
						vl.addComponent(p);
						vl.setExpandRatio(p, 1.0f);
						p.setWidth(100, Sizeable.UNITS_PERCENTAGE);
					} else 
					{
						vl.addComponent(component);
						vl.setExpandRatio(component, 1.0f);
						
					}
				}  
				else 
				{
					vl.addComponent(component);
					vl.setExpandRatio(component, 1.0f);
				}
			}
        }
        return vl;
    }

    @Override
    public void addChild(ProcessToolWidget child) {
        if (!(child instanceof ProcessToolVaadinRenderable)) {
            throw new IllegalArgumentException("child is not instance of " + ProcessToolVaadinRenderable.class.getName());
        }
        ProcessToolVaadinRenderable vChild = (ProcessToolVaadinRenderable) child;
        widgets.add(vChild);
    }

}
