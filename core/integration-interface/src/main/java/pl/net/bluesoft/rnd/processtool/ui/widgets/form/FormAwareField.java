package pl.net.bluesoft.rnd.processtool.ui.widgets.form;

import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/27/12
 * Time: 5:54 PM
 */
public interface FormAwareField extends Field{

    void setFormProperties(Map<String,Property> map);
}
