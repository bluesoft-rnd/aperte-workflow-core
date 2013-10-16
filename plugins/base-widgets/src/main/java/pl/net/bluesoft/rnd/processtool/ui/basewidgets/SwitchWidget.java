package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;

@AliasName(name = "SwitchWidgets")
@AperteDoc(humanNameKey = "widget.switch_widget.name", descriptionKey = "widget.switch_widget.description")
@ChildrenAllowed(true)
@WidgetGroup("base-widgets")
public class SwitchWidget extends ProcessHtmlWidget {

	@AutoWiredProperty(required = true)
	@AperteDoc(humanNameKey = "widget.switch_widget.property.selectorKey.name", descriptionKey = "widget.switch_widget.property.selectorKey.description")
	String								selectorKey;

	@AutoWiredProperty(required = true)
	@AperteDoc(humanNameKey = "widget.switch_widget.property.conditions.name", descriptionKey = "widget.switch_widget.property.conditions.description")
	String								conditions;

	public SwitchWidget() {
	}
}
