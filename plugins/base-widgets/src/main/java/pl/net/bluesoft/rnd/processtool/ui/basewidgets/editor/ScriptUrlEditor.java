package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import org.aperteworkflow.scripting.ScriptProcessor;
import org.aperteworkflow.scripting.ScriptProcessorRegistry;
import org.aperteworkflow.scripting.ScriptValidationException;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ui.widgets.form.FormAwareField;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.getLocalizedMessage;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 3/5/12
 * Time: 1:51 PM
 */
public class ScriptUrlEditor extends CustomField implements FormAwareField {

    private final TextField url;
    private Map<String, Property> formProperties;
    private static final Logger logger = Logger.getLogger(ScriptUrlEditor.class.getName());

    public ScriptUrlEditor() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        setCompositionRoot(hl);
        url = new TextField();
        url.setNullRepresentation("");
        url.setWidth("100%");
        hl.setWidth("100%");
        hl.addComponent(url);
        hl.setExpandRatio(url, 1.0f);

        Button save = new Button(getLocalizedMessage("script.url.editor.save"));
        hl.addComponent(save);
        save.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validateAndSave();
            }
        });
    }

    private void validateAndSave() {
        if (url.getValue() == null || ((String) url.getValue()).trim().isEmpty()) {
            commit();
            return;
        }

        try {
            ScriptProcessorRegistry registry = getRegistry().lookupService(
                    ScriptProcessorRegistry.class.getName());
            Property scriptType = formProperties.get("scriptEngineType");

            if (scriptType == null || scriptType.getValue() == null || ((String) scriptType.getValue()).isEmpty())
                throw new Validator.InvalidValueException("script.undefined.type");
            ScriptProcessor scriptProcessor = registry.getScriptProcessor((String) scriptType.getValue());
            if (scriptProcessor == null)
                throw new Validator.InvalidValueException("script.processor.not.found");
            InputStream is = new URL((String) url.getValue()).openStream();
            scriptProcessor.validate(is);
            url.commit();
            showInfoNotification("validation.script.ok");

        } catch (Validator.InvalidValueException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            showWarningNotification(e.getMessage(), null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            showWarningNotification("validation.script.error", e.getMessage());
        }
    }

    private void showWarningNotification(String code, String message) {
        getApplication().getMainWindow().showNotification(getLocalizedMessage(code) + nvl(message),
                Window.Notification.TYPE_WARNING_MESSAGE);
    }

    private void showInfoNotification(String message) {
        getApplication().getMainWindow().showNotification(getLocalizedMessage(message),
                Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }


    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public void setFormProperties(Map<String, Property> map) {
        this.formProperties = map;
    }

    @Override
    public Property getPropertyDataSource() {
        return url.getPropertyDataSource();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        url.setPropertyDataSource(newDataSource);
    }
}
