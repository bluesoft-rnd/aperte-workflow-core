package pl.net.bluesoft.rnd.pt.dict.global.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.pt.dict.global.controller.bean.DictionaryDTO;
import pl.net.bluesoft.rnd.pt.dict.global.controller.bean.DictionaryItemDTO;
import pl.net.bluesoft.rnd.pt.dict.global.controller.bean.DictionaryItemValueDTO;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.net.URLDecoder;
import java.util.*;

/**
 * Created by pkuciapski on 2014-05-30.
 */
@OsgiController(name = "dictionaryeditorcontroller")
public class DictionaryEditorController implements IOsgiWebController {
    @Autowired
    ProcessToolRegistry registry;

    protected static final ObjectMapper mapper = new ObjectMapper();

    @ControllerMethod(action = "getDictionaryItems")
    public GenericResultBean getDictionaryItems(final OsgiWebRequest invocation) throws Exception {
        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
        JQueryDataTableColumn sortColumn = dataTable.getFirstSortingColumn();
        String dictId = invocation.getRequest().getParameter("dictId");
        Collection<DictionaryItemDTO> dtos = Collections.emptyList();
        Long count = Long.valueOf(0);

        if (dictId != null) {
            dictId = URLDecoder.decode(dictId, "UTF-8");
            ProcessDictionaryDAO dao = registry.getDataRegistry().getProcessDictionaryDAO(invocation.getProcessToolContext().getHibernateSession());

            Collection<ProcessDBDictionaryItem> items = dao.getDictionaryItems(dictId, sortColumn.getPropertyName(), sortColumn.getSortedAsc(), dataTable.getPageLength(), dataTable.getPageOffset());
            dtos = createItemDTOList(items, invocation.getProcessToolRequestContext().getMessageSource());
            count = dao.getDictionaryItemsCount(dictId);
        }
        DataPagingBean<DictionaryItemDTO> dataPagingBean =
                new DataPagingBean<DictionaryItemDTO>(dtos, count.intValue(), dataTable.getEcho());
        return dataPagingBean;

    }

    @ControllerMethod(action = "getAllDictionaries")
    public GenericResultBean getAllDictionaries(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        ProcessDictionaryDAO dao = registry.getDataRegistry().getProcessDictionaryDAO(invocation.getProcessToolContext().getHibernateSession());

        List<ProcessDBDictionary> dictionary = dao.fetchAllDictionaries();
        List<DictionaryDTO> dtos = new ArrayList(createDTOList(dictionary, invocation.getProcessToolRequestContext().getMessageSource()));

        final UserData user = invocation.getProcessToolRequestContext().getUser();
        List<DictionaryDTO> dictionaries = new ArrayList();
        for (DictionaryDTO dto : dtos) {
            if (user.hasRole("DICT_EDITOR_" + dto.getId().toUpperCase()))
                dictionaries.add(dto);
        }
        Collections.sort(dictionaries, new Comparator<DictionaryDTO>() {
            @Override
            public int compare(DictionaryDTO d1, DictionaryDTO d2) {
                return d1.getName().compareTo(d2.getName());
            }
        });

        result.setData(dictionaries);

        return result;
    }

    private Collection<DictionaryDTO> createDTOList(List<ProcessDBDictionary> dictionaries, I18NSource messageSource) {
        Collection<DictionaryDTO> dtos = new ArrayList<DictionaryDTO>();
        for (ProcessDBDictionary dict : dictionaries) {
            DictionaryDTO dto = DictionaryDTO.createFrom(dict, messageSource);
            dtos.add(dto);
        }
        return dtos;
    }

    private Collection<DictionaryItemDTO> createItemDTOList(Collection<ProcessDBDictionaryItem> items, I18NSource messageSource) {
        Collection<DictionaryItemDTO> dtos = new ArrayList<DictionaryItemDTO>();
        for (ProcessDBDictionaryItem item : items) {
            DictionaryItemDTO dto = DictionaryItemDTO.createFrom(item, messageSource);
            dtos.add(dto);
        }
        return dtos;
    }

    @ControllerMethod(action = "deleteDictionaryItem")
    public GenericResultBean deleteDictionaryItem(final OsgiWebRequest invocation) throws Exception {
        GenericResultBean result = new GenericResultBean();
        ProcessDictionaryDAO dao = registry.getDataRegistry().getProcessDictionaryDAO(invocation.getProcessToolContext().getHibernateSession());
        String dictId = invocation.getRequest().getParameter("dictId");
        String itemJson = invocation.getRequest().getParameter("item");
        try {
            DictionaryItemDTO dto = mapper.readValue(itemJson, DictionaryItemDTO.class);
            ProcessDBDictionary dictionary = getDictionary(dictId, invocation.getProcessToolContext());
            removeItem(dictionary, dto);
            dao.updateDictionary(dictionary);
        } catch (Exception e) {
            result.addError("deleteDictionaryItem", e.getMessage());
        }

        return result;
    }

