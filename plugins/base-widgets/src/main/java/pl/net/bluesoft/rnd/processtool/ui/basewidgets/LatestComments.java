package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "LatestComments")
public class LatestComments extends ProcessHtmlWidget
{
    public LatestComments(IBundleResourceProvider bundleResourceProvider)
    {
        setWidgetName("LatestComments");
        setContentProvider(new FileWidgetContentProvider("latest-process-comments.html", bundleResourceProvider));
    }
}
