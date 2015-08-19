package pl.net.bluesoft.rnd.processtool.web.view;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.web.view.IBpmTaskQueryCondition;

import java.util.Collection;
import java.util.List;

/**
 * @author: mpawlak
 */
public class BpmTaskQueryCondition implements IBpmTaskQueryCondition
{
    private static final String PROCESS_NAME_COLUMN = "name";
    private static final String PROCESS_CODE_COLUMN = "code";
    private static final String PROCESS_STEP_COLUMN = "step";
    private static final String PROCESS_BUSINESS_STATUS_COLUMN = "businessStatus";
    private static final String CREATOR_NAME_COLUMN = "creator";
    private static final String ASSIGNEE_NAME_COLUMN = "assignee";
    private static final String CREATED_DATE_COLUMN = "creationDate";
    private static final String TASK_CREATION_DATE = "assignDate";

    @Override
    public String getSortJoinCondition(String sortColumnName) {
        return "";
    }

    @Override
    public String getJoin() {
        return "";
    }

    @Override
    public String getSortQuery(String columnName)
    {
        if(columnName.equals(TASK_CREATION_DATE))
                return "task_.createdOn";
        else if(columnName.equals(CREATED_DATE_COLUMN))
                return "process.createdate";
        else if(columnName.equals( PROCESS_CODE_COLUMN))
                return "CASE WHEN process.externalKey is not null THEN process.externalKey ELSE process.internalid END";
        else if(columnName.equals( PROCESS_STEP_COLUMN))
                return "i18ntext_.shortText";
        else if(columnName.equals( PROCESS_NAME_COLUMN))
                return "process.definitionname";
        else if(columnName.equals( ASSIGNEE_NAME_COLUMN))
                return "task_.actualowner_id";
        else if(columnName.equals( CREATOR_NAME_COLUMN))
                return "process.creatorLogin";
        else if(columnName.equals( PROCESS_BUSINESS_STATUS_COLUMN))
                return "process.business_status";
        else
            return "";
    }

    @Override
    public String getSearchCondition()
    {
        return "";
    }

    @Override
    public String getWhereCondition() {
        return "";
    }

    protected String getSAttrsSearchCondition(List<String> searchSAttrs)
    {
        Collection<String> enclosedSAttrs = Collections2.transform(searchSAttrs, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return "'" + s + "'";
            }
        });

        String joinedSAttrs = StringUtils.join(enclosedSAttrs, ", ");

        return "(select count(ppisa.key_) from pt_process_instance_s_attr ppisa where ppisa.process_instance_id=process.id and ppisa.key_ in("+joinedSAttrs+") and lower(ppisa.value_) like '%' || lower(:expression) || '%') > 0";
    }
}
