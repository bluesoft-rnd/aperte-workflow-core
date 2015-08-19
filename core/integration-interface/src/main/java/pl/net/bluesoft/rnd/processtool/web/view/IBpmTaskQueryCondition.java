package pl.net.bluesoft.rnd.processtool.web.view;

import java.util.List;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IBpmTaskQueryCondition
{
    String getSortJoinCondition(String sortColumnName);

    String getJoin();

    String getSortQuery(String columnName);

    String getSearchCondition();

    String getWhereCondition();
}
