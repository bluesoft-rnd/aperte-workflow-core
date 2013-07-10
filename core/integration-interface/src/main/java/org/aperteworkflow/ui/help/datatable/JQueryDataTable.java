package org.aperteworkflow.ui.help.datatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * jQuery org.aperteworkflow.ui.help.datatable class representaion
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class JQueryDataTable
{
    private String echo;
    private Integer pageOffset;
    private Integer pageLength;
    private List<JQueryDataTableColumn> columns = new ArrayList<JQueryDataTableColumn>();
    private List<JQueryDataTableColumn> sortingColumnOrder = new ArrayList<JQueryDataTableColumn>();

    public List<JQueryDataTableColumn> getColumns() {
        return columns;
    }

    public JQueryDataTableColumn getColumnAt(int index)
    {
        if(index > columns.size() - 1)
            return null;

        return columns.get(index);
    }

    public JQueryDataTableColumn getColumnByPropertyName(String propertyName)
    {
        for(JQueryDataTableColumn column: columns)
            if(column.getPropertyName().equals(propertyName))
                return column;

        return null;
    }

    public int getColumnsCount()
    {
        return this.columns.size();
    }

    public String getEcho() {
        return echo;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public JQueryDataTableColumn getFirstSortingColumn()
    {
        return  sortingColumnOrder.get(0);
    }

    public List<JQueryDataTableColumn> getSortingColumnOrder() {
        return sortingColumnOrder;
    }

    public Integer getPageOffset() {
        return pageOffset;
    }

    public void setPageOffset(Integer pageOffset) {
        this.pageOffset = pageOffset;
    }

    public Integer getPageLength() {
        return pageLength;
    }

    public void setPageLength(Integer pageLength) {
        this.pageLength = pageLength;
    }
}
