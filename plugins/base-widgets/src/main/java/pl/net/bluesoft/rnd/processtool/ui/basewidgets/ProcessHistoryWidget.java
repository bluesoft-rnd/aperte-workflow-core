package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;
import pl.net.bluesoft.util.lang.FormatUtil;
import pl.net.bluesoft.util.lang.Strings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * 
 * History process widget. 
 * 
 * Refactored for css layout
 *
 * @author mpawlak@bluesoft.net.pl
 */
@AliasName(name = "ProcessHistoryWidget", type = WidgetType.Html)
@WidgetGroup("common")
@AperteDoc(humanNameKey="widget.process_history.name", descriptionKey="widget.process_history.description")
@ChildrenAllowed(false)
public class ProcessHistoryWidget extends ProcessHtmlWidget {
    public ProcessHistoryWidget(IBundleResourceProvider bundleResourceProvider) {

        setContentProvider(new FileWidgetContentProvider("process-history.html", bundleResourceProvider));
    }
}
