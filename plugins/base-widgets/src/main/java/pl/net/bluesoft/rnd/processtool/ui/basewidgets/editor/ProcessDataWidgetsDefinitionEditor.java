package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.event.*;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.addon.customfield.CustomField;
import org.vaadin.dialogs.ConfirmDialog;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDataWidgetsDefinitionEditor extends CustomField {

    public static final Logger LOGGER = Logger.getLogger(ProcessDataWidgetsDefinitionEditor.class.getName());
    private TabSheet tabSheet = new TabSheet();
    private TextArea rawText = new TextArea();
    private WidgetDefinitionLoader definitionLoader = WidgetDefinitionLoader.getInstance();
    private HierarchicalContainer hierarchicalContainer;
    private WidgetsDefinitionElement rootWidget;
    private Tree widgetTree;
    private ProcessDataPreviewer processDataPreviewer = new ProcessDataPreviewer();
    private VerticalLayout processPreview = new VerticalLayout();
//    private Label infoLabel = new Label();

    public ProcessDataWidgetsDefinitionEditor() {
        rawText.setNullRepresentation("");
        rawText.setWidth("100%");
        rawText.setHeight("400px");

//        infoLabel.setVisible(false);
//        infoLabel.setStyleName("error");

        VerticalLayout vl = new VerticalLayout();
        vl.addComponent(rawText);
        Button commitButton = new Button("Commit");
        vl.addComponent(commitButton); //TODO i18n
        commitButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    WidgetsDefinitionElement unmarshall = (WidgetsDefinitionElement) definitionLoader.unmarshall((String) rawText.getValue());
                    processXml();
                    List<XmlValidationError> xmlValidationErrors = unmarshall.validate();
                    if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
                        String msg = "";
                        for (XmlValidationError err : xmlValidationErrors) {
                            msg += err.getField() + ": " + err.getMessageKey() + "\n";    //TODO i18n
                        }
                        getApplication().getMainWindow().showNotification("Validation failed",
                                msg,
                                Window.Notification.TYPE_ERROR_MESSAGE);//TODO i18n
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


        hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty("name", String.class, null);
        hierarchicalContainer.addContainerProperty("widget", Object.class, null);
        //TODO parse and add items

//        rootWidget = processXml();

        widgetTree = new Tree();
        widgetTree.setContainerDataSource(hierarchicalContainer);
        widgetTree.setDragMode(Tree.TreeDragMode.NODE);
        widgetTree.setItemCaptionPropertyId("name");
        widgetTree.setDropHandler(new TreeSortDropHandler(widgetTree, hierarchicalContainer));
        widgetTree.setWidth("100%");
//        widgetTree.expandItemsRecursively(rootWidget);
        widgetTree.addShortcutListener(new ShortcutListener("Delete", null, ShortcutAction.KeyCode.DELETE) {
            @Override
            public void handleAction(Object sender, Object target) {
                if (target instanceof Tree) {
                    Tree target1 = (Tree) target;
                    final Object itemId = target1.getValue();
                    if (itemId != null) {
                        ConfirmDialog.show(getApplication().getMainWindow(),
                                "Remove item?", //TODO i18n
                                new ConfirmDialog.Listener() {
                                    @Override
                                    public void onClose(ConfirmDialog confirmDialog) {
                                        if (confirmDialog.isConfirmed())
                                            hierarchicalContainer.removeItem(itemId);
                                        refreshRawXmlAndPreview();
                                    }
                                });
                    }
                }
            }
        });


        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.addComponent(getAvailableWidgetsComponent());
        editorLayout.addComponent(new Label("Drag a control from a list above to add it to a hierarchy. Press Delete or drag a selected node back to a list above to delete it from a hierarchy.")); //TODO i18n
        Panel panel = new Panel("Widget hierarchy");
        panel.setHeight("340px");
        panel.setWidth("250px");
        panel.addComponent(widgetTree);

        
        HorizontalLayout hl = new HorizontalLayout();
        hl.addComponent(panel);
        widgetTree.setWidth("100%");

        final VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("100%");
//        hl.addComponent(panel); //panel with a tree
        hl.addComponent(formLayout);
        hl.setExpandRatio(formLayout, 1.0f);
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setMargin(true);

        editorLayout.addComponent(hl);
        final Form[] prevFormHandler = new Form[1];
        widgetTree.addListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final Object itemId = event.getItemId();
                if (prevFormHandler[0] != null && prevFormHandler[0].isModified()) {
                    ConfirmDialog.show(getApplication().getMainWindow(),
                            "Unsaved data exists, abandon?", //TODO i18n
                            new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(ConfirmDialog confirmDialog) {
                                    if (confirmDialog.isConfirmed())
                                        renderForm(itemId);
                                }
                            });
                } else {
                    renderForm(itemId);
                }
            }

            private void renderForm(final Object itemId) {
                prevFormHandler[0] = null;
                formLayout.removeAllComponents();
                if (itemId != null) {
                    final Object clone;
                    final Class classOfItem = itemId.getClass();
                    try {
                        clone = BeanUtils.cloneBean(itemId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    final Form form = new Form();

                    form.setFormFieldFactory(new DefaultFieldFactory() {

                        @Override
                        public com.vaadin.ui.Field createField(Item item, Object propertyId, Component uiContext) {
                            Property property = item.getItemProperty(propertyId);
                            Class<?> cls = property.getType();
                            Class[] supported = new Class[] { String.class, Boolean.class, Integer.class };
                            if (!Arrays.asList(supported).contains(cls)) {
                                return null;
                            }
                            Field reflectField = findField(propertyId, classOfItem);
                            if (reflectField != null) {
                                com.vaadin.ui.Field field = super.createField(item, propertyId, uiContext);
                                AvailableOptions opts = reflectField.getAnnotation(AvailableOptions.class);
                                if (opts != null && opts.value() != null) {
                                    NativeSelect ns = new NativeSelect();
                                    field = ns;
                                    field.setCaption(createCaptionByPropertyId(propertyId));
                                    for (String opt : opts.value()) {
                                        ns.addItem(opt);
                                    }
                                }
                                if (field instanceof AbstractField) {
                                    AbstractField abstractField = (AbstractField) field;
                                    abstractField.setImmediate(true);
                                }

                                if (field instanceof AbstractTextField) {
                                    AbstractTextField textField = (AbstractTextField) field;
                                    textField.setNullRepresentation("");
                                }
                                if (cls.equals(Integer.class)) {
                                    field.addValidator(new IntegerValidator("is.not.an.integer"));
                                    field.setWidth("100px");
                                } else {
                                    field.setWidth("100%");

                                }
                                if (reflectField.getAnnotation(RequiredAttribute.class) != null) {
                                    field.setRequired(true);
                                }
                                return field;
                            }
                            return null;

                        }
                    });
                    form.setWidth("100%");
                    form.setWriteThrough(false);
                    form.setCaption(itemId.getClass().getSimpleName());
                    form.setItemDataSource(new BeanItem(clone));
                    formLayout.addComponent(form);
                    HorizontalLayout hl = new HorizontalLayout();
                    hl.setSpacing(true);
                    final Button commit = new Button("Commit");   //TODO i18n
                    commit.addListener(new Button.ClickListener(){
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            if (!form.isValid()) {
                                commit.getApplication().getMainWindow().showNotification("Please review validation errors",  //TODO i18n
                                        Window.Notification.TYPE_WARNING_MESSAGE);
                                return;
                            }

                            form.commit();
                            if (clone instanceof WidgetElement) {
                                List<XmlValidationError> xmlValidationErrors = ((WidgetElement) clone).validateElement();
                                if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
                                    String msg = "";
                                    for (XmlValidationError err : xmlValidationErrors) {
                                        msg += err.getField() + ": " + err.getMessageKey() + "\n";    //TODO i18n
                                    }
                                    commit.getApplication().getMainWindow().showNotification("Validation errors",  //TODO i18n
                                            msg, Window.Notification.TYPE_WARNING_MESSAGE);         //TODO i18n
                                    return;
                                } else {
                                    try {
                                        BeanUtils.copyProperties(itemId, clone);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                try {
                                    BeanUtils.copyProperties(itemId, clone);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            refreshRawXmlAndPreview();
                        }
                    });
                    hl.addComponent(commit);
                    formLayout.addComponent(hl);
                    prevFormHandler[0]= form;
                }
            }
        });

        editorLayout.setWidth("100%");
        tabSheet.setWidth("100%");
        tabSheet.addTab(editorLayout, "Editor");//TODO I18N
        tabSheet.addTab(vl, "Source");//TODO I18N
        tabSheet.addTab(processPreview, "Preview"); //TODO i18n
        tabSheet.setSelectedTab(editorLayout);
        setCompositionRoot(tabSheet);

    }

    private void refreshRawXmlAndPreview() {
        updateXml(rootWidget);
        List<XmlValidationError> xmlValidationErrors = rootWidget.validate();
        if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
            String msg = "";
            for (XmlValidationError err : xmlValidationErrors) {
                msg += err.getField() + ": " + err.getMessageKey() + "\n";    //TODO i18n
            }
            Window.Notification n = new Window.Notification("Validation errors",  //TODO i18n
                                                                    msg, Window.Notification.TYPE_TRAY_NOTIFICATION);
            n.setDelayMsec(4000);
            getApplication().getMainWindow().showNotification(n);
        } else {
            String marshall = definitionLoader.marshall(rootWidget);
            rawText.setValue(marshall);
            refreshPreview();
            Window.Notification n = new Window.Notification("Update successfull",  //TODO i18n
                                                            "XML widget definition generated successfully", Window.Notification.TYPE_TRAY_NOTIFICATION);
            n.setDelayMsec(2000);
            getApplication().getMainWindow().showNotification(n);
        }
    }

    private void refreshPreview() {
        processPreview.removeAllComponents();
        processPreview.setWidth("100%");
        processPreview.addComponent(processDataPreviewer.render(rootWidget));
    }

    private void updateXml(Object itemId) {
        //fix relations between widgets
        Collection<?> children = hierarchicalContainer.getChildren(itemId);
        if (itemId instanceof HasWidgetsElement) {
            HasWidgetsElement hasWidgetsElement = (HasWidgetsElement) itemId;
            ArrayList<WidgetElement> widgets = new ArrayList<WidgetElement>();
            if (children != null) for (Object subItemId : children) {
                updateXml(subItemId);
                if (subItemId instanceof WidgetElement)
                    widgets.add((WidgetElement) subItemId);
            }
            hasWidgetsElement.setWidgets(widgets);        
        } else if (itemId instanceof SelectWidgetElement) { //special case, but it should be supported in more generic manner
            SelectWidgetElement selectWidgetElement = (SelectWidgetElement) itemId;
            ArrayList<ItemElement> values = new ArrayList<ItemElement>();
            if (children != null) for (Object subItemId : children) {
                if (subItemId instanceof ItemElement) {
                    values.add((ItemElement) subItemId);
                } else if (subItemId instanceof ScriptElement) {
                    selectWidgetElement.setScript((ScriptElement) subItemId);
                }
            }
            selectWidgetElement.setValues(values);
        }
    }

    private WidgetsDefinitionElement processXml() {
        hierarchicalContainer.removeAllItems();
        try {
            rootWidget = (WidgetsDefinitionElement) definitionLoader.unmarshall(String.valueOf(rawText.getValue()));
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            rootWidget = new WidgetsDefinitionElement();
        }

        Item item = hierarchicalContainer.addItem(rootWidget);
        item.getItemProperty("name").setValue("widgets");
        item.getItemProperty("widget").setValue(rootWidget);

        hierarchicalContainer.setChildrenAllowed(rootWidget, true);
        processWidgetsTree(hierarchicalContainer, rootWidget);
        widgetTree.expandItemsRecursively(rootWidget);
        refreshPreview();
        return rootWidget;
    }

    private static Field findField(Object propertyId, Class classOfItem) {
        Field declaredField;
        try {
            declaredField = classOfItem.getDeclaredField(String.valueOf(propertyId));
        } catch (NoSuchFieldException e) {
            declaredField = null;
        }
        if (declaredField == null && !Object.class.equals(classOfItem))
            return findField(propertyId, classOfItem.getSuperclass());
        return declaredField;
    }

    private void processWidgetsTree(HierarchicalContainer hierarchicalContainer, Object el) {
        Field field = getFieldWithAnnotation(el.getClass(), XmlElements.class);     
        Collection coll = null;
        if (field != null) {
            try {
                Object property = PropertyUtils.getProperty(el, field.getName());
                if (property instanceof Collection) {
                    coll = (Collection) property;
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (coll != null) for (Object widget : coll) {
            Item subItem = hierarchicalContainer.addItem(widget);
            hierarchicalContainer.setParent(widget, el);
            subItem.getItemProperty("name").setValue(widget.getClass().getSimpleName());
            subItem.getItemProperty("widget").setValue(widget);
            if (getFieldAnnotation(widget.getClass(), XmlElements.class) != null) {
                hierarchicalContainer.setChildrenAllowed(widget, true);
                processWidgetsTree(hierarchicalContainer, widget);
            } else {
                hierarchicalContainer.setChildrenAllowed(widget, false);
            }
        }
    }

    private class TreeSortDropHandler implements DropHandler {
        private final Tree tree;

        public TreeSortDropHandler(Tree tree, HierarchicalContainer container) {
            this.tree = tree;
        }

        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }

        public void drop(DragAndDropEvent dropEvent) {
            Transferable t = dropEvent.getTransferable();
            Component src = t.getSourceComponent();
            Object sourceItemId;           
            Object newItemId = null;
            Item subItem = null;
            HierarchicalContainer container = (HierarchicalContainer) tree.getContainerDataSource();
            if (src instanceof WidgetDragAndDropWrapper) {
                //new item has been dragged
                WidgetDragAndDropWrapper dragAndDropWrapper = (WidgetDragAndDropWrapper) src;
                Class cls = dragAndDropWrapper.getCls();
                Object widgetElement = null;
                try {
                    widgetElement = cls.newInstance();
                    subItem = container.addItem(widgetElement);
                    newItemId = widgetElement;
                    subItem.getItemProperty("name").setValue(widgetElement.getClass().getSimpleName());
                    subItem.getItemProperty("widget").setValue(widgetElement);
                    if (getFieldAnnotation(cls, XmlElements.class) != null) {
                        container.setChildrenAllowed(widgetElement, true);
                        tree.expandItem(widgetElement);
                    } else {
                        container.setChildrenAllowed(widgetElement, false);
                    }
                    sourceItemId = widgetElement;
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    src.getApplication().getMainWindow().showNotification("Widget creation failed",
                                                e.getClass().getName() + ", " + e.getMessage(),
                                                Window.Notification.TYPE_ERROR_MESSAGE);//TODO i18n
                    if (subItem != null && widgetElement != null) {
                        container.removeItem(widgetElement);
                    }
                    return;
                }
            } else {
                if (src != tree || !(t instanceof DataBoundTransferable)) {
                    return;
                }
                sourceItemId = ((DataBoundTransferable) t).getItemId();
            }
            

            Tree.TreeTargetDetails dropData = ((Tree.TreeTargetDetails) dropEvent
                    .getTargetDetails());
            Object targetItemId = dropData.getItemIdOver();
            VerticalDropLocation location = dropData.getDropLocation();
            if (targetItemId instanceof WidgetsDefinitionElement) {  //the can be only one! ... root element2
                location = VerticalDropLocation.MIDDLE;
            }
            if(!moveNode(sourceItemId, targetItemId, location)) {
                if (newItemId != null) {
                    container.removeItem(newItemId);
                }
            } else {
                refreshRawXmlAndPreview();
            }
        }

        private boolean moveNode(Object sourceItemId, Object targetItemId,
                                 VerticalDropLocation location) {
            HierarchicalContainer container = (HierarchicalContainer) tree.getContainerDataSource();
            if (sourceItemId == null || targetItemId == null) return false;
            Class srcClass = sourceItemId.getClass();
            Class targetClass = targetItemId.getClass();
            if (location == VerticalDropLocation.MIDDLE) {
                if (checkIfParentChildRelationIsPossible(srcClass, targetClass)) return false;
                if (container.setParent(sourceItemId, targetItemId)
                        && container.hasChildren(targetItemId)) {
                    container.moveAfterSibling(sourceItemId, null);
                }
            } else if (location == VerticalDropLocation.TOP) {
                Object parentId = container.getParent(targetItemId);
                if (checkIfParentChildRelationIsPossible(srcClass, parentId.getClass())) return false;
                if (container.setParent(sourceItemId, parentId)) {
                    container.moveAfterSibling(sourceItemId, targetItemId);
                    container.moveAfterSibling(targetItemId, sourceItemId);
                }
            } else if (location == VerticalDropLocation.BOTTOM) {
                Object parentId = container.getParent(targetItemId);
                if (checkIfParentChildRelationIsPossible(srcClass, parentId.getClass())) return false;
                if (container.setParent(sourceItemId, parentId)) {
                    container.moveAfterSibling(sourceItemId, targetItemId);
                }
            }
            return true;
        }


        private boolean checkIfParentChildRelationIsPossible(Class srcClass, Class targetClass) {
            XmlElements targetClassAnnotation = (XmlElements) getFieldAnnotation(targetClass, XmlElements.class);
            if (targetClassAnnotation == null) {
                return true;
            }
            boolean found = false;
            for (XmlElement xe: targetClassAnnotation.value()) {
                if (srcClass.isAssignableFrom(xe.type())) {
                    found = true;
                }
            }
            if (!found) {
                return true;
            }
            return false;
        }
    }
    
    private static class WidgetDragAndDropWrapper extends DragAndDropWrapper {

        private Class cls;

        private WidgetDragAndDropWrapper(Component root, Class cls) {
            super(root);
            this.cls = cls;
        }

        public Class getCls() {
            return cls;
        }

        public void setCls(Class cls) {
            this.cls = cls;
        }
    }
    private Component getAvailableWidgetsComponent() {
        CssLayout cssLayout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof WidgetDragAndDropWrapper) {
                    WidgetDragAndDropWrapper widgetDragAndDropWrapper = (WidgetDragAndDropWrapper) c;
                    Class cls = widgetDragAndDropWrapper.getCls();
                    String basicCss = "float: left; margin: 3px; padding: 3px;  display: inline; font-weight: bold; border: 2px solid ";
                    if (getFieldAnnotation(cls, XmlElements.class) != null)
                        return basicCss + "#287ece;";
                    else if (WidgetElement.class.isAssignableFrom(cls))
                        return basicCss + "#60b30e;";
                    else
                        return basicCss + "#c6c6c6";
                } 
                return super.getCss(c);
            }
        };
        cssLayout.setWidth("100%");
        Class[] supportedClasses = definitionLoader.getSupportedClasses();
        for (Class cls : supportedClasses) {
            if (getAnnotation(cls, XmlRootElement.class) != null) continue; //ignore root elements
            if (Modifier.isAbstract(cls.getModifiers())) continue;
            Label lbl = new Label(cls.getSimpleName());
            lbl.setSizeUndefined();            
            DragAndDropWrapper c = new WidgetDragAndDropWrapper(lbl, cls);
            c.setData(cls);
            c.setSizeUndefined();
            cssLayout.addComponent(c);
            c.setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
        }

        DragAndDropWrapper wr = new DragAndDropWrapper(cssLayout);
        wr.setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent event) {
                Transferable t = event.getTransferable();
                Component src = t.getSourceComponent();
                if (src != widgetTree || !(t instanceof DataBoundTransferable)) {
                    return;
                }
                Object sourceItemId = ((DataBoundTransferable) t).getItemId();
                hierarchicalContainer.removeItem(sourceItemId);
                refreshRawXmlAndPreview();
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        return wr;

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
    public Class<?> getType() {
        return rawText.getType();
    }

    @Override
    public boolean isReadOnly() {
        return rawText.isReadOnly();
    }

    @Override
    public boolean isInvalidCommitted() {
        return rawText.isInvalidCommitted();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        rawText.setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        rawText.commit();
    }

    @Override
    public void discard() throws SourceException {
        rawText.discard();
    }

    @Override
    public boolean isModified() {
        return rawText.isModified();
    }

    @Override
    public boolean isWriteThrough() {
        return rawText.isWriteThrough();
    }

    @Override
    public void setWriteThrough(boolean writeTrough) throws SourceException, Validator.InvalidValueException {
        rawText.setWriteThrough(writeTrough);
    }

    @Override
    public boolean isReadThrough() {
        return rawText.isReadThrough();
    }

    @Override
    public void setReadThrough(boolean readTrough) throws SourceException {
        rawText.setReadThrough(readTrough);
    }

    @Override
    public String toString() {
        return rawText.toString();
    }

    @Override
    public Object getValue() {
        return rawText.getValue();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        rawText.setValue(newValue);
        rootWidget = processXml();
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
                processXml();

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
        processXml();
    }

    @Override
    public void addValidator(Validator validator) {
        rawText.addValidator(validator);
    }

    @Override
    public Collection<Validator> getValidators() {
        return rawText.getValidators();
    }

    @Override
    public void removeValidator(Validator validator) {
        rawText.removeValidator(validator);
    }

    @Override
    public boolean isValid() {
        return rawText.isValid();
    }

    @Override
    public void validate() throws Validator.InvalidValueException {        
        rawText.validate();
        validateXmlSyntaxAndCorrectness();
    }


    @Override
    public boolean isInvalidAllowed() {
        return rawText.isInvalidAllowed();
    }

    @Override
    public void setInvalidAllowed(boolean invalidAllowed) throws UnsupportedOperationException {
        rawText.setInvalidAllowed(invalidAllowed);
    }

    @Override
    public ErrorMessage getErrorMessage() {
        return rawText.getErrorMessage();
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        rawText.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        rawText.removeListener(listener);
    }

//    @Override
//    protected void fireValueChange(boolean repaintIsNotNeeded) {
//        rawText.fireValueChange(repaintIsNotNeeded);
//    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        rawText.valueChange(event);
    }

    @Override
    public void focus() {
        rawText.focus();
    }

