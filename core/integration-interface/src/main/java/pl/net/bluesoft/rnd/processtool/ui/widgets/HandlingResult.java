package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Date;

public class HandlingResult {

    private Date dateOfChange;
    private String key;
    private String oldValue;
    private String newValue;

    public HandlingResult() {}

    public HandlingResult(Date dateOfChange, String key, String oldValue, String newValue) {
        this.dateOfChange = dateOfChange;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Date getDateOfChange() {
        return dateOfChange;
    }

    public void setDateOfChange(Date dateOfChange) {
        this.dateOfChange = dateOfChange;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

}
