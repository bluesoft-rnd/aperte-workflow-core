package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.Date;
import java.util.List;
import static org.hibernate.criterion.Restrictions.*;

/**
 * User: POlszewski
 * Date: 2011-08-31
 * Time: 10:18:37
 */
public class UserSubstitutionDAOImpl extends SimpleHibernateBean<UserSubstitution> implements UserSubstitutionDAO {
    public UserSubstitutionDAOImpl(Session hibernateSession) {
        super(hibernateSession);
    }

    @Override
    public List<UserSubstitution> getActiveSubstitutions(UserData user, Date date) {
        return session.createQuery("from UserSubstitution where userSubstitute = :user and :date between dateFrom and dateTo")
            .setParameter("user", user)
            .setParameter("date", date)
            .list();                       
//        return session.createCriteria(UserSubstitution.class)
//                .add(eq("userSubstitute", user))
//                .add(ge("dateFrom", date))
//                .add(le("dateTo", date))
//                .list();
    }
}
