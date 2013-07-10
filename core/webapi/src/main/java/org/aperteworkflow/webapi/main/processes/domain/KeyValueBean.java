package org.aperteworkflow.webapi.main.processes.domain;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class KeyValueBean
{
    private String key;
    private String value;

    public KeyValueBean()
    {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
