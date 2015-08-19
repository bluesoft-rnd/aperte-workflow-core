package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.util.Map;

/**
 * Widget data privder for custom attributes
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetDataProvider
{
    Map<String, Object>getData(IAttributesProvider provider, Map<String, Object> baseViewData);


}
