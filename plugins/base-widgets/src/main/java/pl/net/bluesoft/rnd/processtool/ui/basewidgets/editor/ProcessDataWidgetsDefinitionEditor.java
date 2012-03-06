package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.WidgetDefinitionLoader;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.WidgetsDefinitionElement;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.rnd.processtool.ui.widgets.form.FormAwareField;

import java.util.List;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.getLocalizedMessage;
import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.joinValidationErrors;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDataWidgetsDefinitionEditor extends CustomTextAreaFieldWrapper
        implements FormAwareField {

    private WidgetDefinitionLoader definitionLoader = WidgetDefinitionLoader.getInstance();
    private ProcessDataHierarchyEditor hierarchyEditor;

    private ProcessDataPreviewer processDataPreviewer = new ProcessDataPreviewer();
    private VerticalLayout processPreview = new VerticalLayout();
    private Map<String, Property> formProperties;

    public ProcessDataWidgetsDefinitionEditor() {
        hierarchyEditor = new ProcessDataHierarchyEditor(this);

        rawText.setNullRepresentation("");
        rawText.setWidth("100%");
        rawText.setHeight("400px");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidth("100%");
        tabSheet.addTab(hierarchyEditor, getLocalizedMessage("hierarchy-editor"));
        tabSheet.addTab(getRawTextEditorTab(), getLocalizedMessage("source-xml"));
        tabSheet.addTab(processPreview, getLocalizedMessage("preview"));
        tabSheet.setSelectedTab(hierarchyEditor);
        setCompositionRoot(tabSheet);

    }

    private VerticalLayout getRawTextEditorTab() {
        VerticalLayout vl = new VerticalLayout();
        vl.addComponent(rawText);
        Button commitButton = new Button(getLocalizedMessage("commit"));
        vl.addComponent(commitButton);
        commitButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    String value = (String) rawText.getValue();
                    WidgetsDefinitionElement unmarshall = (WidgetsDefinitionElement) definitionLoader.unmarshall(value);
                    hierarchyEditor.processXml(value);
                    List<XmlValidationError> xmlValidationErrors = unmarshall.validate();
                    if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
                        getApplication().getMainWindow().showNotification(getLocalizedMessage("validation-errors"),
                                joinValidationErrors(xmlValidationErrors),
                                Window.Notification.TYPE_ERROR_MESSAGE);
                    } else {
                        getApplication().getMainWindow().showNotification(getLocalizedMessage("element-tree-update-ok"),
                                Window.Notification.TYPE_TRAY_NOTIFICATION);
                    }
                } catch (Throwable t) {
                    getApplication().getMainWindow().showNotification(getLocalizedMessage("validation-errors"),
                            t.getClass().getName() + ", " + t.getMessage(),
                            Window.Notification.TYPE_ERROR_MESSAGE);
                }

            }
        });
        return vl;
    }



    public void updateFromWidgetsDefinitionElement(WidgetsDefinitionElement widgetsDefinitionElement) {
        String marshall = definitionLoader.marshall(widgetsDefinitionElement);
        rawText.setValue(marshall);
        refreshPreview(widgetsDefinitionElement);
    }

    public void processXml(Object newValue) {
        hierarchyEditor.processXml((String) newValue);
    }


    public void refreshPreview(WidgetsDefinitionElement widgetsDefinitionElement) {
        processPreview.removeAllComponents();
        processPreview.setWidth("100%");
        try {
            processPreview.addComponent(processDataPreviewer.render(widgetsDefinitionElement, formProperties));
            getApplication().getMainWindow().showNotification(getLocalizedMessage("preview-success"),
                    Window.Notification.TYPE_TRAY_NOTIFICATION);
        } catch (Throwable t) {
            if (getApplication() != null) {
                getApplication().getMainWindow().showNotification(getLocalizedMessage("preview-failure"),
                        t.getClass().getName() + ", " + t.getMessage(),
                        Window.Notification.TYPE_TRAY_NOTIFICATION);
            }
        }
    }

    private void validateXmlSyntaxAndCorrectness() throws Validator.InvalidValueException {
        try {
            definitionLoader.unmarshall((String) rawText.getValue());
        }
        catch (Throwable t) {
            throw new Validator.InvalidValueException(t.getMessage());
        }

    }


    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        rawText.setValue(newValue);
        processXml(newValue);
    }

//    @Override
//    protected void setValue(Object newValue, boolean repaintIsNotNeeded) throws ReadOnlyException, ConversionException {
//        rawText.setValue(newValue, repaintIsNotNeeded);
//    }

    @Override
    public Property getPropertyDataSource() {
        final Property propertyDataSource = rawText.getPropertyDataSource();
        return new Property() {

            @Override
            public Object getValue() {
                return propertyDataSource.getValue();
            }

            @Override
            public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
                propertyDataSource.setValue(newValue);
                processXml(newValue);

            }

            @Override
            public Class<?> getType() {
                return propertyDataSource.getType();
            }

            @Override
            public boolean isReadOnly() {
                return propertyDataSource.isReadOnly();
            }

            @Override
            public void setReadOnly(boolean newStatus) {
                propertyDataSource.setReadOnly(newStatus);
            }
        };

    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        rawText.setPropertyDataSource(newDataSource);
        processXml(rawText.getValue());
    }



    @Override
    public void validate() throws Validator.InvalidValueException {        
        rawText.validate();
        validateXmlSyntaxAndCorrectness();
    }

    @Override
    public void setFormProperties(Map<String, Property> map) {
        this.formProperties = map;
    }
}
