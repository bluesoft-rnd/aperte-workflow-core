package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import org.aperteworkflow.util.dict.ui.DictionaryItemForm;
import org.aperteworkflow.util.dict.ui.DictionaryItemFormFieldFactory;
import pl.net.bluesoft.rnd.processtool.ui.dict.wrappers.DBDictionaryItemWrapper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Set;

public class DBDictionaryItemForm extends DictionaryItemForm {
    public DBDictionaryItemForm(Application application, I18NSource source, BeanItem<DBDictionaryItemWrapper> item) {
        super(application, source, item);
    }

	@Override
	protected DictionaryItemFormFieldFactory createItemFormFieldFactory(Application application, I18NSource source, Set<String> visiblePropertyIds, Set<String> editablePropertyIds, Set<String> requiredPropertyIds) {
		return new DBDictionaryItemFormFieldFactory(application, source, visiblePropertyIds, editablePropertyIds, requiredPropertyIds);
	}
}
