package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import org.aperteworkflow.scripting.ScriptProcessor;
import org.aperteworkflow.scripting.ScriptProcessorRegistry;
import org.aperteworkflow.scripting.ScriptValidationException;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ui.widgets.form.FormAwareField;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.getLocalizedMessage;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 3/5/12
 * Time: 9:57 AM
 */
public class ScriptCodeEditor extends CustomField implements FormAwareField{

    private Map<String, Property> formProperties;
    private final TextArea code;

    public ScriptCodeEditor() {

        VerticalLayout compositionRoot = new VerticalLayout();
        setCompositionRoot(compositionRoot);
        code = new TextArea();
        code.setWidth("100%");
        code.setNullRepresentation("");

        compositionRoot.addComponent(code);
        HorizontalLayout hl = new HorizontalLayout();
        Button save = new Button(getLocalizedMessage("script.editor.save"));
        save.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validateAndSave();
            }
        });
        hl.addComponent(save);
        compositionRoot.addComponent(hl);
    }

    private void validateAndSave() throws Validator.InvalidValueException{
        if(code.getValue() == null || ((String) code.getValue()).trim().isEmpty()){
            commit();
            return;
        }
        try{
        ScriptProcessorRegistry registry = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().lookupService(
                ScriptProcessorRegistry.class.getName());
        Property scriptType = formProperties.get("scriptEngineType");
        if(scriptType == null || scriptType.getValue() == null || ((String) scriptType.getValue()).isEmpty())
            throw new Validator.InvalidValueException("script.undefined.type");
        ScriptProcessor scriptProcessor = registry.getScriptProcessor((String) scriptType.getValue());
        if(scriptProcessor == null)
            throw new Validator.InvalidValueException("script.processor.not.found");

        InputStream is = new ByteArrayInputStream(((String) code.getValue()).getBytes());

            scriptProcessor.validate(is);
            code.commit();
            getApplication().getMainWindow().showNotification(getLocalizedMessage("validation.script.ok"),
                    Window.Notification.TYPE_HUMANIZED_MESSAGE);
        }catch (Validator.InvalidValueException e){
            getApplication().getMainWindow().showNotification(getLocalizedMessage(e.getMessage()),
                    Window.Notification.TYPE_WARNING_MESSAGE);
        } catch (ScriptValidationException e) {
            getApplication().getMainWindow().showNotification(getLocalizedMessage("validation.script.parser-exception") + e.getMessage(),
                    Window.Notification.TYPE_WARNING_MESSAGE);
        } catch (Exception e) {
            getApplication().getMainWindow().showNotification(getLocalizedMessage("validation.script.exception") + e.getMessage(),
                    Window.Notification.TYPE_WARNING_MESSAGE);
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

    @Override
    public Property getPropertyDataSource() {
        return code.getPropertyDataSource();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        code.setPropertyDataSource(newDataSource);
    }
}
