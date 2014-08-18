package pl.net.bluesoft.rnd.processtool.web.view;

/**
 * @author: Maciej
 */
public interface IBpmTaskQueryCondition
{
    String getJoinCondition(String sortColumnName);

    String getSortQuery(String columnName);
}
