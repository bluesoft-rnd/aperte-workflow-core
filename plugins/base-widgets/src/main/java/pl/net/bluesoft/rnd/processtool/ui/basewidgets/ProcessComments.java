package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * User: POlszewski
 * Date: 2013-10-14
 * Time: 14:20
 */
@AliasName(name = "ProcessComments")
public class ProcessComments extends ProcessHtmlWidget {
	public ProcessComments(IBundleResourceProvider bundleResourceProvider) {
		setContentProvider(new FileWidgetContentProvider("process-comments.html", bundleResourceProvider));
	}
}
