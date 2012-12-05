package pl.net.bluesoft.rnd.processtool.ui.dict.fields;

import com.vaadin.Application;
import org.aperteworkflow.util.dict.ui.fields.DictionaryItemExtensionField;
import org.aperteworkflow.util.dict.ui.fields.DictionaryItemValuesField;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemValueWrapper;
import pl.net.bluesoft.rnd.processtool.ui.dict.wrappers.DBDictionaryItemValueWrapper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

public class DBDictionaryItemValuesField extends DictionaryItemValuesField {
    public DBDictionaryItemValuesField(Application application, I18NSource source, String valueType) {
        super(application,  source, valueType);
    }

	@Override
	protected DictionaryItemValueWrapper createItemValueWrapper() {
		return new DBDictionaryItemValueWrapper();
	}

	@Override
	protected DictionaryItemExtensionField createItemExtensionField(Application application, I18NSource source) {
		return new DBDictionaryItemExtensionField(application, source);
	}
}
