package org.aperteworkflow.search;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceSearchAttribute {
    
    private String name, value;
    private boolean keyword=false;

    public ProcessInstanceSearchAttribute() {
    }

    public ProcessInstanceSearchAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ProcessInstanceSearchAttribute(String name, String value, boolean keyword) {
        this.name = name;
        this.value = value;
        this.keyword = keyword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public void setKeyword(boolean keyword) {
        this.keyword = keyword;
    }
}
