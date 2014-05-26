package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * Widget validation api
 *
 * @author mpawlak@bluesoft.net.pl
 */
public interface IWidgetValidator {
    /**
     * Validate widget
     */
    Collection<String> validate(IAttributesProvider provider, WidgetData widgetData);

}
