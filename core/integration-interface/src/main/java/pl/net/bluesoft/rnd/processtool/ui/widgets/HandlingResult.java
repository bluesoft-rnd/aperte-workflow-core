package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HandlingResult {
    private Date dateOfChange;
    private String key;
	private boolean singleRow;
    private String oldValue;
    private String newValue;
	private Map<String, String> attributes = new HashMap<String, String>();

    public HandlingResult() {}

    public HandlingResult(Date dateOfChange, String key, boolean singleRow, String oldValue, String newValue) {
        this.dateOfChange = dateOfChange;
        this.key = key;
		this.singleRow = singleRow;
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

	public boolean isSingleRow() {
		return singleRow;
	}

	public void setSingleRow(boolean singleRow) {
		this.singleRow = singleRow;
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

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
}
