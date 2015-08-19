package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.dataprovider.LatestCommentsDataProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "LatestComments")
public class LatestComments extends ProcessHtmlWidget
{
	@AutoWiredProperty(required = false)
	@AperteDoc(humanNameKey="widget.latest_process_comments.property.displayed_comments.name", descriptionKey="widget.latest_process_comments.property.displayed_comments.description")
	private int displayedComments = 1;

    public LatestComments(IBundleResourceProvider bundleResourceProvider)
    {
        setContentProvider(new FileWidgetContentProvider("latest-process-comments.html", bundleResourceProvider));

        addDataProvider(new LatestCommentsDataProvider());
    }
}
