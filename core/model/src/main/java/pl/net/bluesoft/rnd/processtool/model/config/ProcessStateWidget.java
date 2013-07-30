package pl.net.bluesoft.rnd.processtool.model.config;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_state_widget")
public class ProcessStateWidget extends PersistentEntity 
{
	private static final long serialVersionUID = 8363229421636212280L;

	@ManyToOne
	@JoinColumn(name="state_id")
	private ProcessStateConfiguration config;

	@ManyToOne
	@JoinColumn(name="parent_id")
	private ProcessStateWidget parent;

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="parent_id")
	private Set<ProcessStateWidget> children = new HashSet<ProcessStateWidget>();
		
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="widget_id")
	private Set<ProcessStateWidgetPermission> permissions = new HashSet<ProcessStateWidgetPermission>();

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="widget_id")
	private Set<ProcessStateWidgetAttribute> attributes = new HashSet<ProcessStateWidgetAttribute>();

	private String name;
	private String className;

    private Boolean optional;
	private Integer priority = 0;

    private String generateFromCollection;

	public String getName() {
		return nvl(name, className);
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProcessStateConfiguration getConfig() {
		return config;
	}

	public void setConfig(ProcessStateConfiguration config) {
		this.config = config;
	}

	public ProcessStateWidget getParent() {
		return parent;
	}

	public void setParent(ProcessStateWidget parent) {
		this.parent = parent;
	}

	public Set<ProcessStateWidget> getChildren() 
	{
        if (children == null) {
			children = new HashSet<ProcessStateWidget>();
		}
        
		return children;
	}

	public void setChildren(Set<ProcessStateWidget> children) 
	{
		this.children = children;
	}

	public Set<ProcessStateWidgetPermission> getPermissions() 
	{
        if (permissions == null) {
			permissions = new HashSet<ProcessStateWidgetPermission>();
		}
        
		return permissions;
	}

	public void setPermissions(Set<ProcessStateWidgetPermission> permissions) 
	{
		this.permissions = permissions;
	}

	public Set<ProcessStateWidgetAttribute> getAttributes() 
	{
        if (attributes == null) {
			attributes = new HashSet<ProcessStateWidgetAttribute>();
		}
        
		return attributes;
	}

	public void setAttributes(Set<ProcessStateWidgetAttribute> attributes) 
	{
		this.attributes = attributes;
	}
	
	public ProcessStateWidgetAttribute getAttributeByName(String attributeName)
	{
		for(ProcessStateWidgetAttribute attribute: this.attributes)
			if(attribute.getName().equals(attributeName))
				return attribute;
		
		return null;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Boolean getOptional() {
		return optional;
	}

	public void setOptional(Boolean optional) {
		this.optional = optional;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getGenerateFromCollection() {
		return generateFromCollection;
	}

	public void setGenerateFromCollection(String generateFromCollection) {
		this.generateFromCollection = generateFromCollection;
	}
}
