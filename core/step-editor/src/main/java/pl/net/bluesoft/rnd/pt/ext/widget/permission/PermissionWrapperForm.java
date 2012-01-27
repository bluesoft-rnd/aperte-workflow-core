package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;

public class PermissionWrapperForm extends Form implements FormFieldFactory {

    public PermissionWrapperForm() {
        this(new PermissionWrapper());
    }

    public PermissionWrapperForm(PermissionWrapper permissionWrapper) {
        setItemDataSource(new BeanItem<PermissionWrapper>(permissionWrapper));
        setFormFieldFactory(this);
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        BeanItem<PermissionWrapper> beanItem = (BeanItem<PermissionWrapper>) item;
        PermissionWrapper permissionWrapper = beanItem.getBean();

        Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
        if ("priviledgeName".equals(propertyId)) {
            field.setEnabled(permissionWrapper.isPriviledgeNameEditable());
        }

        return field;
    }

}
