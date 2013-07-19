package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import org.aperteworkflow.scripting.ScriptProcessorRegistry;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.util.Collection;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 3/1/12
 * Time: 4:52 PM
 */
public class ScriptingEnginesComboBox extends CustomComboBoxField {


    @Override
    protected Container getValues() {
        ScriptProcessorRegistry registry =  getRegistry()
                .lookupService(
                ScriptProcessorRegistry.class.getName());
        Collection<String> registeredProcessors = registry.getRegisteredProcessors();

        return new BeanItemContainer<String>(String.class, registeredProcessors);
    }


    @Override
    public Class<?> getType() {
        return String.class;
    }
}
