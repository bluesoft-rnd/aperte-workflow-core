package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Container;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 3/1/12
 * Time: 4:44 PM
 */
public abstract class CustomComboBoxField extends ComboBox{

    protected abstract Container getValues();

    protected CustomComboBoxField() {
        setContainerDataSource(getValues());

    }
}
