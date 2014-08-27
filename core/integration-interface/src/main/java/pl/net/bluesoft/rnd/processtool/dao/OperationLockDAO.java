package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.OperationLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.Date;
import java.util.List;

/**
 * User: mpawlak@bluesoft.net.pl
 */
public interface OperationLockDAO
{
    void createLock(OperationLock lock);
    OperationLock getLock(String operationName);
    void removeLock(OperationLock operationLock);
}
