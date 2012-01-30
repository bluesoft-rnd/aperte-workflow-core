package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validatable;
import com.vaadin.data.Validator.InvalidValueException;
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
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttachmentAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.ProcessDataWidgetsDefinitionEditor;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.WidgetDefinitionLoader;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;
import pl.net.bluesoft.util.lang.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static com.vaadin.ui.Alignment.*;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

@AliasName(name = "ProcessData")
@AperteDoc(humanNameKey="widget.process_data_block.name", descriptionKey="widget.process_data_block.description")
@ChildrenAllowed(false)
@WidgetGroup("base-widgets")
public class ProcessDataBlockWidget extends BaseProcessToolWidget implements ProcessToolDataWidget, ProcessToolVaadinWidget {
    private static final Logger logger = Logger.getLogger(ProcessDataBlockWidget.class.getName());
    private static final Resolver resolver = new DefaultResolver();

    public static final String ATTRIBUTE_WIDGETS_DEFINITION = "widgetsDefinitionElement";

    private WidgetDefinitionLoader definitionLoader = WidgetDefinitionLoader.getInstance();

    private ProcessDictionaryRegistry processDictionaryRegistry;

    private Map<Property, WidgetElement> boundProperties = new HashMap<Property, WidgetElement>();
    private Map<AbstractSelect, WidgetElement> dictContainers = new HashMap<AbstractSelect, WidgetElement>();
    private Map<String, ProcessInstanceAttribute> processAttributes = new HashMap<String, ProcessInstanceAttribute>();
    protected WidgetsDefinitionElement widgetsDefinitionElement;
    private ProcessInstance processInstance;

    @AutoWiredProperty(required=true)
    @AutoWiredPropertyConfigurator(fieldClass = ProcessDataWidgetsDefinitionEditor.class)
    @AperteDoc(humanNameKey="widget.process_data_block.property.widgetsDefinition.name", descriptionKey="widget.process_data_block.property.widgetsDefinition.description")
    private String widgetsDefinition;

