package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;


@AliasName(name = "ProcessDiagramWidget")
@WidgetGroup("html-process-widget")
public class ProcessDiagramWidget extends ProcessHtmlWidget {

    @AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "widget.process_diagram.width.humanName",
            descriptionKey = "widget.process_diagram.width.description"
    )
    protected String diagramWidth;

    @AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "widget.process_diagram.height.humanName",
            descriptionKey = "widget.process_diagram.height.description"
    )
    protected String diagramHeight;

    @AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "widget.process_diagram.color.humanName",
            descriptionKey = "widget.process_diagram.color.description"
    )
    protected String visitedColor;

	public ProcessDiagramWidget(IBundleResourceProvider bundleResourceProvider) {
        addDataHandler(new SimpleWidgetDataHandler());
        setContentProvider(new FileWidgetContentProvider("process-diagram.html", bundleResourceProvider));		
    }
}


