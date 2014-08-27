package org.aperteworkflow.ui.help.datatable;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class JQueryDataTableColumn
{
    private String propertyName;
    private Integer index;
    private Boolean isSortable;
    private Boolean isSorted;
    private Boolean isSortedAsc;
    private Integer priority;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Boolean getSortable() {
        return isSortable;
    }

    public void setSortable(Boolean sortable) {
        isSortable = sortable;
    }

    public Boolean getSorted() {
        return isSorted;
    }

    public void setSorted(Boolean sorted) {
        isSorted = sorted;
    }

    public Boolean getSortedAsc() {
        return isSortedAsc;
    }

    public void setSortedAsc(Boolean sortedAsc) {
        isSortedAsc = sortedAsc;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
