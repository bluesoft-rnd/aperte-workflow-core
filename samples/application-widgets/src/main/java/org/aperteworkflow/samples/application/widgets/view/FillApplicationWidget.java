package org.aperteworkflow.samples.application.widgets.view;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetType;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * Created by Dominik Dębowczyk on 2015-08-03.
 */
@AliasName(name = "FillApplication", type = WidgetType.Html)
@WidgetGroup("application-process")
public class FillApplicationWidget extends ProcessHtmlWidget {

    public FillApplicationWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("fill-application.html", bundleResourceProvider));
        SimpleWidgetDataHandler simpleWidgetDataHandler = new SimpleWidgetDataHandler();
        addDataHandler(simpleWidgetDataHandler);
    }
}
