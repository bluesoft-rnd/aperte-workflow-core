package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;


@AliasName(name = "ProcessDiagramWidget")
@WidgetGroup("html-process-widget")
public class ProcessDiagramWidget extends ProcessHtmlWidget {
	public ProcessDiagramWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("process-diagram.html", bundleResourceProvider));
    }
}


