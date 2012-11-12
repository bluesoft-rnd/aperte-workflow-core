package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.*;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.dialogs.ConfirmDialog;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.WidgetDefinitionLoader;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.*;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.EditorHelper.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDataHierarchyEditor extends VerticalLayout {
    public static final Logger LOGGER = Logger.getLogger(ProcessDataWidgetsDefinitionEditor.class.getName());

    private WidgetDefinitionLoader definitionLoader = WidgetDefinitionLoader.getInstance();
    private HierarchicalContainer hierarchicalContainer;
    private WidgetsDefinitionElement rootWidget;
    private Tree widgetTree = new Tree();
    private ProcessDataWidgetsDefinitionEditor editor;

    public ProcessDataHierarchyEditor(ProcessDataWidgetsDefinitionEditor editor) {
        this.editor = editor;
        initGUI();
    }

    private void initGUI() {
        setSizeUndefined();
        setWidth("100%");
        hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty("name", String.class, null);
        hierarchicalContainer.addContainerProperty("widget", Object.class, null);

        HorizontalLayout treeAndForm = new HorizontalLayout();
        final VerticalLayout formLayout = getFormLayout();
        treeAndForm.addComponent(getInitedWidgetTreePanel());
        treeAndForm.addComponent(formLayout);
        treeAndForm.setExpandRatio(formLayout, 1.0f);
        treeAndForm.setWidth("100%");
        treeAndForm.setSpacing(true);

        addComponent(getAvailableWidgetsComponent());
        addComponent(new Label(getLocalizedMessage("info")));
        addComponent(treeAndForm);
        setExpandRatio(treeAndForm, 1.0f);
    }

    private VerticalLayout getFormLayout() {
        final VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("100%");
        final WidgetPropertiesEditorFormComponent[] prevFormHandler = new WidgetPropertiesEditorFormComponent[1];
        widgetTree.addListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final Object itemId = event.getItemId();
                final WidgetPropertiesEditorFormComponent formComponent = prevFormHandler[0];
                if (formComponent != null && formComponent.getForm().isModified()) {
                    if (formComponent.getForm().isValid()) {
                        formComponent.commit();
                        renderForm(itemId);
                    } else {
                        ConfirmDialog.show(
                                getApplication().getMainWindow(),
                                getLocalizedMessage("unsaved-data-warning"),
                                new ConfirmDialog.Listener() {
                                    @Override
                                    public void onClose(ConfirmDialog confirmDialog) {
                                        if (confirmDialog.isConfirmed()) {
                                            renderForm(itemId);
                                        } else {
                                            widgetTree.select(formComponent.getItemId());
                                        }
                                    }
                                });
                    }
                } else {
                    renderForm(itemId);
                }
            }
            private void renderForm(Object itemId) {
                prevFormHandler[0] = null;
                formLayout.removeAllComponents();
                if (itemId != null) {
                    WidgetPropertiesEditorFormComponent editorFormComponent = new WidgetPropertiesEditorFormComponent(itemId, ProcessDataHierarchyEditor.this);
                    formLayout.addComponent(editorFormComponent);
                    prevFormHandler[0] = editorFormComponent;
                }
            }
        });
        return formLayout;
    }

    private Panel getInitedWidgetTreePanel() {
        widgetTree.setContainerDataSource(hierarchicalContainer);
        widgetTree.setDragMode(Tree.TreeDragMode.NODE);
        widgetTree.setItemCaptionPropertyId("name");
        widgetTree.setDropHandler(new TreeSortDropHandler(widgetTree, hierarchicalContainer));
        widgetTree.setWidth("100%");
        widgetTree.addShortcutListener(getDeleteShortcutListener());

        Panel panel = new Panel(getLocalizedMessage("widget-hierarchy"));
        panel.setHeight("340px");
        panel.setWidth("250px");
        panel.addComponent(widgetTree);
        return panel;
    }

    private ShortcutListener getDeleteShortcutListener() {
        return new ShortcutListener("Delete", null, ShortcutAction.KeyCode.DELETE) {
            @Override
            public void handleAction(Object sender, Object target) {
                if (target instanceof Tree) {
                    Tree target1 = (Tree) target;
                    final Object itemId = target1.getValue();
                    if (itemId != null) {
                        ConfirmDialog.show(
                                getApplication().getMainWindow(),
                                getLocalizedMessage("remove-item-confirm"),
                                new ConfirmDialog.Listener() {
                                    @Override
                                    public void onClose(ConfirmDialog confirmDialog) {
                                        if (confirmDialog.isConfirmed()) {
                                            removeItemFromTreeRecursively(itemId);
                                        }
                                        refreshRawXmlAndPreview();
                                    }
                                });
                    }
                }
            }
        };
    }

    private void removeItemFromTreeRecursively(Object itemId) {
        hierarchicalContainer.removeItemRecursively(itemId);
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
        } else if (itemId instanceof AbstractSelectWidgetElement) { //special case, but it should be supported in more generic manner
            AbstractSelectWidgetElement selectWidgetElement = (AbstractSelectWidgetElement) itemId;
            ArrayList<ItemElement> values = new ArrayList<ItemElement>();
            if (children != null) for (Object subItemId : children) {
                if (subItemId instanceof ItemElement) {
                    values.add((ItemElement) subItemId);
                }
            }
            selectWidgetElement.setValues(values);
        }
    }

    public void refreshRawXmlAndPreview() {
        updateXml(rootWidget);
        List<XmlValidationError> xmlValidationErrors = rootWidget.validate();
        if (xmlValidationErrors != null && !xmlValidationErrors.isEmpty()) {
            String msg = joinValidationErrors(xmlValidationErrors);
            Window.Notification n = new Window.Notification(getLocalizedMessage("validation-errors"),
                    msg, Window.Notification.TYPE_TRAY_NOTIFICATION);
            n.setDelayMsec(4000);
            getApplication().getMainWindow().showNotification(n);
        } else {
            editor.updateFromWidgetsDefinitionElement(rootWidget);
            Window.Notification n = new Window.Notification(getLocalizedMessage("update-success"),
                    getLocalizedMessage("xml-definition-generate-success"), Window.Notification.TYPE_TRAY_NOTIFICATION);
            n.setDelayMsec(2000);
            getApplication().getMainWindow().showNotification(n);
        }
    }

    public WidgetsDefinitionElement processXml(String value) {
        hierarchicalContainer.removeAllItems();
        try {
            rootWidget = (WidgetsDefinitionElement) definitionLoader.unmarshall(String.valueOf(value));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            rootWidget = new WidgetsDefinitionElement();
        }

        Item item = hierarchicalContainer.addItem(rootWidget);
        item.getItemProperty("name").setValue(rootWidget.getClass().getSimpleName());
        item.getItemProperty("widget").setValue(rootWidget);

        hierarchicalContainer.setChildrenAllowed(rootWidget, true);
        processWidgetsTree(rootWidget);
        widgetTree.expandItemsRecursively(rootWidget);
        editor.refreshPreview(rootWidget);
        return rootWidget;
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
                removeItemFromTreeRecursively(sourceItemId);
                refreshRawXmlAndPreview();
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        return wr;

    }


    private void processWidgetsTree(Object el) {
        java.lang.reflect.Field field = getFieldWithAnnotation(el.getClass(), XmlElements.class);
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
            addTreeItem(widget);
            hierarchicalContainer.setParent(widget, el);
            if (getFieldAnnotation(widget.getClass(), XmlElements.class) != null) {
                processWidgetsTree(widget);
            }
        }
    }

    private Item addTreeItem(Object widgetElement) {
        Class cls = widgetElement.getClass();
        Item subItem;
        subItem = hierarchicalContainer.addItem(widgetElement);
        subItem.getItemProperty("name").setValue(widgetElement.getClass().getSimpleName());
        subItem.getItemProperty("widget").setValue(widgetElement);
        if (getFieldAnnotation(cls, XmlElements.class) != null) {
            hierarchicalContainer.setChildrenAllowed(widgetElement, true);
            widgetTree.expandItem(widgetElement);
        } else {
            hierarchicalContainer.setChildrenAllowed(widgetElement, false);
        }
        return subItem;
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
//            Item subItem = null;
            HierarchicalContainer container = (HierarchicalContainer) tree.getContainerDataSource();
            if (src instanceof WidgetDragAndDropWrapper) {
                WidgetDragAndDropWrapper dragAndDropWrapper = (WidgetDragAndDropWrapper) src;
                Class cls = dragAndDropWrapper.getCls();
                Object widgetElement = null;
                try {
                    widgetElement = cls.newInstance();
                    /*subItem = */addTreeItem(widgetElement);
                    newItemId = widgetElement;
                    sourceItemId = widgetElement;
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    src.getApplication().getMainWindow().showNotification(getLocalizedMessage("widget-creation-failed"),
                            e.getClass().getName() + ", " + e.getMessage(),
                            Window.Notification.TYPE_ERROR_MESSAGE);
//                    if (subItem != null && widgetElement != null) {
//                        container.removeItem(widgetElement);
//                    }
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
            if (!moveNode(sourceItemId, targetItemId, location)) {
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
                if (!checkIfParentChildRelationIsPossible(srcClass, targetClass)) return false;
                if (container.setParent(sourceItemId, targetItemId)
                        && container.hasChildren(targetItemId)) {
                    container.moveAfterSibling(sourceItemId, null);
                }
            } else if (location == VerticalDropLocation.TOP) {
                Object parentId = container.getParent(targetItemId);
                if (!checkIfParentChildRelationIsPossible(srcClass, parentId.getClass())) return false;
                if (container.setParent(sourceItemId, parentId)) {
                    container.moveAfterSibling(sourceItemId, targetItemId);
                    container.moveAfterSibling(targetItemId, sourceItemId);
                }
            } else if (location == VerticalDropLocation.BOTTOM) {
                Object parentId = container.getParent(targetItemId);
                if (!checkIfParentChildRelationIsPossible(srcClass, parentId.getClass())) return false;
                if (container.setParent(sourceItemId, parentId)) {
                    container.moveAfterSibling(sourceItemId, targetItemId);
                }
            }
            return true;
        }


        private boolean checkIfParentChildRelationIsPossible(Class srcClass, Class targetClass) {
            XmlElements targetClassAnnotation = (XmlElements) getFieldAnnotation(targetClass, XmlElements.class);
            if (targetClassAnnotation == null) {
                return false;
            }
            for (XmlElement xe : targetClassAnnotation.value()) {
                if (srcClass.isAssignableFrom(xe.type())) {
                    return true;
                }
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
    }

}
