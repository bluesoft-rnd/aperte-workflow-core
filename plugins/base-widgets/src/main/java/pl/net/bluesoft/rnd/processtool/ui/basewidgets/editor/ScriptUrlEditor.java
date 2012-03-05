package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.scripting.ScriptProcessor;
import org.aperteworkflow.scripting.ScriptProcessorRegistry;
import org.aperteworkflow.scripting.ScriptValidationException;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ui.widgets.form.FormAwareField;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 3/5/12
 * Time: 1:51 PM
 */
public class ScriptUrlEditor extends CustomField implements FormAwareField{

    private final TextField url;
    private Map<String, Property> formProperties;

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

        Button save = new Button("processdata.block.script.url.editor.save");
        hl.addComponent(save);
        save.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validateAndSave();
            }
        });
    }

    private void validateAndSave() {
        if(url.getValue() == null || ((String) url.getValue()).trim().isEmpty()){
            commit();
            return;
        }
        ScriptProcessorRegistry registry = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().lookupService(
                ScriptProcessorRegistry.class.getName());
        Property scriptType = formProperties.get("scriptEngineType");

        if(scriptType == null || scriptType.getValue() == null || ((String) scriptType.getValue()).isEmpty())
            throw new Validator.InvalidValueException("processdata.block.error.script.undefined.type");
        ScriptProcessor scriptProcessor = registry.getScriptProcessor((String) scriptType.getValue());
        if(scriptProcessor == null)
            throw new Validator.InvalidValueException("processdata.block.error.script.processor.not.found");

        try {
            InputStream is = new URL((String) url.getValue()).openStream();
            scriptProcessor.validate(is);
            url.commit();
        } catch (ScriptValidationException e) {
            throw new Validator.InvalidValueException(e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public void setFormProperties(Map<String, Property> map) {
        this.formProperties = map;
    }
}
