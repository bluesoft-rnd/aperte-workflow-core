package pl.net.bluesoft.rnd.processtool.di;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ClassDependencyManager 
{
    private Logger logger = Logger.getLogger(ClassDependencyManager.class.getName());
    
	private static ClassDependencyManager instance;
	
	/** Get the class dependency manager instance */
	public static ClassDependencyManager getInstance()
	{
		if(instance == null)
			instance = new ClassDependencyManager();
		
		return instance;
	}
	
	/** Stored dependencies */
	private Map<String, ImplementationBean> dependencies;
	
	private ClassDependencyManager()
	{
		dependencies = new HashMap<String, ImplementationBean>();
		
		logger.info("Class Dependnecy Manager initilized");
	}
	
	public <T> void injectImplementation(Class<T> patternInterface, Class<? extends T> implementation)
	{
		injectImplementation(patternInterface, implementation, 0);
	}
	
	/**
	 * Inject dependency.
	 * 
	 * @param patternInterface
	 * @param implementation
	 * @param priority
	 */
	public <T> void injectImplementation(Class<T> patternInterface, Class<? extends T> implementation, int priority)
	{
		synchronized(instance)
		{
			/* Find implementaton for provided interface */
			ImplementationBean existingImplementation = dependencies.get(patternInterface.getName());
			
			/* No dependency exists, create one */
			if(existingImplementation == null)
			{
				existingImplementation = new ImplementationBean(implementation, priority);
				dependencies.put(patternInterface.getName(), existingImplementation);
			}
			/* If new implementation has greater priority then the previous one, exchange them */
			else if(existingImplementation.getPriority() <= priority)
			{
				existingImplementation.setClassInstance(implementation);
				existingImplementation.setPriority(priority);
			}
			else
			{
				logger.warning("New implementation ["+implementation+"] of interface "+patternInterface+" has lower priority ["+priority+
						"] then the previus one [" +existingImplementation.getClassInstance()+"]");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the implementation of given interface
	 * 
	 * @param patternInterface interface for which the implementation is searched
	 * @return class object with specified implementation
	 * @throws IllegalArgumentException thrown if there is no implementation of given interface
	 */
	public <T> Class<? extends T> getImplementation(Class<T> patternInterface) throws IllegalArgumentException
	{
		/* Find implementaton for provided interface */
		ImplementationBean existingImplementation = dependencies.get(patternInterface.getName());
		
		if(existingImplementation == null)
			throw new IllegalArgumentException("There is no implementation for given interface: "+patternInterface);
		
		try 
		{
			ClassLoader classLoader = existingImplementation.getClassInstance().getClassLoader();
			
			Class<? extends T> clazz = (Class<? extends T>) classLoader.loadClass(existingImplementation.getClassInstance().getName());
			
			return clazz;
		} 
		catch (ClassNotFoundException e) 
		{
			throw new IllegalArgumentException("There is no accessible implementation for interface: "+patternInterface, e);
		}
	}
	
	void clear()
	{
		dependencies.clear();
	}
	
	/**
	 * Dependency Injection bean with priority
	 */
	private class ImplementationBean
	{
		private Class<?> classInstance;
		private int priority = 0;
		
		public ImplementationBean(Class<?> classInstance, int priority)
		{
			this.setClassInstance(classInstance);
			this.setPriority(priority);
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public Class<?> getClassInstance() {
			return classInstance;
		}

		public void setClassInstance(Class<?> classInstance) {
			this.classInstance = classInstance;
		}
		
		
	}

}
