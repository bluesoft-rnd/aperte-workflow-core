package pl.net.bluesoft.casemanagement.ui.widget;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * Created by pkuciapski on 2014-04-29.
 */
@AliasName(name = "CaseDataWidget", type = WidgetType.Html)
@WidgetGroup("common")
@AperteDoc(humanNameKey = "widget.case.management.data.name", descriptionKey = "widget.case.management.data.description")
@ChildrenAllowed(false)
public class CaseDataWidget extends ProcessHtmlWidget {
    public CaseDataWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("case-data-widget.html", bundleResourceProvider));
        // addDataProvider();
    }
}
