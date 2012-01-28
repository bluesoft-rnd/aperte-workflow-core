package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_PRIVILEDGE_NAME;
import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_ROLE_NAME;
import static pl.net.bluesoft.rnd.pt.ext.widget.permission.PermissionWrapper.PROPERTY_SHORT_NAME;

public class PermissionWrapperTable extends Table {

    private BeanItemContainer<PermissionWrapper> dataSourceContainer;

    public PermissionWrapperTable() {
        setSelectable(true);
        setImmediate(true);
        setReadThrough(true);
        setWriteThrough(true);
        dataSourceContainer = new BeanItemContainer<PermissionWrapper>(PermissionWrapper.class);
        setContainerDataSource(dataSourceContainer);
        setColumnHeader(PROPERTY_SHORT_NAME, VaadinUtility.getThreadI18nSource().getMessage("permission.list"));
        setVisibleColumns(new String[] { PROPERTY_SHORT_NAME });
    }

    public BeanItemContainer<PermissionWrapper> getDataSourceContainer() {
        return dataSourceContainer;
    }

}
