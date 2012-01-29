package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.*;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.addon.customfield.CustomField;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.WidgetDefinitionLoader;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.*;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.joinValidationErrors;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDataWidgetsDefinitionEditor extends CustomTextAreaFieldWrapper {

    private WidgetDefinitionLoader definitionLoader = WidgetDefinitionLoader.getInstance();
    private ProcessDataHierarchyEditor hierarchyEditor;

    private ProcessDataPreviewer processDataPreviewer = new ProcessDataPreviewer();
    private VerticalLayout processPreview = new VerticalLayout();

    public ProcessDataWidgetsDefinitionEditor() {
        hierarchyEditor = new ProcessDataHierarchyEditor(this);

        rawText.setNullRepresentation("");
        rawText.setWidth("100%");
        rawText.setHeight("400px");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidth("100%");
        tabSheet.addTab(hierarchyEditor, "Editor");//TODO I18N
        tabSheet.addTab(getRawTextEditorTab(), "Source");//TODO I18N
        tabSheet.addTab(processPreview, "Preview"); //TODO i18n
        tabSheet.setSelectedTab(hierarchyEditor);
        setCompositionRoot(tabSheet);

    }

    private VerticalLayout getRawTextEditorTab() {
        VerticalLayout vl = new VerticalLayout();
        vl.addComponent(rawText);
        Button commitButton = new Button("Commit");//TODO i18n
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
                        getApplication().getMainWindow().showNotification("Validation failed", //TODO i18n
                                joinValidationErrors(xmlValidationErrors),
                                Window.Notification.TYPE_ERROR_MESSAGE);
                    } else {
                        getApplication().getMainWindow().showNotification("Validation successful", Window.Notification.TYPE_TRAY_NOTIFICATION);//TODO i18n
                    }
                } catch (Throwable t) {
                    getApplication().getMainWindow().showNotification("Validation failed",
                            t.getClass().getName() + ", " + t.getMessage(),
                            Window.Notification.TYPE_ERROR_MESSAGE);//TODO i18n
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
            processPreview.addComponent(processDataPreviewer.render(widgetsDefinitionElement));
        } catch (Throwable t) {
            getApplication().getMainWindow().showNotification("Preview render failed",
                    t.getClass().getName() + ", " + t.getMessage(),
                    Window.Notification.TYPE_TRAY_NOTIFICATION);//TODO i18n
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



}
