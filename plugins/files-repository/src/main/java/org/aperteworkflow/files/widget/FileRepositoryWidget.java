package org.aperteworkflow.files.widget;

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
@AliasName(name = "FileRepositoryWidget", type = WidgetType.Html)
@WidgetGroup("demand-process")
@AperteDoc(humanNameKey="widget.file.repository.name", descriptionKey="widget.file.repository.description")
@ChildrenAllowed(false)
public class FileRepositoryWidget extends ProcessHtmlWidget {
    public FileRepositoryWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("file-repository.html", bundleResourceProvider));
    }
}
