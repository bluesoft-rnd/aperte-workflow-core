package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.query.AbstractQuery;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;


/**
 * JBpm process engine query which encapsultes hql query logic 
 * 
 * @author Maciej Pawlak
 *
 */
public class ProcessEngineQuery<T>
{
	String query;
	String userAuthenticationLogin;
	
	Collection<QueryParameter> parameters;
	Collection<QueryParameterList> listParameters;
	
	public ProcessEngineQuery()
	{
		this.parameters = new ArrayList<ProcessEngineQuery<T>.QueryParameter>();
		this.listParameters = new ArrayList<ProcessEngineQuery<T>.QueryParameterList>();
	}
	
	/** Add query parameter */
	public ProcessEngineQuery<T> addParameter(String key, Object value)
	{
		this.parameters.add(new QueryParameter(key, value));
		return this;
	}
	
	/** Add query parameter */
	public ProcessEngineQuery<T> addListParameter(String key, Collection<?> values)
	{
		this.listParameters.add(new QueryParameterList(key, values));
		return this;
	}
	
	public ProcessEngineQuery<T> setQuery(String query)
	{
		this.query = query;
		return this;
	}
	
	public ProcessEngineQuery<T> setAuthenticationUserLogin(String userAuthenticationLogin)
	{
		this.userAuthenticationLogin = userAuthenticationLogin;
		
		return this;
	}
	
	/** Execute built query using specifited paramters by addParameter
	 * and addListParamter methods
	 */
	public Collection<T> executeQuery(ProcessToolContext ctx)
	{
		/* Create command and query */
   		Command<List<T>> cmd = new ProcessEngineCommand();

   		/* Execute specified query using given parameters */
   		ProcessEngine processEngine = getProcessEngine(ctx);
   		List<T> tasks = processEngine.execute(cmd);
   		
   		return tasks;
	}
	
	private class ProcessEngineCommand implements Command<List<T>>
	{
		@Override
		public List<T> execute(Environment environment) throws Exception 
		{
			ProcessEngineAbstractQuery processEngineQuery = new ProcessEngineAbstractQuery();
			
			return (List<T>) processEngineQuery.execute(environment);
		}
		
	}
	
	/** Class to encapsulate query logic */
	private class ProcessEngineAbstractQuery extends AbstractQuery
	{

		@Override
		protected void applyParameters(Query query) 
		{
			for(QueryParameter parameter: parameters)
					query.setParameter(parameter.getKey(), parameter.getValue());
				
			for(QueryParameterList parameterList: listParameters)
				query.setParameterList(parameterList.getKey(), parameterList.getValues());
			
		}

		@Override
		public String hql() 
		{
			return query;
		}
		
	}
	
	/** Get process engine by given process contexted. If authentication login
	 * was provided it is injected in engine
	 */
   	private ProcessEngine getProcessEngine(ProcessToolContext ctx) 
   	{
   		if (ctx instanceof ProcessToolContextImpl) 
   		{
   			ProcessEngine engine = ((ProcessToolContextImpl) ctx).getProcessEngine();
   			if (userAuthenticationLogin != null)
   				engine.setAuthenticatedUserId(userAuthenticationLogin);

   			return engine;
   		}
   		else 
   			throw new IllegalArgumentException(ctx + " not an instance of " + ProcessToolContextImpl.class.getName());
   	}
	
   	/** Class which provied key-object parameter for query */
	private class QueryParameter
	{
		private String key;
		private Object value;
		
		public QueryParameter(String key, Object value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		public Object getValue() {
			return value;
		}
	}
	
	/** Class which provied key-list list parameter for query */
	private class QueryParameterList
	{
		private String key;
		private Collection<?> values;
		
		public QueryParameterList(String key, Collection<?> values)
		{
			this.key = key;
			this.values = values;
		}
		
		public String getKey() {
			return key;
		}
		public Collection<?> getValues() {
			return values;
		}

	}
}
