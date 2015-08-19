package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

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
