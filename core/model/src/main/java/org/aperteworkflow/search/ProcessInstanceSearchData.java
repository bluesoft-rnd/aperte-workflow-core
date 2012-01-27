package org.aperteworkflow.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceSearchData {
    
    private long processInstanceId;

    private List<ProcessInstanceSearchAttribute> searchAttributes = new ArrayList();

    public ProcessInstanceSearchData() {
    }

    public ProcessInstanceSearchData(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public List<ProcessInstanceSearchAttribute> getSearchAttributes() {
        if (searchAttributes == null)
            searchAttributes = new ArrayList<ProcessInstanceSearchAttribute>();
        return searchAttributes;
    }

    public void addSearchAttributes(String[][] attrs) {
        for (String[] attr : attrs) {
            addSearchAttribute(new ProcessInstanceSearchAttribute(attr[0], attr[1]));
        }
    }
    public void addSearchAttribute(String name, String value) {
        addSearchAttribute(new ProcessInstanceSearchAttribute(name, value));
    }
    public void addSearchAttribute(String name, String value, boolean keyword) {
        addSearchAttribute(new ProcessInstanceSearchAttribute(name, value, keyword));
    }
    public void addSearchAttribute(ProcessInstanceSearchAttribute... searchAttributes) {
        Collections.addAll(this.searchAttributes, searchAttributes);
    }
    public void addSearchAttributes(Collection<ProcessInstanceSearchAttribute> searchAttributes) {
        this.searchAttributes.addAll(searchAttributes);
    }

    public void setSearchAttributes(List<ProcessInstanceSearchAttribute> searchAttributes) {
        this.searchAttributes = searchAttributes;
    }
}
