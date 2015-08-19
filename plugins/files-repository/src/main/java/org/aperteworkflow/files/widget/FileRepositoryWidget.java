package org.aperteworkflow.files.widget;

import org.aperteworkflow.files.widget.dataprovider.FilesRepositoryDataProvider;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * History process widget.
 * <p/>
 * Refactored for css layout
 *
 * @author mpawlak@bluesoft.net.pl
 */
@AliasName(name = "FileRepositoryWidget", type = WidgetType.Html)
@WidgetGroup("common")
@AperteDoc(humanNameKey = "widget.file.repository.name", descriptionKey = "widget.file.repository.description")
@ChildrenAllowed(false)
public class FileRepositoryWidget extends ProcessHtmlWidget
{
    public static enum Mode{
        /** Simple mode */
        STANDARD,
        /** Add additional column "send with mail" */
        MAIL
    };

    @AutoWiredProperty
    private String mode;

	@AutoWiredProperty
	private String hideMailAttachments;

    public FileRepositoryWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("files-repository-widget.html", bundleResourceProvider));
        addDataProvider(new FilesRepositoryDataProvider());
        addDataHandler(new SimpleWidgetDataHandler());
    }
}
