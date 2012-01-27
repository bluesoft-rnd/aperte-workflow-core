package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;

import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_PRIVILEDGE_NAME;
import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_ROLE_NAME;

public class PermissionWrapperForm extends Form implements FormFieldFactory {

    public PermissionWrapperForm() {
        this(new PermissionWrapper());
    }

    public PermissionWrapperForm(PermissionWrapper permissionWrapper) {
        setItemDataSource(new BeanItem<PermissionWrapper>(permissionWrapper));
        setFormFieldFactory(this);
        setImmediate(true);
        setWriteThrough(true);
        setVisibleItemProperties(new Object[] {
                PROPERTY_PRIVILEDGE_NAME,
                PROPERTY_ROLE_NAME
        });
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        BeanItem<PermissionWrapper> beanItem = (BeanItem<PermissionWrapper>) item;
        PermissionWrapper permissionWrapper = beanItem.getBean();

        Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
        field.setWriteThrough(true);

        if (PROPERTY_PRIVILEDGE_NAME.equals(propertyId)) {
            field.setEnabled(permissionWrapper.isPriviledgeNameEditable());
        }

        if (field instanceof AbstractTextField) {
            AbstractTextField textField = (AbstractTextField) field;
            textField.setNullRepresentation("");
        }

        return field;
    }

}
