package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_state_widget")
public class ProcessStateWidget extends PersistentEntity {
//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="state_id")
	private ProcessStateConfiguration config;

//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="parent_id")
	private ProcessStateWidget parent;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="parent_id")
	private Set<ProcessStateWidget> children = new HashSet();
		
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="widget_id")
	private Set<ProcessStateWidgetPermission> permissions = new HashSet();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="widget_id")
	private Set<ProcessStateWidgetAttribute> attributes = new HashSet();

	private String name;
	private String className;

    private Boolean optional;
	private Integer priority = Integer.valueOf(0);

    private String generateFromCollection;
	@Transient
	private String generatorKey;

    public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

    @XmlTransient
	public ProcessStateWidget getParent() {
		return parent;
	}

//    @XmlTransient
	public void setParent(ProcessStateWidget parent) {
		this.parent = parent;
	}

	public Set<ProcessStateWidget> getChildren() {
        if (children == null) children = new HashSet<ProcessStateWidget>();
		return children;
	}

	public void setChildren(Set<ProcessStateWidget> children) {
		this.children = children;
	}

	public Set<ProcessStateWidgetAttribute> getAttributes() {
        if (attributes == null) attributes = new HashSet<ProcessStateWidgetAttribute>();
		return attributes;
	}

	public void setAttributes(Set<ProcessStateWidgetAttribute> attributes) {
		this.attributes = attributes;
	}

    @XmlTransient
	public ProcessStateConfiguration getConfig() {
		return config;
	}

//    @XmlTransient
	public void setConfig(ProcessStateConfiguration config) {
		this.config = config;
	}

	public String getName() {
		return nvl(name, className);
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<ProcessStateWidgetPermission> getPermissions() {
        if (permissions == null) permissions = new HashSet<ProcessStateWidgetPermission>();
		return permissions;
	}

	public void setPermissions(Set<ProcessStateWidgetPermission> permissions) {
		this.permissions = permissions;
	}

	public Integer getPriority() {
		if (priority == null) priority = Integer.valueOf(0);
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public String getGeneratorKey() {
		return generatorKey;
	}

	public void setGeneratorKey(String generatorKey) {
		this.generatorKey = generatorKey;
	}

	public String getGenerateFromCollection() {
		return generateFromCollection;
	}

	public void setGenerateFromCollection(String generateFromCollection) {
		this.generateFromCollection = generateFromCollection;
    }
}