//    @Override
//    protected void setInternalValue(Object newValue) {
//        rawText.setInternalValue(newValue);
//    }

    @Override
    public boolean isRequired() {
        return rawText.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        rawText.setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        rawText.setRequiredError(requiredMessage);
    }

    @Override
    public String getRequiredError() {
        return rawText.getRequiredError();
    }

//    @Override
//    protected boolean isEmpty() {
//        return rawText.isEmpty();
//    }

    @Override
    public boolean isValidationVisible() {
        return rawText.isValidationVisible();
    }

    @Override
    public void setValidationVisible(boolean validateAutomatically) {
        rawText.setValidationVisible(validateAutomatically);
    }

    @Override
    public void setCurrentBufferedSourceException(SourceException currentBufferedSourceException) {
        rawText.setCurrentBufferedSourceException(currentBufferedSourceException);
    }

    private static Annotation getAnnotation(Class cls, Class<? extends Annotation> annotationCls) {
        Annotation res = cls.getAnnotation(annotationCls);
        if (res != null) {
            return res;
        } else if (!cls.equals(Object.class)) {
            return getAnnotation(cls.getSuperclass(), annotationCls);
        } else {
            return null;
        }
    }
    private static Annotation getFieldAnnotation(Class cls, Class<? extends Annotation> annotationCls) {
        for (Field f : cls.getDeclaredFields()) {
            Annotation annotation = f.getAnnotation(annotationCls);
            if (annotation != null) return annotation;
        }
        if (!cls.equals(Object.class)) {
            return getFieldAnnotation(cls.getSuperclass(), annotationCls);
        } else {
            return null;
        }
    }
    
    private static Field getFieldWithAnnotation(Class cls, Class<? extends Annotation> annotationCls) {
        for (Field f : cls.getDeclaredFields()) {
            Annotation annotation = f.getAnnotation(annotationCls);
            if (annotation != null) return f;
        }
        if (!cls.equals(Object.class)) {
            return getFieldWithAnnotation(cls.getSuperclass(), annotationCls);
        } else {
            return null;
        }
    }

}
