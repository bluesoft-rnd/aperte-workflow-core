package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.model.QueueOrder;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class ProcessInstanceFilterSortingColumn
{
    private String columnName;
    private int priority;
    private QueueOrder order;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public QueueOrder getOrder() {
        return order;
    }

    public void setOrder(QueueOrder order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInstanceFilterSortingColumn that = (ProcessInstanceFilterSortingColumn) o;

        if (columnName != null ? !columnName.equals(that.columnName) : that.columnName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return columnName != null ? columnName.hashCode() : 0;
    }
}
