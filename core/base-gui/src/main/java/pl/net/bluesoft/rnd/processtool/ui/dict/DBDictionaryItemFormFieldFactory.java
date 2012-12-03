package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import org.aperteworkflow.util.dict.ui.DictionaryItemFormFieldFactory;
import org.aperteworkflow.util.dict.ui.fields.DictionaryItemValuesField;
import pl.net.bluesoft.rnd.processtool.ui.dict.fields.DBDictionaryItemValuesField;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Set;

public class DBDictionaryItemFormFieldFactory extends DictionaryItemFormFieldFactory {
    public DBDictionaryItemFormFieldFactory(Application application, I18NSource source, Set<String> visiblePropertyIds,
											Set<String> editablePropertyIds, Set<String> requiredPropertyIds) {
        super(application, source, visiblePropertyIds, editablePropertyIds, requiredPropertyIds);
    }

	@Override
	protected DictionaryItemValuesField createItemValuesField(Application application, I18NSource source, String valueType) {
		return new DBDictionaryItemValuesField(application, source, valueType);
	}
}
