package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import org.vaadin.addon.customfield.CustomField;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.getLocalizedMessage;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 3/1/12
 * Time: 4:44 PM
 */
public abstract class CustomComboBoxField extends CustomField {

    private ComboBox comboBox;

    protected abstract Container getValues();

    protected CustomComboBoxField() {
        comboBox = new ComboBox();
        comboBox.setContainerDataSource(getValues());
        comboBox.setWidth("100%");
        comboBox.setTextInputAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.setNullSelectionAllowed(true);

        HorizontalLayout compositionRoot = new HorizontalLayout();
        setCompositionRoot(compositionRoot);
        compositionRoot.addComponent(comboBox);
        compositionRoot.setExpandRatio(comboBox, 1.0f);
        compositionRoot.setSpacing(true);
        compositionRoot.setWidth("100%");

        Button save = new Button(getLocalizedMessage("processdata.block.combo.editor.save"));
        compositionRoot.addComponent(save);
        save.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                save();
            }
        });


    }

    @Override
    public Property getPropertyDataSource() {
        return comboBox.getPropertyDataSource();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        comboBox.setPropertyDataSource(newDataSource);
    }

    protected void save() {
        comboBox.commit();
    }


}
