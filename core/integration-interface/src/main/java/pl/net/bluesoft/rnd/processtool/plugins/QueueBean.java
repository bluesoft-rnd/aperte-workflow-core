package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-03-08.
 */
public class QueueBean
{
    private String queueName;
    private Set<String> roles = new HashSet<String>();

    public String getQueueName() {
        return queueName;
    }

    public QueueBean setQueueName(String queueName) {
        this.queueName = queueName;

        return this;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public QueueBean addRole(String role) {
        this.roles.add(role);

        return this;
    }

    public boolean isRolePrivilegeToQueue(String roleName)
    {
        return roles.contains(roleName);
    }

    public boolean hasUserRightToQueue(UserData userData)
    {
        for(String role: roles)
            if(userData.hasRole(role))
                return true;

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueueBean queueBean = (QueueBean) o;

        if (!queueName.equals(queueBean.queueName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return queueName.hashCode();
    }
}
