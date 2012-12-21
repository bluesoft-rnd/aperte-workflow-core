package pl.net.bluesoft.rnd.processtool.ui.common;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import static com.vaadin.ui.Label.CONTENT_XHTML;

public class FailedProcessToolWidget extends BaseProcessToolWidget implements ProcessToolVaadinRenderable, ProcessToolDataWidget {
    private final Exception e;

    public FailedProcessToolWidget(Exception e) {
        this.e = e;
    }

    @Override
    public String getAttributeValue(String key) {
        return super.getAttributeValue(key);
    }

    @Override
    public Component render() {
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
        return p;
    }

    @Override
    public Collection<String> validateData(BpmTask task, boolean skipRequired) {
        return Arrays.asList("process.data.widget.exception-occurred");
    }

    @Override
    public void saveData(BpmTask task) {
    }

    @Override
    public void loadData(BpmTask task) {
    }

    @Override
    public void addChild(ProcessToolWidget child) {
    }
}
