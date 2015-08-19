package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;

/**
 * Widget data handler
 *
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetDataHandler
{
    /** Handle widget data change */
    void handleWidgetData(IAttributesConsumer consumer, WidgetData data);
}
