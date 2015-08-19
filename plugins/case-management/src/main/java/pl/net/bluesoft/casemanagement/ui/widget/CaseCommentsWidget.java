package pl.net.bluesoft.casemanagement.ui.widget;

import pl.net.bluesoft.casemanagement.ui.widget.datahandler.CaseCommentDataHandler;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetType;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * Created by pkuciapski on 2014-05-15.
 */
@AliasName(name = "CaseComments", type = WidgetType.Html)
public class CaseCommentsWidget extends ProcessHtmlWidget {

    public CaseCommentsWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("pl/net/bluesoft/rnd/processtool/ui/basewidgets/process-comments.html", bundleResourceProvider));

        addDataHandler(new CaseCommentDataHandler());
    }
}
