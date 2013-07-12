package pl.net.bluesoft.rnd.processtool.model;

/**
 * @author mpawlak
 */
public enum QueueOrderCondition
{
    SORT_BY_DATE_ORDER(" order by task.start_ "),
    SORT_BY_CREATE_DATE_ORDER(" order by process.createdate "),
    SORT_BY_PROCESS_CODE_ORDER(" order by process.internalid "),
    SORT_BY_PROCESS_STEP_ORDER(" order by task.activity_name_ "),
    SORT_BY_PROCESS_NAME_ORDER(" order by process.definitionname "),
    SORT_BY_ASSIGNEE_ORDER(" order by htask.assignee_ "),
    SORT_BY_CREATOR_ORDER(" order by aperteuser.login ");
    private String query;
    private QueueOrderCondition(String query)
    {
        this.query = query;
    }

    public String getQuery()
    {
        return this.query;
    }
}
