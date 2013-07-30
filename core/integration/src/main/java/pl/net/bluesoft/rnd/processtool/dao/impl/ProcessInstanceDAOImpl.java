package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.ProcessInstanceSearchData;
import org.aperteworkflow.search.SearchProvider;
import org.aperteworkflow.search.Searchable;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.transform.NestedAliasToBeanResultTransformer;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStatePermission;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Transformer;

import java.util.*;

import static org.hibernate.criterion.Restrictions.*;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.DateUtil.addDays;
import static pl.net.bluesoft.util.lang.DateUtil.truncHours;
import static pl.net.bluesoft.util.lang.FormatUtil.formatShortDate;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;


/**
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */
public class ProcessInstanceDAOImpl extends SimpleHibernateBean<ProcessInstance> implements ProcessInstanceDAO {
    private SearchProvider searchProvider;

    public ProcessInstanceDAOImpl(Session session, SearchProvider searchProvider) {
		super(session);
        this.searchProvider = searchProvider;
	}

	@Override
	public long saveProcessInstance(ProcessInstance processInstance) {
        if (processInstance.getToDelete() != null) {
            for (Object o : processInstance.getToDelete()) {
                session.delete(o);
            }
        }
        Set<ProcessInstanceAttribute> procAttrib = processInstance.getProcessAttributes();
        for (ProcessInstanceAttribute attrib:procAttrib){
        	if (attrib instanceof ProcessInstanceAttachmentAttribute && attrib.getId()!=null){
        		session.evict(session.get(attrib.getClass(), attrib.getId()));
        	}
        }
		session.saveOrUpdate(processInstance);
        session.flush();
        long time = System.currentTimeMillis();
        //update search indexes
        ProcessInstanceSearchData searchData = new ProcessInstanceSearchData(processInstance.getId());
        //put some default search attributes

		UserData creator = getRegistry().getUserSource().getUserByLogin(processInstance.getCreatorLogin());

        if (creator != null) {
            searchData.addSearchAttribute(new ProcessInstanceSearchAttribute("creator_login", creator.getLogin()));
            searchData.addSearchAttribute(new ProcessInstanceSearchAttribute("creator_email", creator.getEmail()));
            searchData.addSearchAttribute(new ProcessInstanceSearchAttribute("creator_realname", creator.getRealName()));
        }
        searchData.addSearchAttributes(new String[][]{
                {"instance_key", processInstance.getExternalKey()},
                {"definition_name", processInstance.getDefinitionName()},
                {"instance_internal_id", processInstance.getInternalId()},
//                {"instance_state", processInstance.getState()},//TODO remember about multiple states (when BpmTask is merged)
                {"instance_create_date", formatShortDate(processInstance.getCreateDate())},
        });
        ProcessDefinitionConfig def = processInstance.getDefinition();
		searchData.addSearchAttributes(new String[][]{
                {"definition_key", def.getBpmDefinitionKey()},
                {"definition_description", def.getDescription()},
                {"definition_comment", def.getComment()},
                {"definition_processname", def.getDescription() },
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

        for (BpmTask t : nvl(processInstance.getActiveTasks(), new BpmTask[0])) {
            ProcessStateConfiguration psc = t.getCurrentProcessStateConfiguration();
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
        searchData.addSearchAttribute("__AWF__running", String.valueOf(processInstance.isProcessRunning()), true);

        logger.finest("Prepare data for Lucene index update for" + processInstance + " took "
                + (System.currentTimeMillis()-time) + " ms");
        time = System.currentTimeMillis();
        searchProvider.updateIndex(searchData);
        logger.finest("Lucene index update for " + processInstance + " (" + searchData.getSearchAttributes().size()
                + "attributes)  took " + (System.currentTimeMillis()-time) + " ms");
        
		return processInstance.getId();
	}

	@Override
	public ProcessInstance getProcessInstance(long id) {
		return (ProcessInstance) session.get(ProcessInstance.class, id);
	}

    @Override
    public List<ProcessInstance> getProcessInstances(Collection<Long> ids) {
        if (ids.isEmpty())
            return new ArrayList();
        return getSession().createCriteria(ProcessInstance.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.in("id", ids)).list();
    }

    @Override
    public ProcessInstance refreshProcessInstance(ProcessInstance processInstance) {
        return (ProcessInstance) getSession().merge(processInstance);
    }

    @Override
    public ProcessInstance getProcessInstanceByInternalId(String internalIds) {
    	ProcessInstance pi = (ProcessInstance) getSession().createCriteria(ProcessInstance.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(eq("internalId", internalIds))
                .uniqueResult();
         return pi;
    }

	@Override
	public ProcessInstance getProcessInstanceByExternalId(String externalId) {
		List list = session.createCriteria(ProcessInstance.class)
						.add(eq("externalKey", externalId))
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .list();
		if (list.isEmpty())
			return null;
		else
			return (ProcessInstance) list.get(0);
	}

	@Override            //          TODO
	public List<ProcessInstance> findProcessInstancesByKeyword(String keyword, String processType) {
		return session.createCriteria(ProcessInstance.class)
						.add(eq("keyword", keyword))
//						.add(eq("definition.bpmDefinitionKey", processType))
						.addOrder(Order.desc("id"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
						.list();

	}
	
	
 
	@Override
	public Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(Collection<String> internalId) {
        if (internalId.isEmpty()) {
             return new HashMap<String, ProcessInstance>();
         }
         List<ProcessInstance> list = getSession().createCriteria(ProcessInstance.class)
                 .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                 .add(in("internalId", internalId))
                 .list();
         return Collections.transform(list, new Transformer<ProcessInstance, String>() {
             @Override
             public String transform(ProcessInstance obj) {
                 return obj.getInternalId();
             }
         });
	}

	public List<ProcessInstance> getProcessInstancesByIds(List<Long> ids) {
		return getProcessInstances(ids);
	}
	

	@Override
	public void deleteProcessInstance(ProcessInstance instance) {
		long start = System.currentTimeMillis();
		session.delete(instance);
		long duration = System.currentTimeMillis() - start;
		logger.severe("deleteProcessInstanceByInternalId: " +  duration);
	}
	
	

    @Override
	public Collection<ProcessInstanceLog> getUserHistory(UserData user, Date startDate, Date endDate) {
    	
    	 long start = System.currentTimeMillis();
        Criteria criteria = session.createCriteria(ProcessInstanceLog.class)
                .addOrder(Order.desc("entryDate"));
        
           

        if (user != null) {
            criteria.add(or(eq("user", user), eq("userSubstitute", user)));
        }

        if (startDate != null) {
            criteria.add(ge("entryDate", truncHours(startDate)));
        }
        if (endDate != null) {
            criteria.add(le("entryDate", truncHours(addDays(endDate, 1))));
        }

        criteria.createAlias("state", "s", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("processInstance", "pi");
        criteria.createAlias("pi.definition", "def");
        criteria.createAlias("pi.creator", "crtr");
        criteria.createAlias("ownProcessInstance", "ownPi");
        criteria.createAlias("user", "u");
        criteria.createAlias("userSubstitute", "us", CriteriaSpecification.LEFT_JOIN);
 
        ProjectionList pl = Projections.projectionList()
                .add(Projections.id(), "id")
                .add(Projections.property("entryDate"), "entryDate")
                .add(Projections.property("eventI18NKey"), "eventI18NKey")
                .add(Projections.property("executionId"), "executionId")
                .add(Projections.property("additionalInfo"), "additionalInfo")
                .add(Projections.property("u.id"), "user.id")
                .add(Projections.property("u.login"), "user.login")
                .add(Projections.property("u.firstName"), "user.firstName")
                .add(Projections.property("u.lastName"), "user.lastName")
                .add(Projections.property("s.id"), "state.id")
                .add(Projections.property("s.name"), "state.name")
                .add(Projections.property("s.description"), "state.description")
                .add(Projections.property("us.id"), "userSubstitute.id")
                .add(Projections.property("us.firstName"), "userSubstitute.firstName")
                .add(Projections.property("us.lastName"), "userSubstitute.lastName")
                .add(Projections.property("pi.id"), "processInstance.id")
                .add(Projections.property("pi.internalId"), "processInstance.internalId")
                .add(Projections.property("pi.externalKey"), "processInstance.externalKey")
                .add(Projections.property("pi.status"), "processInstance.status")
                .add(Projections.property("pi.createDate"), "processInstance.createDate")
                .add(Projections.property("def.id"), "processInstance.definition.id")
                .add(Projections.property("def.description"), "processInstance.definition.description")
                .add(Projections.property("def.comment"), "processInstance.definition.comment")
                .add(Projections.property("crtr.firstName"), "processInstance.creator.firstName")
                .add(Projections.property("crtr.lastName"), "processInstance.creator.lastName")
                .add(Projections.property("ownPi.id"), "ownProcessInstance.id");

        criteria.setProjection(pl);

        criteria.setResultTransformer(new NestedAliasToBeanResultTransformer(ProcessInstanceLog.class));

        long duration = System.currentTimeMillis() - start;
		logger.severe("getUserHistory: " +  duration);
        
        return criteria.list();
    }
    
        
    @Override
    public Collection<ProcessInstance> getUserProcessesAfterDate(String userLogin, Date minDate) {
    	return getUserProcessesBetweenDates(userLogin, minDate, null);
    }
    
   
    @Override
    public Collection<ProcessInstance> getUserProcessesBetweenDates(String userLogin, Date minDate, Date maxDate) {
           
    	 long start = System.currentTimeMillis();
    	Session session = getSession(); 
 
            ProjectionList properties = Projections.projectionList();
            properties.add(Projections.property("id"));
            properties.add(Projections.property("internalId"));
           Criteria criteria = session.createCriteria(ProcessInstance.class)
                    .setProjection(Projections.distinct(properties))
                    .createCriteria("processLogs");
                    
                    if(minDate!=null){
                    	criteria.add(gt("entryDate", minDate));
                    }
                    
                    if(maxDate!=null){
                    	criteria.add(le("entryDate", maxDate));
                    }
           
                   criteria.createAlias("user", "u")
                    .add(eq("u.id", userLogin));

           List<Object[]> list = criteria.list();
             Collection<ProcessInstance> collect = Collections.collect(list, new Transformer<Object[], ProcessInstance>() {
                @Override
                public ProcessInstance transform(Object[] row) {
                    ProcessInstance pi = new ProcessInstance();
                    pi.setId((Long) row[0]);
                    pi.setInternalId((String) row[1]);
                    return pi;
                }
            });
            
             long duration = System.currentTimeMillis() - start;
 			logger.severe("getUserProcessesBetweenDates: " +  duration);
             return collect;
            
        }
    
    
    

        @Override
        public ResultsPageWrapper<ProcessInstance> getRecentProcesses(String userLogin, Date minDate, Integer offset, Integer limit) {
            Session session = getSession();
            List<ProcessInstance> instances = null;
            if (offset != null && limit != null) {
                List<Long> list = session.createCriteria(ProcessInstance.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .setProjection(Projections.distinct(Projections.property("id")))
                        .addOrder(Order.desc("id"))
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .createCriteria("processLogs")
                        .add(Restrictions.gt("entryDate", minDate))
                        .createAlias("user", "u")
                        .add(Restrictions.eq("u.id", userLogin))
                        .list();
                instances = getProcessInstancesByIds(list);
            }

            Number total = (Number) session.createCriteria(ProcessInstance.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .setProjection(Projections.countDistinct("id"))
                    .createCriteria("processLogs")
                    .add(Restrictions.gt("entryDate", minDate))
                    .createAlias("user", "u")
                    .add(Restrictions.eq("u.id", userLogin)).uniqueResult();
            
            ResultsPageWrapper<ProcessInstance> resultsPageWrapper = new ResultsPageWrapper<ProcessInstance>(instances != null ? instances : new ArrayList<ProcessInstance>(),
                    total == null ? 0 : total.intValue());

            return resultsPageWrapper;
        }

        @Override
        final public ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(final Collection<String> internalIds,
                                                                                                     final ProcessInstanceFilter filter, Integer offset, Integer limit) {
            if (internalIds.isEmpty()) {
                return new ResultsPageWrapper<ProcessInstance>();
            }
            Session session = getSession();

            DetachedCriteria detachedCriteriaForIds = buildhibernateQuery(internalIds, filter);

            Criteria criteria = detachedCriteriaForIds.getExecutableCriteria(session);
            criteria.setFetchMode("definition",FetchMode.SELECT);
            criteria.setFetchMode("creator",FetchMode.SELECT);
            criteria.setFetchMode("parent",FetchMode.SELECT);

            List<ProcessInstance> result = (List<ProcessInstance>)criteria.list();
            int resultsCount = result.size();

            List<ProcessInstance> list;
            /* If limit is zero or null, return results */
            if(limit == null || limit <= 0) 
            {
                list = new ArrayList<ProcessInstance>(result);
            }
            else 
            {
                criteria.setFirstResult(offset);
                criteria.setMaxResults(limit);
                criteria.addOrder(Order.desc("createDate"));
                criteria.setProjection(Projections.id());//null).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                List ids = criteria.list();

                if (ids != null && !ids.isEmpty()) {
                    DetachedCriteria detachedCriteriaForData = DetachedCriteria.forClass(ProcessInstance.class, "data");
                    detachedCriteriaForData.addOrder(Order.desc("createDate"));
                    detachedCriteriaForData.add(Property.forName("id").in(ids));
                    detachedCriteriaForData.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                    
                    Criteria criteria2 = detachedCriteriaForData.getExecutableCriteria(session);
                    criteria2.setFetchMode("definition",FetchMode.SELECT);
		            criteria2.setFetchMode("creator",FetchMode.SELECT);
		            criteria2.setFetchMode("parent",FetchMode.SELECT);
 
                    list = criteria2.list();
                }
                else {
                    list = new ArrayList<ProcessInstance>(0);
                }
            }

            return new ResultsPageWrapper<ProcessInstance>(list, resultsCount);
        }

        private DetachedCriteria buildhibernateQuery(Collection<String> internalIds, ProcessInstanceFilter filter) {
            DetachedCriteria criteria = DetachedCriteria.forClass(ProcessInstance.class, "ids");

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            //criteria.setProjection(Projections.distinct(Projections.property("id")));

            criteria = criteria.add(Restrictions.in("internalId", internalIds));

            if (filter.getCreatedAfter() != null) {
                criteria = criteria.add(Restrictions.gt("createDate", filter.getCreatedAfter()));
            }

            if (filter.getCreatedBefore() != null) {
                criteria = criteria.add(lt("createDate", filter.getCreatedBefore()));
            }

            if (filter.getCreatorLogins() != null && !filter.getCreatorLogins().isEmpty()) {
                criteria = criteria.add(in("creatorLogin", filter.getCreatorLogins()));
            }

            if (filter.getUpdatedAfter() != null) {
                criteria = criteria
                        .createCriteria("processLogs")
                        .add(gt("entryDate", filter.getUpdatedAfter()));
            }

            if (filter.getNotUpdatedAfter() != null) {
                DetachedCriteria entryDateCriteria = DetachedCriteria.forClass(ProcessInstanceLog.class).add(Restrictions.gt("entryDate", filter.getNotUpdatedAfter()));
                criteria = criteria
                        .createCriteria("processLogs")
                        .add(not(Subqueries.exists(entryDateCriteria)));
            }

            return criteria;
        }


    @Override
    public Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit,
                                                       boolean onlyRunning, String[] userRoles,
                                                       String assignee, String... queues) {
    	
    	 long start = System.currentTimeMillis();
//        List<Long> processIds = searchProvider.searchProcesses(filter, offset, limit, onlyRunning, userRoles, assignee, queues);
//        List<ProcessInstance> processInstancesByIds = getProcessInstancesByIds(processIds);
//        java.util.Collections.sort(processInstancesByIds, new Comparator<ProcessInstance>() {
//            @Override
//            public int compare(ProcessInstance o1, ProcessInstance o2) {
//                return o2.getId().compareTo(o1.getId());
//            }
//        });
//
        List<ProcessInstance> list = (List<ProcessInstance>)session.createCriteria(ProcessInstance.class)
                .add(or(like("externalKey", "%"+filter+"%"), like("internalId", "%"+filter+"%")))
                .setMaxResults(limit)
                .setFirstResult(offset)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("createDate"))
                .list();
        long duration = System.currentTimeMillis() - start;
		logger.severe("searchProcesses: " +  duration);
        return list;
    }
}
