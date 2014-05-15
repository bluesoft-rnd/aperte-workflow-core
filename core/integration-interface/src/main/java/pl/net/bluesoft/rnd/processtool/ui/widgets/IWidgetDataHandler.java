package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * Widget data handler
 *
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetDataHandler
{
    /** Handle widget data change */
    Collection<HandlingResult> handleWidgetData(IAttributesProvider provider, WidgetData data);

}
