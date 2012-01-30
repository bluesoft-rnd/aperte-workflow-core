package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.ProcessInstanceSearchData;
import org.aperteworkflow.search.SearchProvider;
import org.aperteworkflow.search.Searchable;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStatePermission;

import java.util.*;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;
import static pl.net.bluesoft.util.lang.FormatUtil.formatShortDate;
import static pl.net.bluesoft.util.lang.FormatUtil.join;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceDAOImpl extends SimpleHibernateBean<ProcessInstance> implements ProcessInstanceDAO {
    private SearchProvider searchProvider;

    public ProcessInstanceDAOImpl(Session session, SearchProvider searchProvider) {
		super(session);
        this.searchProvider = searchProvider;
	}

	public long saveProcessInstance(ProcessInstance processInstance) {
		UserData creator = processInstance.getCreator();
		if (creator != null) {
			if (creator.getId() != null) {
				processInstance.setCreator((UserData) session.get(UserData.class, creator.getId()));
			} else {
				List users = session.createCriteria(UserData.class)
						.add(eq("login", creator.getLogin())).list();
				if (users.isEmpty()) {
					session.saveOrUpdate(creator);
				} else {
					processInstance.setCreator((UserData) users.get(0));
				}
			}
		}
        if (processInstance.getToDelete() != null) {
            for (Object o : processInstance.getToDelete()) {
                session.delete(o);
            }
        }
		session.saveOrUpdate(processInstance);
        long time = System.currentTimeMillis();
        //update search indexes
        ProcessInstanceSearchData searchData = new ProcessInstanceSearchData(processInstance.getId());
        //put some default search attributes
        if (creator != null) {
            searchData.addSearchAttribute(new ProcessInstanceSearchAttribute("creator_login", creator.getLogin()));
            searchData.addSearchAttribute(new ProcessInstanceSearchAttribute("creator_email", creator.getEmail()));
            searchData.addSearchAttribute(new ProcessInstanceSearchAttribute("creator_realname", creator.getRealName()));
        }
        searchData.addSearchAttributes(new String[][]{
                {"instance_key", processInstance.getExternalKey()},
                {"definition_name", processInstance.getDefinitionName()},
                {"instance_description", processInstance.getDescription()},
                {"instance_internal_id", processInstance.getInternalId()},
                {"instance_keyword", processInstance.getKeyword()},
                {"instance_state", processInstance.getState()},//TODO remember about multiple states (when BpmTask is merged)
                {"instance_create_date", formatShortDate(processInstance.getCreateDate())},
        });
        ProcessDefinitionConfig def = processInstance.getDefinition();
        searchData.addSearchAttributes(new String[][]{
                {"definition_key", def.getBpmDefinitionKey()},
                {"definition_description", def.getDescription()},
                {"definition_comment", def.getComment()},
                {"definition_processname", def.getProcessName()},
        });
        for (ProcessDefinitionPermission perm : def.getPermissions()) {
            if ("SEARCH".equals(perm.getPrivilegeName())) {
                String roleName = perm.getRoleName();
                if (roleName.equals(".*"))
                    roleName = "__AWF__ROLE_ALL";
                roleName = roleName.replace(' ', '_');
                searchData.addSearchAttribute("__AWF__ROLE", roleName, true);
            }
        }
        //lookup process state configuration
        ProcessStateConfiguration psc
                = new ProcessDefinitionDAOImpl(session).getProcessStateConfiguration(processInstance);
        if (psc != null) {
            searchData.addSearchAttributes(new String[][]{
                            {"state_commentary", psc.getCommentary()},
                            {"state_description", psc.getDescription()},
                            {"state_name", psc.getName()},
                    });
            for (ProcessStatePermission perm : psc.getPermissions()) {
                if ("SEARCH".equals(perm.getPrivilegeName())) {
                    String roleName = perm.getRoleName();
                    if (roleName.equals(".*"))
                        roleName = "__AWF__ROLE_ALL";
                    roleName = roleName.replace(' ', '_');
                    searchData.addSearchAttribute("__AWF__ROLE", roleName, true);
                }
            }
        }
        for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
            if (attr instanceof Searchable) {
                Collection<ProcessInstanceSearchAttribute> attributes = ((Searchable) attr).getAttributes();
                for (ProcessInstanceSearchAttribute pisa : attributes) {
                    if (pisa.getName().startsWith("__AWF__")) { //no cheating please!
                        String newName = pisa.getName().replace("__AWF__", "");
                        logger.severe("Renaming process provided attribute " + pisa.getName() + " to " + newName +
                                " as it may clash with internal search attributes. PLEASE CORRECT PROCESS DEFINITION.");
                        pisa.setName(newName);
                    }
                }
                searchData.addSearchAttributes(attributes);
            }
        }
        for (String assignee : processInstance.getAssignees()) {
            searchData.addSearchAttribute("__AWF__assignee", assignee, true);
            logger.info("__AWF__assignee: "+ assignee);
        }
        for (String queue : processInstance.getTaskQueues()) {
            searchData.addSearchAttribute("__AWF__queue", queue, true);
            logger.info("__AWF__queue: "+ queue);
        }
        searchData.addSearchAttribute("__AWF__running", String.valueOf(processInstance.getRunning()), true);

        logger.warning("Prepare data for Lucene index update for" + processInstance + " took "
                + (System.currentTimeMillis()-time) + " ms");
        time = System.currentTimeMillis();
        searchProvider.updateIndex(searchData);
        logger.warning("Lucene index update for " + processInstance + " (" + searchData.getSearchAttributes().size()
                + "attributes)  took " + (System.currentTimeMillis()-time) + " ms");
		return processInstance.getId();
	}

	public ProcessInstance getProcessInstance(long id) {
		return (ProcessInstance) session.get(ProcessInstance.class, id);
	}

	@Override
	public ProcessInstance getProcessInstanceByInternalId(String internalIds) {

		List list = session.createCriteria(ProcessInstance.class)
						.add(eq("internalId", internalIds)).list();
		if (list.isEmpty())
			return null;
		else
			return (ProcessInstance) list.get(0);
	}

	@Override
	public ProcessInstance getProcessInstanceByExternalId(String externalId) {
		List list = session.createCriteria(ProcessInstance.class)
						.add(eq("externalKey", externalId)).list();
		if (list.isEmpty())
			return null;
		else
			return (ProcessInstance) list.get(0);
	}

	@Override
	public List<ProcessInstance> findProcessInstancesByKeyword(String keyword, String processType) {
		return session.createCriteria(ProcessInstance.class)
						.add(eq("keyword", keyword))
//						.add(eq("definition.bpmDefinitionKey", processType))
						.addOrder(Order.desc("id"))						
						.list();

	}

	@Override
	public Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(List<String> internalId) {
		if (internalId.isEmpty()) return new HashMap();
		List<ProcessInstance> list = session.createCriteria(ProcessInstance.class)
						.add(in("internalId", internalId)).list();
		Map<String,ProcessInstance> res = new HashMap();
		for (ProcessInstance pi : list) {
			res.put(pi.getInternalId(), pi);
		}
		return res;
	}

	public List<ProcessInstance> getProcessInstancesByIds(List<Long> ids) {
		if (ids.isEmpty()) return new ArrayList<ProcessInstance>();
		List<ProcessInstance> list = session.createCriteria(ProcessInstance.class)
						.add(in("id", ids)).list();
		return list;
	}

	public void deleteProcessInstance(ProcessInstance instance) {
		session.delete(instance);
	}

	@Override
	public UserData findOrCreateUser(UserData ud) {
		List userList = session.createCriteria(UserData.class)
				.add(Restrictions.eq("login", ud.getLogin())).list();
		if (userList.isEmpty()) {
			//create new user
			session.saveOrUpdate(ud);
			userList = session.createCriteria(UserData.class).add(Restrictions.eq("login", ud.getLogin())).list();
		}
		return (UserData) userList.get(0);


	}

	@Override
	public List<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate, 
                                                    String filter, int offset, int limit) {
		List<Long> list = session.createCriteria(ProcessInstance.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.setProjection(Projections.distinct(Projections.property("id")))
				.addOrder(Order.desc("id"))
				.createCriteria("processLogs")
				.add(Restrictions.gt("entryDate", minDate))
				.createAlias("user", "u")
                .createAlias("userSubstitute", "us", CriteriaSpecification.LEFT_JOIN)
				.add(Restrictions.or(
                        Restrictions.eq("u.id", userData.getId()),
                        Restrictions.eq("us.id", userData.getId())))
				.setMaxResults(100).list();
        if (filter != null && !filter.trim().isEmpty()) {
            String query = "+__AWF__ID:(" + join(list, " ")+") +(" + filter + ")";
            return new ArrayList<ProcessInstance>(searchProcesses(query, offset, limit, false, null, null));
        } else {
		    return getProcessInstancesByIds(list);
        }
	}

    @Override
    public Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit, boolean onlyRunning, String[] userRoles, String assignee, String... queues) {
        List<Long> processIds = searchProvider.searchProcesses(filter, offset, limit, onlyRunning, userRoles, assignee, queues);
        List<ProcessInstance> processInstancesByIds = getProcessInstancesByIds(processIds);
        Collections.sort(processInstancesByIds, new Comparator<ProcessInstance>() {
            @Override
            public int compare(ProcessInstance o1, ProcessInstance o2) {
                return o2.getId().compareTo(o1.getId());
            }
        });
        return processInstancesByIds;
    }
}
