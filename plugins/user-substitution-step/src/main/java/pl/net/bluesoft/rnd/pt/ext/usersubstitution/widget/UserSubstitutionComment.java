package pl.net.bluesoft.rnd.pt.ext.usersubstitution.widget;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;
import pl.net.bluesoft.rnd.pt.ext.usersubstitution.widget.validator.SubstitutionCommentValidator;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "UserSubstitutionComment")
public class UserSubstitutionComment extends ProcessHtmlWidget
{
    public UserSubstitutionComment(IBundleResourceProvider bundleResourceProvider)
    {
        setWidgetName("UserSubstitutionComment");
        setContentProvider(new FileWidgetContentProvider("substitution-comments.html", bundleResourceProvider));
        setValidator(new SubstitutionCommentValidator());
        setDataHandler(new SimpleWidgetDataHandler());
    }
}
