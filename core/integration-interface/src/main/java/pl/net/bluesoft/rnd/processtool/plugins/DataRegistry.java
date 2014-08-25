package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.auditlog.AuditLogHandler;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.expressions.ExpressionEvaluators;
import pl.net.bluesoft.rnd.processtool.model.IAttribute;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import java.sql.Connection;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 21:46
 */
public interface DataRegistry {
	void addClassLoader(String name, ClassLoader loader);
	void removeClassLoader(String name);
	ClassLoader getModelAwareClassLoader(ClassLoader parent);

	boolean registerModelExtension(Class<?>... cls);
	boolean unregisterModelExtension(Class<?>... cls);
	void commitModelExtensions();

    TransactionAwareDataSourceProxy getDataSourceProxy();

	void addHibernateResource(String name, byte[] resource);
	void removeHibernateResource(String name);

    Dialect getHibernateDialect();

	boolean isJta();

	SessionFactory getSessionFactory();

	ProcessToolContextFactory getProcessToolContextFactory();

	ProcessDictionaryDAO getProcessDictionaryDAO(Session hibernateSession);
	ProcessInstanceDAO getProcessInstanceDAO(Session hibernateSession);
	UserSubstitutionDAO getUserSubstitutionDAO(Session hibernateSession);
	ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO(Session hibernateSession);
	ProcessDefinitionDAO getProcessDefinitionDAO(Session hibernateSession);

    void registerAttributesMapper(Class<? extends IAttributesMapper> mapperClass);
    void unregisterAttributesMapper(Class<? extends IAttributesMapper> mapperClass);
    List<Class<? extends IAttributesMapper>> getAttributesMappers();
    List<IAttributesMapper> getAttributesMappersFor(Class<? extends IAttribute> clazz);

    void registerMapper(Class<? extends IMapper> mapperClass);
    void unregisterMapper(Class<? extends IMapper> mapperClass);
    List<IMapper> getMappersFor(Class<? extends IAttributesProvider> clazz, String definitionName);

	void addAuditLogHandler(AuditLogHandler handler);
	void removeAuditLogHandler(AuditLogHandler handler);
	List<AuditLogHandler> getAuditLogHandlers();

	ExpressionEvaluators getExpressionEvaluators();


}