    private void removeItem(ProcessDBDictionary dictionary, DictionaryItemDTO itemDTO) {
        dictionary.removeItem(itemDTO.getKey());
    }

    @ControllerMethod(action = "getItemValues")
    public GenericResultBean getItemValues(final OsgiWebRequest invocation) throws Exception {
        // JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
        // JQueryDataTableColumn sortColumn = dataTable.getFirstSortingColumn();
        String dictId = invocation.getRequest().getParameter("dictId");
        String itemId = invocation.getRequest().getParameter("itemId");
        Collection<DictionaryItemValueDTO> dtos = Collections.emptyList();
        if ("undefined".equals(itemId))
            itemId = null;
        GenericResultBean result = new GenericResultBean();
        if (dictId != null) {
            // todo get the item from dao
            ProcessDBDictionary dictionary = getDictionary(dictId, invocation.getProcessToolContext());
            if (dictionary != null) {
                ProcessDictionaryDAO dao = registry.getDataRegistry().getProcessDictionaryDAO(invocation.getProcessToolContext().getHibernateSession());
                dictionary = dao.refresh(dictionary);
                ProcessDBDictionaryItem item = getItemById(dictionary.getItems(), itemId);
                if (item != null) {
                    item = dao.refresh(item);
                    dtos = createItemValueDTOList(item.getValues(), invocation.getProcessToolRequestContext().getMessageSource());
                }
            }
        }
        result.setData(dtos);
        //DataPagingBean<DictionaryItemValueDTO> dataPagingBean =
        //        new DataPagingBean<DictionaryItemValueDTO>(dtos, dtos.size(), dataTable.getEcho());

        return result;
    }

    private ProcessDBDictionary getDictionary(String dictId, ProcessToolContext context) throws Exception {
        dictId = URLDecoder.decode(dictId, "UTF-8");
        ProcessDictionaryDAO dao = registry.getDataRegistry().getProcessDictionaryDAO(context.getHibernateSession());
        return dao.fetchDictionary(dictId);
    }

    private Collection<DictionaryItemValueDTO> createItemValueDTOList(Set<ProcessDBDictionaryItemValue> values, I18NSource messageSource) {
        Collection<DictionaryItemValueDTO> dtos = new ArrayList<DictionaryItemValueDTO>();
        for (ProcessDBDictionaryItemValue value : values) {
            DictionaryItemValueDTO dto = DictionaryItemValueDTO.createFrom(value, messageSource);
            dtos.add(dto);
        }
        return dtos;
    }

    private ProcessDBDictionaryItem getItemById(Map<String, ProcessDBDictionaryItem> items, String itemId) {
        if (itemId == null)
            return null;
        for (ProcessDBDictionaryItem item : items.values()) {
            if (item.getId().equals(Long.valueOf(itemId)))
                return item;
        }
        return null;
    }


    @ControllerMethod(action = "saveDictionaryItem")
    public GenericResultBean saveDictionaryItem(final OsgiWebRequest invocation) throws Exception {
        GenericResultBean result = new GenericResultBean();
        String dictId = invocation.getRequest().getParameter("dictId");
        String item = invocation.getRequest().getParameter("item");
        ProcessDictionaryDAO dao = registry.getDataRegistry().getProcessDictionaryDAO(invocation.getProcessToolContext().getHibernateSession());
        I18NSource messageSource = invocation.getProcessToolRequestContext().getMessageSource();
        ProcessDBDictionaryItem itemToValidate = null;
        try {
            DictionaryItemDTO dto = mapper.readValue(item, DictionaryItemDTO.class);
            ProcessDBDictionary dictionary = getDictionary(dictId, invocation.getProcessToolContext());
            dictionary = dao.refresh(dictionary);
            try {
                if (dto.getId() == null) {
                    itemToValidate = dto.toProcessDBDictionaryItem(messageSource.getLocale().getLanguage());
                    dictionary.addItem(itemToValidate);
                } else {
                    ProcessDBDictionaryItem dbItem = getItemById(dictionary.getItems(), String.valueOf(dto.getId()));
                    dbItem = dao.refresh(dbItem);
                    itemToValidate = updateItem(dictionary, dbItem, dto, messageSource);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(messageSource.getMessage("dictionary.editor.validator.itemValues.already.exists", (Object)itemToValidate.getKey()));
            }
            if (itemToValidate != null)
                new DictionaryValidator(messageSource).validate(itemToValidate);
            dao.updateDictionary(dictionary);
        } catch (Exception e) {
            result.addError("saveDictionaryItem", e.getMessage());
            if (dao.getSession().isDirty())
                dao.getSession().clear();
        }
        return result;
    }

    private ProcessDBDictionaryItem updateItem(ProcessDBDictionary dictionary, ProcessDBDictionaryItem item, DictionaryItemDTO dto, I18NSource messageSource) {
        if (item.getId() != null && item.getId().equals(dto.getId())) {
            dictionary.removeItem(item.getKey());
            dto.updateItem(item, messageSource.getLocale().getLanguage());
            dictionary.addItem(item);
            return item;
        }
        return null;
    }
}
