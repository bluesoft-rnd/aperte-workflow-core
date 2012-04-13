package org.aperteworkflow.ext.activiti.mybatis;

import org.activiti.engine.impl.TaskQueryImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TaskQueryImplEnhanced extends TaskQueryImpl {
    private Set<String> creators = new HashSet<String>();
    private Set<String> owners = new HashSet<String>();
    private Set<String> groups = new HashSet<String>();
    private Set<String> notOwners = new HashSet<String>();
    private Set<String> taskNames = new HashSet<String>();

    public Set<String> getGroups() {
        return groups;
    }

    public Set<String> getNotOwners() {
        return notOwners;
    }

    public Set<String> getOwners() {
        return owners;
    }

    public Set<String> getCreators() {
        return owners;
    }

    public Set<String> getTaskNames() {
        return taskNames;
    }

    public TaskQueryImplEnhanced addTaskName(String name) {
        taskNames.add(name);
        return this;
    }
    public TaskQueryImplEnhanced addOwner(String login) {
        owners.add(login);
        return this;
    }

    public TaskQueryImplEnhanced addGroup(String name) {
        groups.add(name);
        return this;
    }

    public TaskQueryImplEnhanced addNotOwner(String login) {
        notOwners.add(login);
        return this;
    }

    public TaskQueryImplEnhanced addCreator(String login) {
        creators.add(login);
        return this;
    }
}
