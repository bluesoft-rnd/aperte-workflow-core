package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.validator.SubstitutionCommentValidator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "UserSubstitutionComment")
public class UserSubstitutionComment extends ProcessHtmlWidget
{
    public UserSubstitutionComment(IBundleResourceProvider bundleResourceProvider)
    {
        setContentProvider(new FileWidgetContentProvider("substitution-comments.html", bundleResourceProvider));
        setValidator(new SubstitutionCommentValidator());
        addDataHandler(new SimpleWidgetDataHandler());
    }
}
