package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validatable;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Layout.AlignmentHandler;
import com.vaadin.ui.Layout.SpacingHandler;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import org.aperteworkflow.scripting.ScriptProcessor;
import org.aperteworkflow.scripting.ScriptProcessorRegistry;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.ProcessDataWidgetsDefinitionEditor;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.ScriptCodeEditor;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.ScriptUrlEditor;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.ScriptingEnginesComboBox;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.WidgetDefinitionLoader;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Strings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vaadin.ui.Alignment.*;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

@AliasName(name = "ProcessData")
@AperteDoc(humanNameKey = "widget.process_data_block.name", descriptionKey = "widget.process_data_block.description")
@ChildrenAllowed(false)
@WidgetGroup("base-widgets")
public class ProcessDataBlockWidget extends BaseProcessToolVaadinWidget implements ProcessToolDataWidget, ProcessToolVaadinRenderable {
    private static final Logger logger = Logger.getLogger(ProcessDataBlockWidget.class.getName());
    private static final Resolver resolver = new DefaultResolver();
    private static Boolean allowFileldsListener = true;

    private WidgetDefinitionLoader definitionLoader = WidgetDefinitionLoader.getInstance();

    private ProcessDictionaryRegistry processDictionaryRegistry;

    //during re-rendering widget, this map could be modified while is iterated - it must be thread safe
    private Map<Property, WidgetElement> boundProperties = new ConcurrentHashMap<Property, WidgetElement>();
    private Map<AbstractSelect, WidgetElement> dictContainers = new HashMap<AbstractSelect, WidgetElement>();
    private Map<AbstractSelect, WidgetElement> instanceDictContainers = new HashMap<AbstractSelect, WidgetElement>();
    private Map<String, ProcessInstanceAttribute> processAttributes = new HashMap<String, ProcessInstanceAttribute>();
    protected WidgetsDefinitionElement widgetsDefinitionElement;
    private ProcessInstance processInstance;
    private Map<WidgetElement, Property> widgetDataSources = new HashMap<WidgetElement, Property>();

    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = ScriptingEnginesComboBox.class)
    @AperteDoc(
            humanNameKey = "widget.process_data_block.property.scriptEngineType.name",
            descriptionKey = "widget.process_data_block.property.scriptEngineType.description"
    )
    private String scriptEngineType;

    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = ScriptUrlEditor.class)
    @AperteDoc(
            humanNameKey = "widget.process_data_block.property.scriptExternalUrl.name",
            descriptionKey = "widget.process_data_block.property.scriptExternalUrl.description"
    )
    private String scriptExternalUrl;

    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = ScriptCodeEditor.class)
    @AperteDoc(
            humanNameKey = "widget.process_data_block.property.scriptSourceCode.name",
            descriptionKey = "widget.process_data_block.property.scriptSourceCode.description"
    )
    private String scriptSourceCode;

    @AutoWiredProperty(required = true)
    @AutoWiredPropertyConfigurator(fieldClass = ProcessDataWidgetsDefinitionEditor.class)
    @AperteDoc(
            humanNameKey = "widget.process_data_block.property.widgetsDefinition.name",
            descriptionKey = "widget.process_data_block.property.widgetsDefinition.description"
    )
    private String widgetsDefinition;
    protected ComponentContainer mainPanel = null;

    public void setDefinitionLoader(WidgetDefinitionLoader definitionLoader) {
        this.definitionLoader = definitionLoader;
    }

    public void setProcessDictionaryRegistry(ProcessDictionaryRegistry processDictionaryRegistry) {
        this.processDictionaryRegistry = processDictionaryRegistry;
    }

    public String getScriptEngineType() {
        return scriptEngineType;
    }

    public void setScriptEngineType(String scriptEngineType) {
        this.scriptEngineType = scriptEngineType;
    }

    public String getScriptExternalUrl() {
        return scriptExternalUrl;
    }

    public void setScriptExternalUrl(String scriptExternalUrl) {
        this.scriptExternalUrl = scriptExternalUrl;
    }

    public String getScriptSourceCode() {
        return scriptSourceCode;
    }

    public void setScriptSourceCode(String scriptSourceCode) {
        this.scriptSourceCode = scriptSourceCode;
    }

    @Override
    public void setContext(ProcessStateConfiguration state, ProcessStateWidget configuration, I18NSource i18NSource,
                           ProcessToolBpmSession bpmSession, Application application, Set<String> permissions, boolean isOwner) {
        super.setContext(state, configuration, i18NSource, bpmSession,
                application, permissions, isOwner);
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
    }

    private abstract class ComponentEvaluator<T> {
        protected T currentComponent;
        protected WidgetElement currentElement;

        protected ComponentEvaluator(Map<T, WidgetElement> input) {
            try {
                if (input != null) {
                
                    for (Entry<T, WidgetElement> entry : input.entrySet()) {
                        evaluate(currentComponent = entry.getKey(), currentElement = entry.getValue());
                    }
                	
                }
            } catch (Exception e) {
                handleException(getMessage("processdata.block.error.eval.other")
                        .replaceFirst("%s", nvl(currentComponent.toString(), "NIL"))
                        .replaceFirst("%s", nvl(currentElement.getBind(), "NIL")), e);
            }
        }

        public abstract void evaluate(T component, WidgetElement element) throws Exception;
    }

    @Override
    public Collection<String> validateData(final BpmTask task, boolean skipRequired) {
        final List<String> errors = new ArrayList<String>();
        new ComponentEvaluator<Property>(boundProperties) {
            @Override
            public void evaluate(Property component, WidgetElement element) throws Exception {
                if (component instanceof Validatable) {
                    Validatable validatable = (Validatable) component;
                    try {
                        validatable.validate();
                    } catch (InvalidValueException e) {
                        errors.add(e.getMessage());
                    }
                }
                if (!component.isReadOnly()) {
                    try {
                        fetchOrCreateAttribute(element);
                    } catch (Exception e) {
                        errors.add(getMessage("processdata.block.error.eval.other").replaceFirst("%s",
                                component.toString()).replaceFirst("%s", element.getBind()) +
                                "<br/>" + e.getMessage());
                    }
                }
            }
        };
        return errors;
    }

    private ProcessInstanceAttribute fetchOrCreateAttribute(WidgetElement element) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        int index = element.getBind().indexOf('.');
        String attributeName = index == -1 ? element.getBind() : element.getBind().substring(0, index);
        ProcessInstanceAttribute attribute = processAttributes.get(attributeName);
        if (attribute == null && (hasText(element.getInheritedAttributeClass()) || index == -1)) {
            attribute = hasText(element.getInheritedAttributeClass())
                    ? (ProcessInstanceAttribute) getClass().getClassLoader().loadClass(element.getInheritedAttributeClass()).newInstance()
                    : new ProcessInstanceSimpleAttribute();
            attribute.setProcessInstance(processInstance);
            attribute.setKey(attributeName);

            processAttributes.put(attributeName, attribute);
        }
        if (index != -1) {
            String propertyName = element.getBind().substring(index + 1);
            Object bean = attribute;
            while (resolver.hasNested(propertyName)) {
                String next = resolver.next(propertyName);
                Object nestedBean = resolver.isMapped(next) ? PropertyUtils.getMappedProperty(bean, next)
                        : resolver.isIndexed(next) ? PropertyUtils.getIndexedProperty(bean, next)
                        : PropertyUtils.getSimpleProperty(bean, next);
                if (nestedBean == null) {
                    Class clazz = PropertyUtils.getPropertyType(bean, next);
                    PropertyUtils.setProperty(bean, next, nestedBean = clazz.newInstance());
                }
                bean = nestedBean;
                propertyName = resolver.remove(propertyName);
            }
        }
        return attribute;
    }

    @Override
    public void saveData(final BpmTask task) {
        ProcessInstance pi = task.getProcessInstance();
        processAttributes.clear();
        for (ProcessInstanceAttribute attribute : pi.getProcessAttributes()) {
            processAttributes.put(attribute.getKey(), attribute);
        }

        new ComponentEvaluator<Property>(boundProperties) {
            @Override
            public void evaluate(Property component, WidgetElement element) throws Exception {
                if (!component.isReadOnly()) {
                    ProcessInstanceAttribute attribute = fetchOrCreateAttribute(element);
                    if (attribute instanceof ProcessInstanceSimpleAttribute) {
                        if (element instanceof DateWidgetElement) {
                            String dateString = null;
                            if (component.getValue() != null)
                                dateString = new SimpleDateFormat(((DateWidgetElement) element).getFormat()).format(component.getValue());
                            ((ProcessInstanceSimpleAttribute) attribute).setValue(dateString);
                        } else if (component.getValue() != null) {
                            ((ProcessInstanceSimpleAttribute) attribute).setValue(component.getValue().toString());
                        }
                    } else {
                        if (component instanceof FileUploadComponent) {
                            ProcessInstanceAttachmentAttribute attachment = (ProcessInstanceAttachmentAttribute) component.getValue();
                            attachment.setProcessState(task.getTaskName());
                            attachment.setProcessInstance(task.getProcessInstance());
                            attachment.setKey(attribute.getKey());
                        }
                        PropertyUtils.setProperty(processAttributes, element.getBind(), component.getValue());
                    }
                }
            }
        };
        pi.setProcessAttributes(new HashSet<ProcessInstanceAttribute>(processAttributes.values()));
    }

    @Override
    public void loadData(final BpmTask task) {
        boundProperties.clear();
        dictContainers.clear();
        if (Strings.hasText(widgetsDefinition)) {
            widgetsDefinitionElement = (WidgetsDefinitionElement) definitionLoader.unmarshall(widgetsDefinition);
        }
        this.processInstance = task.getProcessInstance();
        processAttributes.clear();
        for (ProcessInstanceAttribute attribute : processInstance.getProcessAttributes()) {
            processAttributes.put(attribute.getKey(), attribute);
        }
    }

    private void loadBindings() {
        new ComponentEvaluator<Property>(boundProperties) {
            @Override
            public void evaluate(Property component, WidgetElement element) throws Exception {
            	
            	allowFileldsListener=false;  	// not so great but works, needs further works.
                Object value = null;
                try {
                    value = PropertyUtils.getProperty(processAttributes, element.getBind());
                } catch (NestedNullException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                value = value instanceof ProcessInstanceSimpleAttribute ?
                        ((ProcessInstanceSimpleAttribute) value).getValue() : value;
                if (value != null) {
                    boolean readonly = component.isReadOnly();
                    if (readonly) {
                        component.setReadOnly(false);
                    }
                    if (Date.class.isAssignableFrom(component.getType())) {
						DateWidgetElement dwe = Lang2.assumeType(element, DateWidgetElement.class);
						Date v = new SimpleDateFormat(dwe.getFormat()).parse(String.valueOf(value));
                        component.setValue(v);
                    } else if (String.class.isAssignableFrom(component.getType())) {
                        component.setValue(nvl(value, ""));
                    } else if (component instanceof Container &&
                            component.getType().isAssignableFrom(value.getClass())) {
                        if (((Container) component).containsId(value)) {
                            component.setValue(value);
                        }
                    }
                    if (readonly) {
                        component.setReadOnly(true);
                    }
                }
                allowFileldsListener=true;
            }
        };
    }

    private void loadDictionaries() {
        new ComponentEvaluator<AbstractSelect>(dictContainers) {
            @Override
            public void evaluate(AbstractSelect component, WidgetElement element) throws Exception {
                ProcessDictionary dict = nvl(element.getGlobal(), false) ?
                        processDictionaryRegistry.getSpecificOrDefaultGlobalDictionary(element.getProvider(),
                                element.getDict(), i18NSource.getLocale().toString()) :
                        processDictionaryRegistry.getSpecificOrDefaultProcessDictionary(
                                processInstance.getDefinition(), element.getProvider(),
                                element.getDict(), i18NSource.getLocale().toString());

                if (dict != null) {
                    Date validForDate = getValidForDate(element);
                    int i = 0;
                    for (Object o : dict.items()) {
                        ProcessDictionaryItem item = (ProcessDictionaryItem) o;
                        component.addItem(item.getKey());
                        ProcessDictionaryItemValue val = item.getValueForDate(validForDate);
                        component.setItemCaption(item.getKey(), getMessage(
                                (String) (val != null ? val.getValue() : item.getKey())));
                        if (element instanceof AbstractSelectWidgetElement) {
                            AbstractSelectWidgetElement select = (AbstractSelectWidgetElement) element;
                            if (select.getDefaultSelect() != null && i == select.getDefaultSelect()) {
                                component.setValue(item.getKey());
                            }
                        }
                        ++i;
                    }
                }
            }
        };
    }


    private void loadProcessInstanceDictionaries() {
        new ComponentEvaluator<AbstractSelect>(instanceDictContainers) {
            @Override
            public void evaluate(AbstractSelect component, WidgetElement element) throws Exception {
                AbstractSelectWidgetElement aswe = Lang2.assumeType(element, AbstractSelectWidgetElement.class);
                String dictAttribute = aswe.getDictionaryAttribute();
                ProcessInstanceDictionaryAttribute dict = (ProcessInstanceDictionaryAttribute) processInstance.findAttributeByKey(dictAttribute);
                if (dict != null) {
                    int i = 0;
                    Boolean prevReadOnly = component.isReadOnly();
                    component.setReadOnly(false);
                    for (Object o : dict.getItems()) {
                        ProcessInstanceDictionaryItem itemProcess = (ProcessInstanceDictionaryItem) o;
                        component.addItem(itemProcess.getKey());
                        component.setItemCaption(itemProcess.getKey(), itemProcess.getValue());
                        if (element instanceof AbstractSelectWidgetElement) {
                            AbstractSelectWidgetElement select = (AbstractSelectWidgetElement) element;
                            if (select.getDefaultSelect() != null && i == select.getDefaultSelect()) {
                                component.setValue(itemProcess.getKey());
                            }
                        }
                        ++i;
                    }
                    component.setReadOnly(prevReadOnly);
                }
            }
        };
    }

    private Date getValidForDate(WidgetElement element) throws Exception {
        Date validForDate = processInstance.getCreateDate();
        if (Strings.hasText(element.getValidFor())) {
            String[] splits = element.getValidFor().split(":");
            if (splits.length != 2) {
                throw new IllegalArgumentException("Error while loading values for "
                        + (nvl(element.getGlobal(), false) ? "global" : "process") + " dictionary["
                        + element.getDict() + "], provider[" + element.getProvider() + "]: "
                        + "Illegal argument: " + element.getValidFor()
                        + " for element: " + element.getClass().getName());
            }
            String controlChar = splits[0];
            String validFor = splits[1];
            DateFormat dateFormat = VaadinUtility.simpleDateFormat();
            if ("d".equalsIgnoreCase(controlChar) && !"current".equalsIgnoreCase(validFor)) {
                validForDate = dateFormat.parse(validFor);
            } else if ("a".equalsIgnoreCase(controlChar)) {
                Object attributeValue = PropertyUtils.getProperty(processAttributes, validFor);
                if (attributeValue instanceof String) {
                    validForDate = dateFormat.parse(validFor);
                } else if (attributeValue instanceof Date) {
                    validForDate = (Date) attributeValue;
                } else {
                    throw new IllegalArgumentException("Unable to date from property: " + validFor
                            + "for element: " + element.getClass().getName());
                }
            }
        }
        return validForDate;
    }

    @Override
    public void addChild(ProcessToolWidget child) {
        throw new IllegalArgumentException("children are not supported in this widget");
    }

    @Override
    public Component render() {
        return widgetsDefinitionElement != null ? renderInternal() : new Label(getMessage("processdata.block.nothing.to.render"));
    }

    /**
     * In subclasses without access to proper application object this method should be overridden
     * TODO: rethink this approach
     */
    protected void handleException(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
        VaadinUtility.validationNotification(getApplication(), i18NSource, message + "<br/>" + e.getMessage());
    }

    private Component renderInternal() {
        try {
            mainPanel = !hasText(widgetsDefinitionElement.getClassName()) ? new VerticalLayout()
                    : (ComponentContainer) getClass().getClassLoader().loadClass(widgetsDefinitionElement.getClassName()).newInstance();
        } catch (Exception e) {
            handleException(getMessage("processdata.block.error.load.class").replaceFirst("%s", widgetsDefinitionElement.getClassName()), e);
        }

        setupWidget(widgetsDefinitionElement, mainPanel);

        for (WidgetElement we : widgetsDefinitionElement.getWidgets()) {
            processWidgetElement(widgetsDefinitionElement, we, mainPanel);
        }
        loadDictionaries();
        loadProcessInstanceDictionaries();
        loadBindings();

        if (executeScript()) {

            mainPanel.removeAllComponents();
            try {
                for (WidgetElement we : widgetsDefinitionElement.getWidgets()) {
                    processWidgetElement(widgetsDefinitionElement, we, mainPanel);
                }
            } catch (Exception e) {
                handleException(getMessage("widget.process_data_block.editor.validation.script.error"), e);
                mainPanel = null;
            }
        }

        return mainPanel;
    }

    private boolean executeScript() {
    	
        boolean executed = false;
        try {
            if (!hasText(getScriptEngineType()) || !hasText(getScriptSourceCode()) && !hasText(getScriptExternalUrl()))
                return executed;

            Map<String, Object> fields = getFieldsMap(widgetsDefinitionElement.getWidgets());
            fields.put("process", processInstance);

            ScriptProcessorRegistry registry = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().lookupService(
                    ScriptProcessorRegistry.class.getName());
//          TODO: some smart cacheing
            InputStream is = loadScriptCode();
            ScriptProcessor scriptProcessor = registry.getScriptProcessor(getScriptEngineType());
            if (scriptProcessor == null) {
                logger.severe("Script processor not found: " + getScriptEngineType() + ", skipping script execution. ");
                return executed;
            }
            scriptProcessor.process(fields, is);
            executed = true;
            boundProperties.clear();
            dictContainers.clear();

        } catch (Exception e) {
            handleException(getMessage("widget.process_data_block.editor.validation.script.error"), e);
            executed = false;
        }
        return executed;
    }


    private Map<String, Object> getFieldsMap(List<WidgetElement> widgets) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (WidgetElement we : widgets) {
            Property property = widgetDataSources.get(we);
            if (property != null && property.getValue() != null)
                we.setValue(property.getValue());
            if (we.getId() != null) {
                map.put(we.getId(), we);
            }
            if (we instanceof HasWidgetsElement)
                map.putAll(getFieldsMap(((HasWidgetsElement) we).getWidgets()));
        }
        return map;
    }

    private InputStream loadScriptCode() {


        if (hasText(getScriptExternalUrl()))
            try {
                return new URL(getScriptExternalUrl()).openStream();
            } catch (IOException e) {
                handleException(getMessage("validation.script.url-io-exception"), e);
            }
        if (hasText(getScriptSourceCode()))
            return new ByteArrayInputStream(getScriptSourceCode().getBytes());
        return null;
    }

    private AbstractComponent processWidgetElement(WidgetElement parent, WidgetElement element, ComponentContainer container) {
        AbstractComponent component = null;
        if (element instanceof LabelWidgetElement) {
            component = createLabelField((LabelWidgetElement) element);
        } else if (element instanceof InputWidgetElement) {
            component = createInputField((InputWidgetElement) element);
        } else if (element instanceof VerticalLayoutWidgetElement) {
            component = createVerticalLayout((VerticalLayoutWidgetElement) element);
        } else if (element instanceof HorizontalLayoutWidgetElement) {
            component = createHorizontalLayout((HorizontalLayoutWidgetElement) element);
        } else if (element instanceof AlignElement) {
            applyAlignment((AlignElement) element, container);
        } else if (element instanceof DateWidgetElement) {
            component = createDateField((DateWidgetElement) element);
        } else if (element instanceof FormWidgetElement) {
            component = createFormField((FormWidgetElement) element);
        } else if (element instanceof GridWidgetElement) {
            component = createGrid((GridWidgetElement) element);
        } else if (element instanceof LinkWidgetElement) {
            component = createLink((LinkWidgetElement) element);
        } else if (element instanceof ComboboxSelectElementWidget) {
            component = createComboSelectField((ComboboxSelectElementWidget) element);
        } else if (element instanceof RadioButtonSelectElementWidget) {
            component = createRadioButtonSelectField((RadioButtonSelectElementWidget) element);
        } else if (element instanceof TextAreaWidgetElement) {
            component = createTextAreaField((TextAreaWidgetElement) element);
        } else if (element instanceof CheckBoxWidgetElement) {
            component = createCheckBoxField((CheckBoxWidgetElement) element);
        } else if (element instanceof UploadWidgetElement) {
            component = createFileUploadField((UploadWidgetElement) element);
        }

        if (component != null) {
            component.setImmediate(true);
            component.setEnabled(hasPermission("EDIT"));
            if (component.isReadOnly() || !component.isEnabled()) {
                component.setHeight(null);
            }
            setupWidget(element, component);
            container.addComponent(component);
        }
        element.setParent(parent);

        if (component instanceof Field) {
            Property property = (Property) component;
            widgetDataSources.put(element, property);

            if (element.getDynamicValidation() == Boolean.TRUE)
                ((Field) component).addListener(new ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                    	if(allowFileldsListener){ // not so great, but works, needs further works.
                    	boolean executedScript = executeScript();
                        if (!executedScript){
                            return;
                        }

                        mainPanel.removeAllComponents();
                        for (WidgetElement we : widgetsDefinitionElement.getWidgets()) {
                            processWidgetElement(widgetsDefinitionElement, we, mainPanel);
                        } 
					}
                    }
                });
        }

        performAdditionalProcessing(element, component);

        return component;
    }

    /**
     * Override in subclasses for additional element/component processing
     *
     * @param element
     * @param component
     */
    protected void performAdditionalProcessing(WidgetElement element, AbstractComponent component) {
//      NOOP
    }

    private AbstractComponent createRadioButtonSelectField(RadioButtonSelectElementWidget element) {
        OptionGroup radioSelect = new OptionGroup();
        radioSelect.setNullSelectionAllowed(false);
        radioSelect.setCaption(element.getCaption());
        radioSelect.setHtmlContentAllowed(true);
//        if(element.getValues().isEmpty())
        for (int i = 0; i < element.getValues().size(); i++) {
            ItemElement item = element.getValues().get(i);
            radioSelect.addItem(item.getKey());
            radioSelect.setItemCaption(item.getKey(), item.getValue());


            if (radioSelect.getValue() == null && element.getDefaultSelect() != null && i == element.getDefaultSelect()) {
                radioSelect.setValue(item.getKey());
            }

        }
        if (element.getValue() != null) {
            radioSelect.setValue(element.getValue());
        }
        if (nvl(element.getRequired(), false)) {
            radioSelect.setRequired(true);
            if (hasText(element.getCaption())) {
                radioSelect.setRequiredError(getMessage("processdata.block.field-required-error") + " " + element.getCaption());
            } else {
                radioSelect.setRequiredError(getMessage("processdata.block.field-required-error"));
            }
        }
        return radioSelect;
    }

    private AbstractComponent createFileUploadField(UploadWidgetElement element) {
        FileUploadComponent upload = new FileUploadComponent(i18NSource);
        return upload;
    }

    private CheckBox createCheckBoxField(CheckBoxWidgetElement we) {
        CheckBox cb = new CheckBox();
        if (we.getDefaultSelect() != null && we.getDefaultSelect()) {
            cb.setValue(we.getDefaultSelect());
        }
        if (we.getValue() != null)
            cb.setValue(we.getValue());
        return cb;
    }

    private Select createComboSelectField(ComboboxSelectElementWidget swe) {
        Select select = new Select();
        if (!swe.getValues().isEmpty()) {
            for (int i = 0; i < swe.getValues().size(); ++i) {
                ItemElement item = swe.getValues().get(i);
                select.addItem(item.getKey());
                select.setItemCaption(item.getKey(), item.getValue());
                if (swe.getDefaultSelect() != null && i == swe.getDefaultSelect()) {
                    select.setValue(item.getKey());
                }
            }
            if (swe.getValue() != null)
                select.setValue(swe.getValue());
        }

        if (nvl(swe.getRequired(), false)) {
            select.setRequired(true);
            if (hasText(swe.getCaption())) {
                select.setRequiredError(getMessage("processdata.block.field-required-error") + " " + swe.getCaption());
            } else {
                select.setRequiredError(getMessage("processdata.block.field-required-error"));
            }
        }
        return select;
    }

