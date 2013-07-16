package org.aperteworkflow.webapi.main.processes.widget.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Widget bean representation
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WidgetBean implements Serializable
{
	private static final long serialVersionUID = 3399177937221548724L;
	
	private String id;
	private String name;
	private String className;
	private String caption;
	
	private List<WidgetBean> children = new ArrayList<WidgetBean>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public List<WidgetBean> getChildren() {
		return children;
	}
	public void setChildren(List<WidgetBean> children) {
		this.children = children;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	

}
