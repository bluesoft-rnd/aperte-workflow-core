package org.aperteworkflow.search;

import java.util.List;

/**
 * This interface is placed here because of dependency on apache Lucene (and KISS principle).
 *
 * @author tlipski@bluesoft.net.pl
 */
public interface SearchProvider {

    void updateIndex(ProcessInstanceSearchData processInstanceSearchData);
    List<Long> searchProcesses(String query, int offset, int limit, boolean onlyRunning, 
                               String[] userRoles,
                               String assignee, String... queues);
}