//    private void processScriptElement(Select select, ScriptElement script) {
//        throw new RuntimeException("Not implemented yet!");
//    }

    private Form createFormField(FormWidgetElement fwe) {
        FormLayout layout = new FormLayout();
        processOrderedLayout(fwe, layout);
        Form form = new Form(layout);
        return form;
    }

    private AbstractComponent createTextAreaField(TextAreaWidgetElement taw) {
        AbstractComponent component;
        if (taw.getRich() != null && taw.getRich()) {
            RichTextArea rta = new RichTextArea();
            if (taw.getVisibleLines() != null) {
                rta.setHeight(taw.getVisibleLines() * 2 + 4, Sizeable.UNITS_EM);
            }
            if (taw.getLimit() != null) {
                rta.addValidator(new StringLengthValidator(getMessage("processdata.block.error.text.exceeded").replaceFirst("%s",
                        "" + taw.getLimit()), 0, taw.getLimit(), true));
            }
            if (nvl(taw.getRequired(), false)) {
                rta.setRequired(true);
                if (hasText(taw.getCaption())) {
                    rta.setRequiredError(getMessage("processdata.block.field-required-error") + " " + taw.getCaption());
                } else {
                    rta.setRequiredError(getMessage("processdata.block.field-required-error"));
                }
            }
            if (!hasPermission("EDIT")) {
                rta.setReadOnly(true);
                rta.setHeight(null);
            }
            if (taw.getValue() != null) {
                rta.setValue(taw.getValue());
			}

            component = rta;
        } else {
            TextArea ta = new TextArea();
            if (taw.getVisibleLines() != null) {
                ta.setRows(taw.getVisibleLines());
            }
            if (taw.getLimit() != null) {
                ta.setMaxLength(taw.getLimit());
            }

            if (nvl(taw.getRequired(), false)) {
                ta.setRequired(true);
                if (hasText(taw.getCaption())) {
                    ta.setRequiredError(getMessage("processdata.block.field-required-error") + " " + taw.getCaption());
                } else {
                    ta.setRequiredError(getMessage("processdata.block.field-required-error"));
                }
            }
            if (taw.getValue() != null) {
                ta.setValue(taw.getValue());
			}

            component = ta;
        }

        return component;
    }

    private Link createLink(LinkWidgetElement we) {
        Link link = new Link();
        link.setTargetName("_blank");
        String url = we.getUrl();
		if(url.matches("#\\{.*\\}")){
        	String urlKey = url.replaceAll("#\\{(.*)\\}", "$1");
        	if(processAttributes.containsKey(urlKey))
        		url = ((ProcessInstanceSimpleAttribute)processAttributes.get(urlKey)).getValue();
        }
        link.setResource(new ExternalResource(url));
        return link;
    }

    private GridLayout createGrid(GridWidgetElement gwe) {
        GridLayout grid = new GridLayout();
        if (gwe.getCols() != null) {
            grid.setColumns(gwe.getCols());
        }
        if (gwe.getRows() != null) {
            grid.setRows(gwe.getRows());
        }
        processOrderedLayout(gwe, grid);
        return grid;
    }

    private VerticalLayout createVerticalLayout(VerticalLayoutWidgetElement vlw) {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        processOrderedLayout(vlw, vl);
        return vl;
    }

    private HorizontalLayout createHorizontalLayout(HorizontalLayoutWidgetElement hlw) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        processOrderedLayout(hlw, hl);
        return hl;
    }

    private void processOrderedLayout(HasWidgetsElement hwe, AbstractLayout al) {
        for (WidgetElement we : hwe.getWidgets()) {
            Component widget = processWidgetElement(hwe, we, al);
            if (widget != null && al instanceof AbstractOrderedLayout) {
                AbstractOrderedLayout aol = (AbstractOrderedLayout) al;
                aol.setExpandRatio(widget, 1f);
            }
        }
    }

    private void applyAlignment(AlignElement ae, ComponentContainer container) {
        if (container instanceof AlignmentHandler) {
            AlignmentHandler ah = (AlignmentHandler) container;
            for (WidgetElement awe : ae.getWidgets()) {
                Component widget = processWidgetElement(ae, awe, container);
                if (widget != null) {
                    if (XmlConstants.ALIGN_POS_CENTER_TOP.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, TOP_CENTER);
                    } else if (XmlConstants.ALIGN_POS_LEFT_TOP.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, TOP_LEFT);
                    } else if (XmlConstants.ALIGN_POS_RIGHT_TOP.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, TOP_RIGHT);
                    } else if (XmlConstants.ALIGN_POS_CENTER_MIDDLE.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, MIDDLE_CENTER);
                    } else if (XmlConstants.ALIGN_POS_LEFT_MIDDLE.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, MIDDLE_LEFT);
                    } else if (XmlConstants.ALIGN_POS_RIGHT_MIDDLE.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, MIDDLE_RIGHT);
                    } else if (XmlConstants.ALIGN_POS_CENTER_BOTTOM.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, BOTTOM_CENTER);
                    } else if (XmlConstants.ALIGN_POS_LEFT_BOTTOM.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, BOTTOM_LEFT);
                    } else if (XmlConstants.ALIGN_POS_RIGHT_BOTTOM.equals(ae.getPos())) {
                        ah.setComponentAlignment(widget, BOTTOM_RIGHT);
                    }
                }
            }
        }
    }

    private AbstractTextField createInputField(InputWidgetElement iwe) {
        AbstractTextField field = iwe.getSecret() != null && iwe.getSecret() ? new PasswordField() : new TextField();
        if (iwe.getMaxLength() != null) {
            field.setMaxLength(iwe.getMaxLength());
        }
        if (hasText(iwe.getRegexp()) && hasText(iwe.getRegexp())) {
            field.addValidator(new RegexpValidator(WidgetDefinitionLoader.replaceXmlEscapeCharacters(iwe.getRegexp()), iwe.getErrorKey() != null ?
                    iwe.getErrorKey() : getMessage("processdata.block.error.regexp").replaceFirst("%s", iwe.getRegexp())));
        }
        if (nvl(iwe.getRequired(), false)) {
            field.setRequired(true);
            if (hasText(iwe.getCaption())) {
                String caption = iwe.getCaption();
                if (caption.endsWith(":"))
                    caption = caption.substring(0, caption.length() - 1);
                field.setRequiredError(getMessage("processdata.block.field-required-error") + " " + caption);
            } else {
                field.setRequiredError(getMessage("processdata.block.field-required-error"));
            }
        }
        if (hasText(iwe.getBaseText())) {
            field.setValue(getMessage(iwe.getBaseText()));
        }
        if (hasText(iwe.getPrompt())) {
            field.setInputPrompt(getMessage(iwe.getPrompt()));
        }
        if (iwe.getValue() != null)
            field.setValue(iwe.getValue());
        return field;
    }

    private Label createLabelField(LabelWidgetElement lwe) {
        Label label = new Label();
        if (lwe.getMode() != null) {
            label.setContentMode(lwe.getMode());
        }
        if (hasText(lwe.getText())) {
            label.setValue(WidgetDefinitionLoader.removeCDATATag(WidgetDefinitionLoader.replaceXmlEscapeCharacters(lwe.getText())));
        }
        if (lwe.getValue() != null)
            label.setValue(lwe.getValue());
        return label;
    }

    private DateField createDateField(final DateWidgetElement dwe) {
        final SimpleDateFormat sdf;
       try {
           sdf = new SimpleDateFormat(dwe.getFormat());
       } catch (Exception e) {
           handleException(getMessage("processdata.block.error.unparsable.format").replaceFirst("%s", dwe.getFormat()), e);
           return null;
       }

       final PopupDateField field = new PopupDateField();
       final boolean fieldMinResolution = dwe.getShowMinutes() != null && dwe.getShowMinutes();

       field.setDateFormat(dwe.getFormat());
       field.setResolution(fieldMinResolution ? DateField.RESOLUTION_MIN : DateField.RESOLUTION_DAY);
       if (hasText(dwe.getNotAfter())) {
           try {
               boolean usesCurrent = XmlConstants.DATE_CURRENT.equalsIgnoreCase(dwe.getNotAfter());
               final Date notAfter = (usesCurrent) ? sdf.parse(sdf.format(new Date())) : sdf.parse(dwe.getNotAfter());
               field.addValidator(new AbstractValidator(getMessage("processdata.block.error.date.notafter").replaceFirst("%s", dwe.getNotAfter())) {
					@Override
					public boolean isValid(Object value) {
						Date formatedDateFromCalendarInput = formatTimeFromCalendarInput((Date)value,sdf);
						return value == null ||  isBeforeCurrentDate(formatedDateFromCalendarInput);
					}
					private boolean isBeforeCurrentDate(Date formatedDateFromCalendarInput){

						return	isRightSideOpen()?  isBeforeCurrentDateRightSideOpen(formatedDateFromCalendarInput):isBeforeCurrentDateRightSideClosed(formatedDateFromCalendarInput);

						}

						private boolean isRightSideOpen(){
						if(dwe.getDiscludeNotAfter()==null){
							return false;
						}
						else{
							return dwe.getDiscludeNotAfter();
						}
					}


						private boolean isBeforeCurrentDateRightSideClosed(Date formatedDateFromCalendarInput){

							return notAfter.equals(formatedDateFromCalendarInput)? true : isNotAfter(formatedDateFromCalendarInput);
						}

						private boolean isBeforeCurrentDateRightSideOpen(Date formatedDateFromCalendarInput){
							
							return   notAfter.equals(formatedDateFromCalendarInput)? false : isNotAfter(formatedDateFromCalendarInput);
						}
						
						private boolean isNotAfter(Date formatedDateFromCalendarInput){
							return   !notAfter.before(formatedDateFromCalendarInput);
							
						}
				});
               //why notify and interrupt?
               //we already have a perfect validation mechanisms
               //so let's use classic validators
//               field.addListener(new ValueChangeListener() {
//                   @Override
//                   public void valueChange(ValueChangeEvent event) {
//                       Object value = event.getProperty().getValue();
//                       if (value != null && value instanceof Date) {
//                           if (notAfter.before((Date) value)) {
////                               TODO: TODO: notification fails on preview, because application object is only a stub
//                               VaadinUtility.validationNotification(getApplication(), i18NSource,
//                                       getMessage("processdata.block.error.date.notafter").replaceFirst("%s", dwe.getNotAfter()));
//                               field.setValue(notAfter);
//                           }
//                       }
//                   }
//               });
           } catch (ParseException e) {
               handleException(getMessage("processdata.block.error.unparsable.date").replaceFirst("%s", dwe.getNotAfter()), e);
           }
       }
       if (hasText(dwe.getNotBefore())) {
           try {
               boolean usesCurrent = XmlConstants.DATE_CURRENT.equalsIgnoreCase(dwe.getNotBefore());
               final Date notBefore = (usesCurrent) ? sdf.parse(sdf.format(new Date())) : sdf.parse(dwe.getNotBefore());
               field.addValidator(new AbstractValidator(getMessage("processdata.block.error.date.notbefore").replaceFirst("%s", dwe.getNotBefore())) {
					@Override
					public boolean isValid(Object value) {
						Date formatedDateFromCalendarInput = formatTimeFromCalendarInput((Date)value,sdf);
						return value == null || isAfterCurrentDate(formatedDateFromCalendarInput);
					}

					private boolean isAfterCurrentDate(Date formatedDateFromCalendarInput){

					return	isLeftSideOpen()?  isAfterCurrentDateLeftSideOpen(formatedDateFromCalendarInput):isAfterCurrentDateLeftSideClosed(formatedDateFromCalendarInput);

					}

					private boolean isLeftSideOpen(){
						if(dwe.getDiscludeNotBefore()==null){
							return false;
						}
						else{
						return dwe.getDiscludeNotBefore();
						}
					}

					private boolean isAfterCurrentDateLeftSideClosed(Date formatedDateFromCalendarInput){

						return notBefore.equals(formatedDateFromCalendarInput)? true : isNotBefore(formatedDateFromCalendarInput);
					}

					private boolean isAfterCurrentDateLeftSideOpen(Date formatedDateFromCalendarInput){

						return  notBefore.equals(formatedDateFromCalendarInput)? false : isNotBefore(formatedDateFromCalendarInput);
					}
					
					
					
					private boolean isNotBefore(Date formatedDateFromCalendarInput){
						return   !notBefore.after(formatedDateFromCalendarInput);
						
					}
				});
//               field.addListener(new ValueChangeListener() {
//                   @Override
//                   public void valueChange(ValueChangeEvent event) {
//                       Object value = event.getProperty().getValue();
//                       if (value != null && value instanceof Date) {
//                           if (notBefore.after((Date) value)) {
////                               TODO: notification fails on preview, because application object is only a stub
//                               VaadinUtility.validationNotification(getApplication(), i18NSource,
//                                       getMessage("processdata.block.error.date.notbefore").replaceFirst("%s", dwe.getNotBefore()));
//                               field.setValue(notBefore);
//                           }
//                       }
//                   }
//               });
           } catch (ParseException e) {
               handleException(getMessage("processdata.block.error.unparsable.date").replaceFirst("%s", dwe.getNotBefore()), e);
           }
       }
       if (nvl(dwe.getRequired(), false)) {
           field.setRequired(true);
           if (hasText(dwe.getCaption())) {
               field.setRequiredError(getMessage("processdata.block.field-required-error") + " " + dwe.getCaption());
           } else {
               field.setRequiredError(getMessage("processdata.block.field-required-error"));
           }
       }
       if (dwe.getValue() != null)
           field.setValue(dwe.getValue());


       return field;
   }
   
    private Date formatTimeFromCalendarInput(Date date, SimpleDateFormat sdf){
		Date parsedDate;
		try {
			parsedDate = sdf.parse(sdf.format(date));
			return parsedDate;
		} catch (ParseException e) {
			handleException(getMessage("processdata.block.error.unparsable.date").replaceFirst("%s", date.toString()), e);
		}
		return date;
	}
   
    private void setupWidget(WidgetElement we, Component component) {
        if (hasText(we.getCaption())) {
            component.setCaption(getMessage(we.getCaption()));
        }
        if (we.getFullSize() != null && we.getFullSize()) {
            component.setSizeFull();
        }
        if (we.getUndefinedSize() != null && we.getUndefinedSize()) {
            component.setSizeUndefined();
        }
        if (hasText(we.getHeight())) {
            component.setHeight(we.getHeight());
        }
        if (hasText(we.getWidth())) {
            component.setWidth(we.getWidth());
        }
        if (we.getReadonly() != null && we.getReadonly()) {
            component.setReadOnly(we.getReadonly());
        }
        if (hasText(we.getStyle())) {
            component.addStyleName(we.getStyle());
        }
        if (we instanceof HasWidgetsElement && component instanceof SpacingHandler) {
            HasWidgetsElement hwe = (HasWidgetsElement) we;
            if (hwe.getSpacing() != null && hwe.getSpacing()) {
                ((SpacingHandler) component).setSpacing(hwe.getSpacing());
            }
        }

        if (we.getVisible() == Boolean.FALSE) {
            component.setVisible(false);
        }

        if (hasText(we.getBind()) && component instanceof Property) {
            Property property = (Property) component;
            boundProperties.put(property, we);
        }

        if (hasText(we.getDict()) && hasText(we.getProvider()) && component instanceof AbstractSelect) {
            AbstractSelect select = (AbstractSelect) component;
            dictContainers.put(select, we);
        }

        if (we instanceof AbstractSelectWidgetElement) {
            AbstractSelectWidgetElement aswe = (AbstractSelectWidgetElement) we;
            if (!hasText(we.getDict()) && !hasText(we.getProvider()) && hasText(aswe.getDictionaryAttribute())) {
                AbstractSelect select = (AbstractSelect) component;
                instanceDictContainers.put(select, aswe);

            }
        }
    }

    public String getWidgetsDefinition() {
        return widgetsDefinition;
    }

    public void setWidgetsDefinition(String widgetsDefinition) {
        this.widgetsDefinition = widgetsDefinition;
    }

}
