package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.datahandler.CommentDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * User: POlszewski
 * Date: 2013-10-14
 * Time: 14:20
 */
@AliasName(name = "ProcessComments")
public class ProcessComments extends ProcessHtmlWidget
{
    private static final String TYPE_COMMENT = "comment";

	public ProcessComments(IBundleResourceProvider bundleResourceProvider) {
		setContentProvider(new FileWidgetContentProvider("pl/net/bluesoft/rnd/processtool/ui/basewidgets/process-comments.html", bundleResourceProvider));

        addDataHandler(new CommentDataHandler());
	}
}
