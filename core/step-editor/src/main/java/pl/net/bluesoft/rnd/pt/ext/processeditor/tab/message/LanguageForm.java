package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.message;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.Language;

public class LanguageForm extends Form implements FormFieldFactory {

    private BeanItem<Language> item;
    
    public LanguageForm() {
        item = new BeanItem<Language>(new Language());

        setFormFieldFactory(this);
        setItemDataSource(item);
    }

    public Language getLanguage() {
        return item.getBean();
    }
    
    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);

        // Styling
        if (field instanceof AbstractTextField) {
            AbstractTextField textField = (AbstractTextField) field;
            textField.setNullRepresentation("");
        }

        // All fields are required
        field.setRequired(true);

        return field;
    }
}
