package pl.net.bluesoft.rnd.pt.dict.global;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryItem;
import pl.net.bluesoft.rnd.processtool.dict.IDictionaryFacade;

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

    @Autowired
    private IDictionaryFacade dictionaryFacade;

    @ControllerMethod(action="getAll")
    public GenericResultBean getAll(final OsgiWebRequest invocation)
    {
        GenericResultBean result = new GenericResultBean();


        String dictId = invocation.getRequest().getParameter("dictionaryId");
        if(dictId==null || dictId.length() <=0){
            result.addError("Dictionary","dictionaryId is empty");
            return result;
        }
        Locale locale =   invocation.getRequest().getLocale();
        String langCode = locale.getLanguage();
        logger.log(Level.ALL, "Getting for language" + langCode);

        String filter = invocation.getRequest().getParameter("filter");

        Collection<DictionaryItem> dictionaryItems = dictionaryFacade.getAllDictionaryItems(dictId, locale, filter);

        result.setData(dictionaryItems);

        return result;
    }


}
