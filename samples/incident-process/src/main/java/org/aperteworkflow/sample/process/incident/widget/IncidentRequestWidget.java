package org.aperteworkflow.sample.process.incident.widget;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "OrderWidget")
@WidgetGroup("html-process-widget")
public class IncidentRequestWidget extends ProcessHtmlWidget {
    public IncidentRequestWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("request.html", bundleResourceProvider));
    }
}
