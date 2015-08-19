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

    private static String getStringValue(Map<String, String[]> paramters, String valueName)
    {
        String[] array = paramters.get(valueName);
        if(array == null)
            return null;

        return array[0];
    }

    private static Boolean getBooleanValue(Map<String, String[]> paramters, String valueName)
    {
        String[] array = paramters.get(valueName);
        if(array == null)
            return null;

        return Boolean.parseBoolean(array[0]);
    }

    private static Integer getIntegerValue(Map<String, String[]> paramters, String valueName)
    {
        String[] array = paramters.get(valueName);
        if(array == null)
            return null;

        return Integer.parseInt(array[0]);
    }

    public static JQueryDataTable analyzeRequest(Map<String, String[]> paramters)
    {
        JQueryDataTable jQueryDataTable = new JQueryDataTable();

        Integer draw = getIntegerValue(paramters, "draw");

        jQueryDataTable.setDraw(draw);

        Integer displayStart = getIntegerValue(paramters, "start");
        Integer displayLength = getIntegerValue(paramters, "length");

        jQueryDataTable.setPageOffset(displayStart);
        jQueryDataTable.setPageLength(displayLength);

        int columnsTotal = 0;

        for(int index=0;index<20;index++)
        {
            String columnName = getStringValue(paramters, "columns["+index+"][name]");

            /** No more columns */
            if(columnName == null)
                break;

            columnsTotal++;

            JQueryDataTableColumn column = new JQueryDataTableColumn();
            column.setPropertyName(columnName);

            Boolean columnSortale = getBooleanValue(paramters, "columns["+index+"][orderable]");
            column.setSortable(columnSortale);

            column.setIndex(index);

            jQueryDataTable.getColumns().add(column);
        }


        for(int index=0; index<columnsTotal; index++)
        {
            Integer orderColumn = getIntegerValue(paramters, "order["+index+"][column]");
            /** No more order */
            if(orderColumn == null)
                break;


            String orderDir = getStringValue(paramters, "order["+index+"][dir]");

            JQueryDataTableColumn column = jQueryDataTable.getColumnAt(orderColumn);
            column.setSorted(true);
            column.setSortedAsc(orderDir.equals("asc") ? true : false);
            column.setPriority(index);

            jQueryDataTable.getSortingColumnOrder().add(column);


        }




        return jQueryDataTable;
    }
}
