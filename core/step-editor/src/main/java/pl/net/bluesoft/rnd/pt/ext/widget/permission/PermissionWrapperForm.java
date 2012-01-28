package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;

import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_PRIVILEDGE_NAME;
import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_ROLE_NAME;
import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_SHORT_NAME;

public class PermissionWrapperForm extends Form implements FormFieldFactory {
    
    public PermissionWrapperForm() {
        this(new BeanItem<PermissionWrapper>(new PermissionWrapper()));
    }

    public PermissionWrapperForm(BeanItem<PermissionWrapper> bean) {
        setFormFieldFactory(this);
        setWriteThrough(true);
        setReadThrough(true);
        setImmediate(true);

        setItemDataSource(bean);
        setVisibleItemProperties(new Object[] {
                PROPERTY_PRIVILEDGE_NAME,
                PROPERTY_ROLE_NAME
        });
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        final BeanItem<PermissionWrapper> beanItem = (BeanItem<PermissionWrapper>) item;
        final PermissionWrapper wrapper = beanItem.getBean();

        Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);

        if (PROPERTY_PRIVILEDGE_NAME.equals(propertyId)) {
            field.setEnabled(wrapper.isPriviledgeNameEditable());
        }

        if (!PROPERTY_SHORT_NAME.equals(propertyId)) {
            // Don't register this on the shortName property itself, otherwise it will cause StackOverflow
            field.addListener(new ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    // change the value using the wrapping Property so UI components will be
                    // notified about this change
                    beanItem.getItemProperty(PROPERTY_SHORT_NAME).setValue(wrapper.getShortNameFromAttributes());
                }
            });
        }

        if (field instanceof AbstractTextField) {
            AbstractTextField textField = (AbstractTextField) field;
            textField.setNullRepresentation("");
        }

        return field;
    }

}