    @AutoWiredProperty
    private String caption;

    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = RichTextArea.class)
    private String comment;

    public void setDefinitionLoader(WidgetDefinitionLoader definitionLoader) {
        this.definitionLoader = definitionLoader;
    }

    public void setProcessDictionaryRegistry(ProcessDictionaryRegistry processDictionaryRegistry) {
        this.processDictionaryRegistry = processDictionaryRegistry;
    }

    @Override
    public void setContext(ProcessStateConfiguration state, ProcessStateWidget configuration, I18NSource i18NSource,
                           ProcessToolBpmSession bpmSession, Application application, Set<String> permissions, boolean isOwner) {
        super.setContext(state, configuration, i18NSource, bpmSession,
                application, permissions, isOwner);
        ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
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
                logException(getMessage("processdata.block.error.eval.other").replaceFirst("%s",
                        currentComponent.toString()).replaceFirst("%s", currentElement.getBind()), e);
            }
        }

        public abstract void evaluate(T component, WidgetElement element) throws Exception;
    }

    @Override
    public Collection<String> validateData(final ProcessInstance processInstance) {
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
    public void saveData(final ProcessInstance processInstance) {
        processAttributes.clear();
        for (ProcessInstanceAttribute attribute : processInstance.getProcessAttributes()) {
            processAttributes.put(attribute.getKey(), attribute);
        }

        new ComponentEvaluator<Property>(boundProperties) {
            @Override
            public void evaluate(Property component, WidgetElement element) throws Exception {
                if (!component.isReadOnly()) {
                    ProcessInstanceAttribute attribute = fetchOrCreateAttribute(element);
                    if (attribute instanceof ProcessInstanceSimpleAttribute) {
	                    if (element instanceof DateWidgetElement) {
		                   ((ProcessInstanceSimpleAttribute) attribute).setValue(
				            	new SimpleDateFormat(((DateWidgetElement)element).getFormat()).format(component.getValue())
		                   );
	                    } else if (component.getValue() != null) {
		                    ((ProcessInstanceSimpleAttribute) attribute).setValue(component.getValue().toString());
	                    }
                    } else {
                        if (component instanceof FileUploadComponent) {
                            ProcessInstanceAttachmentAttribute attachment = (ProcessInstanceAttachmentAttribute) component.getValue();
                            attachment.setProcessState(processInstance.getState());
                            attachment.setProcessInstance(processInstance);
                            attachment.setKey(attribute.getKey());
                        }
                        PropertyUtils.setProperty(processAttributes, element.getBind(), component.getValue());
                    }
                }
            }
        };
        processInstance.setProcessAttributes(new HashSet<ProcessInstanceAttribute>(processAttributes.values()));

    }

    @Override
    public void loadData(final ProcessInstance processInstance) {
        boundProperties.clear();
        dictContainers.clear();

        if (StringUtil.hasText(widgetsDefinition)) {
            widgetsDefinitionElement = (WidgetsDefinitionElement) definitionLoader.unmarshall(widgetsDefinition);
        }

        this.processInstance = processInstance;
        processAttributes.clear();
        for (ProcessInstanceAttribute attribute : processInstance.getProcessAttributes()) {
            processAttributes.put(attribute.getKey(), attribute);
        }
    }

    private void loadBindings() {
        new ComponentEvaluator<Property>(boundProperties) {
            @Override
            public void evaluate(Property component, WidgetElement element) throws Exception {
                Object value = null;
                try {
                    value = PropertyUtils.getProperty(processAttributes, element.getBind());
                } catch (NestedNullException e) {
                    logger.info(e.getMessage());
                }
                if (value != null) {
                    boolean readonly = component.isReadOnly();
                    if (readonly) {
                        component.setReadOnly(false);
                    }
	                value = value instanceof ProcessInstanceSimpleAttribute ?
			                ((ProcessInstanceSimpleAttribute) value).getValue() : value;
                    if (Date.class.isAssignableFrom(component.getType())) {
		                Date v = new SimpleDateFormat(((DateWidgetElement) element).getFormat()).parse(String.valueOf(
				                value));
		                component.setValue(v);

	                } else if (String.class.isAssignableFrom(component.getType())) {
                        component.setValue(nvl(value, ""));
	                }
                    if (readonly) {
                        component.setReadOnly(true);
                    }
                }
            }
        };
    }

    private void loadDictionaries() {
        new ComponentEvaluator<AbstractSelect>(dictContainers) {
            @Override
            public void evaluate(AbstractSelect component, WidgetElement element) throws Exception {

                ProcessDictionary dict = processDictionaryRegistry.getSpecificOrDefaultDictionary(
                        processInstance.getDefinition(), element.getProvider(),
                        element.getDict(), i18NSource.getLocale().toString());
                if (dict != null) {
                    int i = 0;
                    for (Object o : dict.items()) {
                        ProcessDictionaryItem itemProcess = (ProcessDictionaryItem) o;
                        component.addItem(itemProcess.getKey());
                        component.setItemCaption(itemProcess.getKey(), getMessage((String) itemProcess.getValue()));
                        if (element instanceof SelectWidgetElement) {
                            SelectWidgetElement select = (SelectWidgetElement) element;
                            if (select.getDefaultSelect() != null && i == select.getDefaultSelect()) {
                                component.setValue(itemProcess.getKey());
                            }
                        }
                        ++i;
                    }
                }
            }
        };
    }

    @Override
    public void addChild(ProcessToolWidget child) {
        throw new IllegalArgumentException("children are not supported in this widget");
    }

    @Override
    public Component render() {
        return widgetsDefinitionElement != null ? renderInternal() : new Label(getMessage("processdata.block.nothing.to.render"));
    }

    private void logException(String message, Exception e) {
        logger.severe(message + "<br/>" + e.getMessage());
        VaadinUtility.validationNotification(getApplication(), i18NSource, message + "<br/>" + e.getMessage());
    }

    private Component renderInternal() {
        ComponentContainer mainPanel = null;
        try {
            mainPanel = !hasText(widgetsDefinitionElement.getClassName()) ? new VerticalLayout()
                    : (ComponentContainer) getClass().getClassLoader().loadClass(widgetsDefinitionElement.getClassName()).newInstance();
        } catch (Exception e) {
            logException(getMessage("processdata.block.error.load.class").replaceFirst("%s", widgetsDefinitionElement.getClassName()), e);
        }

        setupWidget(widgetsDefinitionElement, mainPanel);

        for (WidgetElement we : widgetsDefinitionElement.getWidgets()) {
            processWidgetElement(widgetsDefinitionElement, we, mainPanel);
        }

        loadDictionaries();
        loadBindings();

        return mainPanel;
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
        } else if (element instanceof SelectWidgetElement) {
            component = createSelectField((SelectWidgetElement) element);
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

        return component;
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
        return cb;
    }

    private Select createSelectField(SelectWidgetElement swe) {
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
        }
//        else if (swe.getScript() != null) {
//            processScriptElement(select, swe.getScript());
//        }
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

    private void processScriptElement(Select select, ScriptElement script) {
        throw new RuntimeException("Not implemented yet!");
    }

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
				rta.setHeight(taw.getVisibleLines()*2+4, Sizeable.UNITS_EM);
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
			component = rta;
        } else {
            TextArea ta = new TextArea();
            if (taw.getVisibleLines() != null) {
                ta.setRows(taw.getVisibleLines());
            }
            if (taw.getLimit() != null) {
                ta.setMaxLength(taw.getLimit());
            }

            component = ta;
            if (nvl(taw.getRequired(), false)) {
                ta.setRequired(true);
                if (hasText(taw.getCaption())) {
                    ta.setRequiredError(getMessage("processdata.block.field-required-error") + " " + taw.getCaption());
                } else {
                    ta.setRequiredError(getMessage("processdata.block.field-required-error"));
                }
            }

            component = ta;
        }

        return component;
    }

    private Link createLink(LinkWidgetElement we) {
        Link link = new Link();
        link.setTargetName("_blank");
        link.setResource(new ExternalResource(we.getUrl()));
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

/*    private AbstractTextField createInputField(InputWidgetElement iwe) {
        AbstractTextField field = iwe.getSecret() != null && iwe.getSecret() ? new PasswordField() : new TextField();
        if (iwe.getMaxLength() != null) {
            field.setMaxLength(iwe.getMaxLength());
        }
        if (hasText(iwe.getRegexp()) && hasText(iwe.getRegexp())) {
            field.addValidator(new RegexpValidator(OXHelper.replaceXmlEscapeCharacters(iwe.getRegexp()), iwe.getErrorKey() != null ?
                iwe.getErrorKey() : getMessage("processdata.block.error.regexp").replaceFirst("%s", iwe.getRegexp())));
        }
        if (hasText(iwe.getBaseText())) {
            field.setValue(getMessage(iwe.getBaseText()));
        }
        if (hasText(iwe.getPrompt())) {
            field.setInputPrompt(getMessage(iwe.getPrompt()));
        }
        return field;
    }*/

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
                field.setRequiredError(getMessage("processdata.block.field-required-error") + " " + iwe.getCaption());
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
        return label;
    }

    private DateField createDateField(final DateWidgetElement dwe) {
        SimpleDateFormat sdf;
        try {
            sdf = new SimpleDateFormat(dwe.getFormat());
        } catch (Exception e) {
            logException(getMessage("processdata.block.error.unparsable.format").replaceFirst("%s", dwe.getFormat()), e);
            return null;
        }

        final PopupDateField field = new PopupDateField();
        field.setDateFormat(dwe.getFormat());
        field.setResolution(dwe.getShowMinutes() != null && dwe.getShowMinutes() ? DateField.RESOLUTION_MIN : DateField.RESOLUTION_DAY);
        if (hasText(dwe.getNotAfter())) {
            try {
                final Date notAfter = XmlConstants.DATE_CURRENT.equalsIgnoreCase(dwe.getNotAfter()) ? new Date() : sdf.parse(dwe.getNotAfter());
                field.addListener(new ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        Object value = event.getProperty().getValue();
                        if (value != null && value instanceof Date) {
                            if (notAfter.before((Date) value)) {
                                VaadinUtility.validationNotification(getApplication(), i18NSource,
                                        getMessage("processdata.block.error.date.notafter").replaceFirst("%s", dwe.getNotAfter()));
                                field.setValue(notAfter);
                            }
                        }
                    }
                });
            } catch (ParseException e) {
                logException(getMessage("processdata.block.error.unparsable.date").replaceFirst("%s", dwe.getNotAfter()), e);
            }
        }
        if (hasText(dwe.getNotBefore())) {
            try {
                final Date notBefore = XmlConstants.DATE_CURRENT.equalsIgnoreCase(dwe.getNotBefore()) ? new Date() : sdf.parse(dwe.getNotBefore());
                field.addListener(new ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        Object value = event.getProperty().getValue();
                        if (value != null && value instanceof Date) {
                            if (notBefore.after((Date) value)) {
                                VaadinUtility.validationNotification(getApplication(), i18NSource,
                                        getMessage("processdata.block.error.date.notbefore").replaceFirst("%s", dwe.getNotAfter()));
                                field.setValue(notBefore);
                            }
                        }
                    }
                });
            } catch (ParseException e) {
                logException(getMessage("processdata.block.error.unparsable.date").replaceFirst("%s", dwe.getNotBefore()), e);
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

        return field;
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

        if (hasText(we.getBind()) && component instanceof Property) {
            Property property = (Property) component;
            boundProperties.put(property, we);
        }

        if (hasText(we.getDict()) && hasText(we.getProvider()) && component instanceof AbstractSelect) {
            AbstractSelect select = (AbstractSelect) component;
            dictContainers.put(select, we);
        }
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getWidgetsDefinition() {
        return widgetsDefinition;
    }

    public void setWidgetsDefinition(String widgetsDefinition) {
        this.widgetsDefinition = widgetsDefinition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
