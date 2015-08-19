package pl.net.bluesoft.rnd.processtool.model;

/**
 * @author mpawlak
 */
public enum QueueOrderCondition implements IBpmTaskOrderColumnName {
    SORT_BY_DATE_ORDER,
    SORT_BY_CREATE_DATE_ORDER,
    SORT_BY_PROCESS_CODE_ORDER,
    SORT_BY_PROCESS_STEP_ORDER,
    SORT_BY_PROCESS_NAME_ORDER,
    SORT_BY_ASSIGNEE_ORDER,
    SORT_BY_CREATOR_ORDER,
    SORT_BY_PROCESS_BUSINESS_STATUS_ORDER,
    SORT_BY_STEP_INFO
}
