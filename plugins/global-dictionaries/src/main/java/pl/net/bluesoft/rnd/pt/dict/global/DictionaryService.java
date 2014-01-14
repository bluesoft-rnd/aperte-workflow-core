package pl.net.bluesoft.rnd.pt.dict.global;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.util.lang.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        ArrayList<Pair<String,String>> reArray  =new ArrayList<Pair<String,String>>();
        for(ProcessDictionaryItem pdi : list){
            String desc = pdi.getDescription(locale);
            reArray.add(
                 new Pair<String, String>(
                         pdi.getKey(),
                         pdi.getValueForDate(new Date()).getValue(locale)+ ((desc==null)?"":(" - "+desc))
                 )
            );
        }

        result.setData(reArray);

        return result;
    }

}
