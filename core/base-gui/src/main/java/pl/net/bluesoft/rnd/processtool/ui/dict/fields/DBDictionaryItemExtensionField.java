package pl.net.bluesoft.rnd.processtool.ui.dict.fields;

import com.vaadin.Application;
import org.aperteworkflow.util.dict.ui.fields.DictionaryItemExtensionField;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper;
import pl.net.bluesoft.rnd.processtool.ui.dict.wrappers.DBDictionaryItemExtensionWrapper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

public class DBDictionaryItemExtensionField extends DictionaryItemExtensionField {
    public DBDictionaryItemExtensionField(Application application, I18NSource source) {
        super(application, source);
	}

	@Override
	protected DictionaryItemExtensionWrapper createDictionaryItemExtensionWrapper() {
		return new DBDictionaryItemExtensionWrapper();
	}
}
