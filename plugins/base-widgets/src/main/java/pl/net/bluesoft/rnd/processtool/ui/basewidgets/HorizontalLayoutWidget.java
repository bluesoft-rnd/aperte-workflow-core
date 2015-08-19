package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;

/**
 * Created by Dominik Dębowczyk on 2015-08-11.
 */
@AliasName(name = "HorizontalLayout")
@AperteDoc(humanNameKey = "widget.horizontal_layout.name", descriptionKey = "widget.horizontal_layout.description")
@ChildrenAllowed(true)
@WidgetGroup("base-widgets")
public class HorizontalLayoutWidget extends ProcessHtmlWidget {

    public HorizontalLayoutWidget() {
    }
}
