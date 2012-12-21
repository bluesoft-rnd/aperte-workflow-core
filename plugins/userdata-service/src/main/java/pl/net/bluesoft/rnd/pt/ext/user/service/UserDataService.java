package pl.net.bluesoft.rnd.pt.ext.user.service;

import org.hibernate.FetchMode;
import org.hibernate.criterion.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.CriteriaConfigurer;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.service.ProcessToolUserService;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserDataService implements ProcessToolUserService {
    protected ProcessToolRegistry registry;

    public UserDataService(ProcessToolRegistry registry) {
        this.registry = registry;
    }

    protected <T> T withContext(ReturningProcessToolContextCallback<T> callback) {
        return registry.withExistingOrNewContext(callback);
    }

    protected <T> T withContext(ProcessToolContext ctx, ReturningProcessToolContextCallback<T> callback) {
        return ctx == null ? withContext(callback) : callback.processWithContext(ctx);
    }

    @Override
    public UserData findUserByLogin(final String login) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return ctx.getUserDataDAO().loadUserByLogin(login);
            }
        });
    }

    @Override
    public UserData findUserByEmail(final String email) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return ctx.getUserDataDAO().findUnique(Restrictions.eq("email", email));
            }
        });
    }

    @Override
    public List<UserData> findUsersByExample(final UserData userData) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = dao.getDetachedCriteria().add(Example.create(userData)).addOrder(Order.asc("login"));
                return ctx.getUserDataDAO().findByCriteria(criteria);
            }
        });
    }

    @Override
    public List<UserData> findAllUsers() {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                return ctx.getUserDataDAO().findAll();
            }
        });
    }

    @Override
    public UserData findUserById(final Long id) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return ctx.getUserDataDAO().findUnique(Restrictions.idEq(id));
            }
        });
    }

    @Override
    public List<UserData> findUsersByLogins(final Collection<String> logins) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = dao.getDetachedCriteria().add(Restrictions.in("login", logins))
                        .addOrder(Order.asc("login"));
                return dao.findByCriteria(criteria);
            }
        });
    }

    @Override
    public List<UserData> findUsersByEmails(final Collection<String> emails) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = dao.getDetachedCriteria().add(Restrictions.in("email", emails))
                        .addOrder(Order.asc("login"));
                return dao.findByCriteria(criteria);
            }
        });
    }

    @Override
    public List<UserData> findUsersByIds(final Collection<Long> ids) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = dao.getDetachedCriteria().add(Restrictions.in("id", ids))
                        .addOrder(Order.asc("login"));
                return dao.findByCriteria(criteria);
            }
        });
    }

    @Override
    public List<UserData> findUsersByAttribute(final String key, final String... values) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = dao.getDetachedCriteria()
                        .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                        .addOrder(Order.asc("login"))
                        .createCriteria("attributes")
                        .add(Restrictions.eq("key", key));
                if(values.length == 1)
                	criteria.add(Restrictions.eq("value", values[0]));
                else
                	criteria.add(Restrictions.in("value", values));
                return dao.findByCriteria(criteria);
            }
        });
    }

    @Override
    public List<UserData> findUsersContainingAttributes(final String... key) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = dao.getDetachedCriteria()
                        .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                        .addOrder(Order.asc("login"))
                        .createCriteria("attributes")
                        .add(Restrictions.in("key", key));
                return dao.findByCriteria(criteria);
            }
        });
    }

    @Override
    public List<UserData> findUsersByAttributes(final Map<String, String> attributeValues) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria criteria = configureAttributesJoinCriteria(dao.getDetachedCriteria(), attributeValues);
                return dao.findByCriteria(criteria);
            }
        });
    }

    protected DetachedCriteria configureAttributesJoinCriteria(DetachedCriteria root, Map<String, String> attributeValues) {
        root.addOrder(Order.asc("login"));
        root.setFetchMode("attributes", FetchMode.JOIN);
        root.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (attributeValues != null && !attributeValues.isEmpty()) {
            JoinAliasGenerator usr = new JoinAliasGenerator("usr"), ua = new JoinAliasGenerator("ua");
            DetachedCriteria user = DetachedCriteria.forClass(UserData.class, usr.next());
			for (Map.Entry<String, String> entry : attributeValues.entrySet()) {
                String attributeAlias = ua.next();
                user.createAlias(usr.last() + ".attributes", attributeAlias)
                        .add(Restrictions.eq(attributeAlias + ".key", entry.getKey()))
                        .add(Restrictions.eq(attributeAlias + ".value", entry.getValue()));
                user.createAlias(attributeAlias + ".user", usr.next());
            }
            user.setResultTransformer(DetachedCriteria.PROJECTION);
            user.setProjection(Projections.projectionList().add(Projections.id()));
            root.add(Property.forName("id").in(user));
        }
        return root;
    }

    @Override
    public List<UserData> findUsersByCriteria(final Criterion... criteria) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria detachedCriteria = dao.getDetachedCriteria();
                for (Criterion c : criteria) {
                    detachedCriteria.add(c);
                }
                return dao.findByCriteria(detachedCriteria);
            }
        });
    }

    @Override
    public List<UserData> findUsersByCriteria(Collection<Criterion> criteria) {
        return findUsersByCriteria(criteria.toArray(new Criterion[criteria.size()]));
    }

    @Override
    public List<UserData> findUsersByCriteria(final CriteriaConfigurer configurer) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                UserDataDAO dao = ctx.getUserDataDAO();
                DetachedCriteria detachedCriteria = dao.getDetachedCriteria();
                configurer.configure(detachedCriteria);
                return dao.findByCriteria(detachedCriteria);
            }
        });
    }

    @Override
    public UserData updateUser(final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return updateUserInternal(ctx, user);
            }
        });
    }

    protected UserData updateUserInternal(ProcessToolContext ctx, UserData user) {
        UserDataDAO dao = ctx.getUserDataDAO();
        UserData base = dao.loadOrCreateUserByLogin(user);
        UserData merged = UserConverterUtils.mergeUsers(base, user);
        dao.saveOrUpdate(merged);
        return merged;
    }

    @Override
    public List<UserData> updateUsers(final Collection<UserData> users) {
        return withContext(new ReturningProcessToolContextCallback<List<UserData>>() {
            @Override
            public List<UserData> processWithContext(ProcessToolContext ctx) {
                return updateUsersInternal(ctx, users);
            }
        });
    }

    protected List<UserData> updateUsersInternal(ProcessToolContext ctx, Collection<UserData> users) {
        List<UserData> mergedUsers = new LinkedList<UserData>();
        for (UserData user : users) {
            mergedUsers.add(updateUserInternal(ctx, user));
        }
        return mergedUsers;
    }
}
