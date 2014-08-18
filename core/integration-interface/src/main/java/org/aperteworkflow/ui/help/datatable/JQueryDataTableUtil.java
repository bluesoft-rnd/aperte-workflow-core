package org.aperteworkflow.ui.help.datatable;

import org.apache.commons.beanutils.PropertyUtils;

import java.util.Comparator;
import java.util.Map;

/**
 * Utility support class for jQuery Datatables
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class JQueryDataTableUtil
{
    public static <T> Comparator<T> getComparator(JQueryDataTable jQueryDataTable)
    {
       final JQueryDataTableColumn sortingColumn = jQueryDataTable.getFirstSortingColumn();
       return new Comparator<T>()
       {
           @Override
           public int compare(T o1, T o2)
           {

               try
               {
                   Comparable propertyValue1 = (Comparable) PropertyUtils.getProperty(o1, sortingColumn.getPropertyName());
                   Comparable propertyValue2 = (Comparable)PropertyUtils.getProperty(o2, sortingColumn.getPropertyName());

                   if(sortingColumn.getSortedAsc())
                     return propertyValue1.compareTo(propertyValue2);
                   else
                       return propertyValue2.compareTo(propertyValue1);
               }
               catch (Throwable e)
               {
                   return 0;
               }

           }
       };
    }

    public static JQueryDataTable analyzeRequest(Map<String, String[]> paramters)
    {
        JQueryDataTable jQueryDataTable = new JQueryDataTable();

        String echo = paramters.get("sEcho")[0];
        jQueryDataTable.setEcho(echo);

        String displayStartString = paramters.get("iDisplayStart")[0];
        String displayLengthString = paramters.get("iDisplayLength")[0];

        Integer displayStart = Integer.parseInt(displayStartString);
        Integer displayLength = Integer.parseInt(displayLengthString);
        jQueryDataTable.setPageOffset(displayStart);
        jQueryDataTable.setPageLength(displayLength);

        String sColumns = paramters.get("sColumns")[0];
        String iSortingColumns = paramters.get("iSortingCols")[0];

        String[] columnNames = sColumns.split(",");

        Integer sortingColumnsCount = Integer.parseInt(iSortingColumns);

        for(int index=0; index<columnNames.length; index++)
        {
            JQueryDataTableColumn column = new JQueryDataTableColumn();
            column.setPropertyName(columnNames[index]);
            column.setIndex(index);

            String isSortableString = paramters.get("bSortable_"+index)[0];
            column.setSortable(Boolean.parseBoolean(isSortableString));

            jQueryDataTable.getColumns().add(column);
        }


        for(int index=0; index<sortingColumnsCount; index++)
        {
            String sortingColumnIndexString = paramters.get("iSortCol_"+index)[0];
            String sortingColumnOrderString = paramters.get("sSortDir_"+index)[0];

            Integer sortingColumnIndex = Integer.parseInt(sortingColumnIndexString);

            JQueryDataTableColumn column = jQueryDataTable.getColumnAt(sortingColumnIndex);
            column.setSorted(true);
            column.setSortedAsc(sortingColumnOrderString.equals("asc") ? true : false);
            column.setPriority(index);

            jQueryDataTable.getSortingColumnOrder().add(column);


        }




        return jQueryDataTable;
    }
}
