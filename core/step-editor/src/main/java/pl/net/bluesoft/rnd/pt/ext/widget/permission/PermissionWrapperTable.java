package pl.net.bluesoft.rnd.pt.ext.widget.permission;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

public class PermissionWrapperTable extends Table implements Table.ColumnGenerator {

    private BeanItemContainer<PermissionWrapper> dataSourceContainer;

    public PermissionWrapperTable() {
        dataSourceContainer = new BeanItemContainer<PermissionWrapper>(PermissionWrapper.class);
        setContainerDataSource(dataSourceContainer);
        setSelectable(true);
        setColumnHeader("foo", VaadinUtility.getThreadI18nSource().getMessage("permission.list"));
        addGeneratedColumn("foo", this);
        setVisibleColumns(new String[] { "foo" });
    }

    public BeanItemContainer<PermissionWrapper> getDataSourceContainer() {
        return dataSourceContainer;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        PermissionWrapper wrapper = (PermissionWrapper) itemId;
        String name = wrapper.getPriviledgeName();
        if (name == null) {
            name = VaadinUtility.getThreadI18nSource().getMessage("permission.new");
        } else if (wrapper.getRoleName() != null) {
            name += " : " + wrapper.getRoleName();
        }
        return name;
    }
}
