package pl.net.bluesoft.rnd.processtool.hibernate;

import org.hibernate.Session;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface HibernateTransactionCallback<T> {

	T doInTransaction(Session s);
	
}
