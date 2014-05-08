package pl.net.bluesoft.rnd.pt.dict.global;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.pt.dict.global.bean.DictionaryItem;
import pl.net.bluesoft.rnd.pt.dict.global.bean.DictionaryItemExt;
import pl.net.bluesoft.util.lang.Pair;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: MZU
 * Date: 14.01.14
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
@OsgiController(name="dictservice")
public class DictionaryService  implements IOsgiWebController {
    private static Logger logger = Logger.getLogger(DictionaryService.class.getName());

    @ControllerMethod(action="getAll")
    public GenericResultBean getAll(final OsgiWebRequest invocation)
    {
        GenericResultBean result = new GenericResultBean();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if(ctx==null){
            result.addError("Dictionary","Cannot initialize context");
            return result;
        }
        ProcessDictionaryRegistry processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
        if(processDictionaryRegistry==null){
            result.addError("Dictionary","Cannot initialize registry");
            return result;
        }

        String dictId = invocation.getRequest().getParameter("dictionaryId");
        if(dictId==null || dictId.length() <=0){
            result.addError("Dictionary","dictionaryId is empty");
            return result;
        }
        Locale locale =   invocation.getRequest().getLocale();
        String langCode = locale.getLanguage();
        logger.log(Level.ALL, "Getting for language" + langCode);

        ProcessDictionary pd = processDictionaryRegistry.getDictionary(dictId);
        if(pd==null ){
            result.addError("Dictionary","No dictionary found with name "+ dictId);
            return result;
        }

        List<ProcessDictionaryItem> list = pd.sortedItems(langCode);

        String filter = invocation.getRequest().getParameter("filter");
        Collection<DictFilter> filters = parseFilters(filter);

        List<DictionaryItem> reArray  =new ArrayList<DictionaryItem>();

        for(ProcessDictionaryItem pdi : list)
        {
            String desc = pdi.getDescription(locale);
            DictionaryItem dictionaryItem = new DictionaryItem();
            dictionaryItem.setKey(pdi.getKey());
            dictionaryItem.setValue(pdi.getValueForDate(new Date()).getValue(locale));
            dictionaryItem.setDescription(desc);

            for(ProcessDictionaryItemExtension extension: pdi.getValueForCurrentDate().getItemExtensions())
            {
                DictionaryItemExt dictionaryItemExt = new DictionaryItemExt();
                dictionaryItemExt.setKey(extension.getName());
                dictionaryItemExt.setValue(extension.getValue());

                dictionaryItem.getExtensions().add(dictionaryItemExt);
            }

            if(checkForFilters(dictionaryItem, filters))
                reArray.add(dictionaryItem);
        }

        result.setData(reArray);

        return result;
    }

    private boolean checkForFilters(DictionaryItem item, Collection<DictFilter> filters)
    {
        for(DictFilter dictFilter: filters)
            if(!checkForFilter(item, dictFilter))
                return false;

        return true;
    }

    private boolean checkForFilter(DictionaryItem item, DictFilter filter)
    {
        for(DictionaryItemExt ext: item.getExtensions())
            if(ext.getKey().equals(filter.getKey()) && ext.getValue().equals(filter.getValue()))
                return true;

        return false;
    }

    private Collection<DictFilter> parseFilters(String query)
    {
        Collection<DictFilter> filters = new LinkedList<DictFilter>();
        if(query == null  || query.isEmpty())
            return filters;

        String[] parts = query.split("[,;]");
        for (String part : parts) {
            String[] assignment = part.split("[:=]");
            if (assignment.length != 2)
                continue;

            if (assignment[1].startsWith("\"") && assignment[1].endsWith("\""))
                assignment[1] = assignment[1].substring(1, assignment[1].length() - 1);

            String key = assignment[0];
            String value = assignment[1];

            DictFilter dictFilter = new DictFilter();
            dictFilter.setKey(key);
            dictFilter.setValue(value);

            filters.add(dictFilter);
        }

        return filters;
    }

    private class DictFilter
    {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
