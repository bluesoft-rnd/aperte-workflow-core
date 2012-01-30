package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.ProcessDataBlockWidget;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.WidgetsDefinitionElement;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDataPreviewer extends ProcessDataBlockWidget {
    public ProcessDataPreviewer() {
        //fake context initialization
        setContext(new ProcessStateConfiguration(),
                new ProcessStateWidget(), getDumbI18nSource(), null, getDumbApplication(),
                new HashSet<String>(Arrays.asList("EDIT")), true);
    }

    private Application getDumbApplication() {
        return new Application() {
            @Override
            public void init() {
                //nothing
            }
        };
    }

    private I18NSource getDumbI18nSource() {
        return new I18NSource() {

            @Override
            public String getMessage(String key) {
                return key;
            }

            @Override
            public String getMessage(String key, String defaultValue) {
                return key;
            }

            @Override
            public Locale getLocale() {
                return Locale.getDefault();
            }

            @Override
            public void setLocale(Locale locale) {
                //nothing
            }
        };
    }

    public Component render(WidgetsDefinitionElement element) {
        loadData(new ProcessInstance());
        widgetsDefinitionElement = element;
        return render();
    }
}
