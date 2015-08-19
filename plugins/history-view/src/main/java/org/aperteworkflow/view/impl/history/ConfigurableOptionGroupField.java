package org.aperteworkflow.view.impl.history;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;
import org.aperteworkflow.util.vaadin.ui.OrderedLayoutFactory;
import org.vaadin.addon.customfield.CustomField;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroup;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroupItemComponent;
import pl.net.bluesoft.util.lang.Transformer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class ConfigurableOptionGroupField<T> extends CustomField implements Container {
    private FlexibleOptionGroup optionGroup;
    private Transformer<T, String> itemCaptionResolver;
    private OrderedLayoutFactory layoutFactory;
    private ItemComponentGenerator<T> componentGenerator;
    private Map<T, Component> generatedComponentsMap;
    private boolean disableOthersOnChange = true;

    public ConfigurableOptionGroupField(Collection<T> collection, Transformer<T, String> itemCaptionResolver, ItemComponentGenerator<T> componentGenerator,
                                        Boolean multiSelect) {
        this.itemCaptionResolver = itemCaptionResolver;
        this.optionGroup = new FlexibleOptionGroup(collection);
        this.componentGenerator = componentGenerator != null ? componentGenerator : new DefaultItemComponentGenerator<T>();
        if (multiSelect != null) {
            setMultiSelect(multiSelect);
        }
        this.optionGroup.setImmediate(true);
        this.optionGroup.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                disableOthersOnChange(event.getProperty().getValue());
            }
        });
    }

    private void disableOthersOnChange(Object value) {
        if (!(value instanceof Collection) && value != null) {
            if (disableOthersOnChange) {
                for (T itemId : generatedComponentsMap.keySet()) {
                    generatedComponentsMap.get(itemId).setEnabled(itemId.equals(value));
                }
            }
        }
    }

    public ConfigurableOptionGroupField(Collection<T> collection, Transformer<T, String> itemCaptionResolver, ItemComponentGenerator<T> componentGenerator) {
        this(collection, itemCaptionResolver, componentGenerator, null);
    }

    public ConfigurableOptionGroupField(Collection<T> collection) {
        this(collection, null, null);
    }

    protected AbstractOrderedLayout createLayout() {
        return layoutFactory != null ? layoutFactory.create() : new AligningHorizontalLayout(Alignment.MIDDLE_LEFT);
    }

    public ConfigurableOptionGroupField<T> setMultiSelect(boolean multiSelect) {
        this.optionGroup.setMultiSelect(multiSelect);
        this.disableOthersOnChange = !multiSelect;
        return this;
    }

    public ConfigurableOptionGroupField<T> setItemCaptionResolver(Transformer<T, String> itemCaptionResolver) {
        this.itemCaptionResolver = itemCaptionResolver;
        return this;
    }

    public ConfigurableOptionGroupField<T> setItemComponentGenerator(ItemComponentGenerator<T> componentGenerator) {
        this.componentGenerator = componentGenerator != null ? componentGenerator : new DefaultItemComponentGenerator<T>();
        return this;
    }

    public ConfigurableOptionGroupField<T> setLayoutFactory(OrderedLayoutFactory layoutFactory) {
        this.layoutFactory = layoutFactory;
        return this;
    }

    public ConfigurableOptionGroupField setDisableOthersOnChange(boolean disable) {
        this.disableOthersOnChange = disable;
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        optionGroup.setReadOnly(readOnly);
    }

    public void addOptionChangedListener(ValueChangeListener listener) {
        optionGroup.addListener(listener);
    }

    public ConfigurableOptionGroupField<T> init() {
        AbstractOrderedLayout layout = createLayout();
        generatedComponentsMap = new HashMap<T, Component>();
        for (Iterator<FlexibleOptionGroupItemComponent> it = optionGroup.getItemComponentIterator(); it.hasNext(); ) {
            FlexibleOptionGroupItemComponent comp = it.next();
            T itemId = (T) comp.getItemId();
            Component generatedComponent = componentGenerator.generate(optionGroup, itemId, comp, itemCaptionResolver);
            layout.addComponent(generatedComponent);
            generatedComponentsMap.put(itemId, generatedComponent);
        }
        Object value = optionGroup.getValue();
        if (!optionGroup.isMultiSelect()) {
            Collection<?> itemIds = optionGroup.getContainerDataSource().getItemIds();
            if (value != null && itemIds.contains(value)) {
                optionGroup.setValue(value);
            }
            else if (!itemIds.isEmpty()) {
                optionGroup.setValue(value = itemIds.iterator().next());
            }
        }
        disableOthersOnChange(value);
        layout.addListener(createLayoutClickListener());
        setCompositionRoot(layout);
        return this;
    }

    protected LayoutClickListener createLayoutClickListener() {
        return new LayoutClickListener() {
            public void layoutClick(LayoutClickEvent event) {
                FlexibleOptionGroupItemComponent c = null;
                boolean allowUnselection = optionGroup.isMultiSelect();
                if (event.getChildComponent() instanceof FlexibleOptionGroupItemComponent) {
                    c = (FlexibleOptionGroupItemComponent) event.getChildComponent();
                }
                else if (event.getChildComponent() instanceof AbstractComponent) {
                    Object data = ((AbstractComponent) event.getChildComponent()).getData();
                    if (data instanceof FlexibleOptionGroupItemComponent) {
                        c = (FlexibleOptionGroupItemComponent) data;
                    }
                    if (event.getChildComponent() instanceof HorizontalLayout) {
                        allowUnselection = false;
                    }
                }
                if (c != null) {
                    Object itemId = c.getItemId();
                    if (optionGroup.isSelected(itemId) && allowUnselection) {
                        optionGroup.unselect(itemId);
                    }
                    else {
                        optionGroup.select(itemId);
                    }
                }
            }
        };
    }

    public static class DefaultItemComponentGenerator<T> implements ItemComponentGenerator<T> {
        @Override
        public Component generate(final FlexibleOptionGroup optionGroup, final T itemId,
                                  final FlexibleOptionGroupItemComponent itemComponent,
                                  final Transformer<T, String> itemCaptionResolver) {
            final Label title = new Label(itemCaptionResolver != null ? itemCaptionResolver.transform(itemId)
                    : itemComponent.getCaption(), Label.CONTENT_XHTML) {{
                setWidth("100%");
                addStyleName("pointer");
                setData(itemComponent);
            }};
            HorizontalLayout hl = new HorizontalLayout();
            hl.addComponent(itemComponent);
            hl.addComponent(title);
            hl.setData(itemComponent);
            return hl;
        }
    }

    public static interface ItemComponentGenerator<T> {
        Component generate(FlexibleOptionGroup optionGroup, T itemId, FlexibleOptionGroupItemComponent itemComponent,
                           Transformer<T, String> itemCaptionResolver);
    }

    @Override
    public Class<?> getType() {
        return Collection.class;
    }

    @Override
    public void setTabIndex(int tabIndex) {
        super.setTabIndex(tabIndex);
        optionGroup.setTabIndex(tabIndex);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        optionGroup.setPropertyDataSource(newDataSource);
        init();
    }

    @Override
    public Object getValue() {
        return optionGroup.getValue();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        optionGroup.setValue(newValue);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        optionGroup.commit();
    }

    @Override
    public void discard() throws SourceException {
        optionGroup.discard();
    }

    @Override
    public Item getItem(Object itemId) {
        return optionGroup.getItem(itemId);
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return optionGroup.getContainerPropertyIds();
    }

    @Override
    public Collection<?> getItemIds() {
        return optionGroup.getItemIds();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return optionGroup.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return optionGroup.getType(propertyId);
    }

    @Override
    public int size() {
        return optionGroup.size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return optionGroup.containsId(itemId);
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        return optionGroup.addItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        return optionGroup.addItem();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return optionGroup.removeItem(itemId);
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        return optionGroup.addContainerProperty(propertyId, type, defaultValue);
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        return optionGroup.removeContainerProperty(propertyId);
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        return optionGroup.removeAllItems();
    }
}
