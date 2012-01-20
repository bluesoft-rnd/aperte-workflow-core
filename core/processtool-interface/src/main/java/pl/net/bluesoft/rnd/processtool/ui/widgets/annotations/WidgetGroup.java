package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

/**
 * Annotation used to group separate widgets into a group
 */
public @interface WidgetGroup {

    /**
     * The name used to aggregate widgets into a widget group
     * @return Widget group name
     */
     String value();

}
