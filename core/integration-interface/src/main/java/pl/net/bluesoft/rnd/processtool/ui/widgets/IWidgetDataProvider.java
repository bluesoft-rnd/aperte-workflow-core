package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;

import java.util.Map;

/**
 * Widget data privder for custom attributes
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetDataProvider
{
    Map<String, Object>getData(BpmTask task);


}
