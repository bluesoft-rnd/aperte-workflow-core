package pl.net.bluesoft.rnd.processtool.ui.basewidgets.dataprovider;

import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maciej on 2014-11-03.
 */
public class LatestCommentsDataProvider implements IWidgetDataProvider
{

    @Override
    public Map<String, Object> getData(IAttributesProvider provider, Map<String, Object> baseViewData)
    {
        Map<String, Object> additionalAttributes = new HashMap<String, Object>();
        if(provider instanceof ProcessInstance) {
//            ProcessInstance processInstance = (ProcessInstance)provider;
//            processInstance.getCommentsOrderedByDate()
//
//
//            additionalAttributes.put(CASE_ATTRIBUTE, sourceCase);


        }
        return additionalAttributes;
    }
}
