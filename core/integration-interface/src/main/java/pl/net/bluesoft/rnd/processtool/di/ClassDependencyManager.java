package pl.net.bluesoft.rnd.processtool.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

	private final Map<Class, Object> singletons = new HashMap<Class, Object>();
	
	/** Get the class dependency manager instance */
	public static synchronized ClassDependencyManager getInstance()
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

	public <T> void injectImplementation(Class<T> patternInterface, Class<? extends T> implementation, boolean singleton)
	{
		injectImplementation(patternInterface, implementation, 0, singleton);
	}

	public <T> void injectImplementation(Class<T> patternInterface, Class<? extends T> implementation, int priority)
	{
		injectImplementation(patternInterface, implementation, priority, false);
	}

	/**
	 * Inject dependency.
	 * 
	 * @param patternInterface
	 * @param implementation
	 * @param priority
	 */
	public <T> void injectImplementation(Class<T> patternInterface, Class<? extends T> implementation, int priority, boolean singleton)
	{
		synchronized(instance)
		{
			/* Find implementaton for provided api */
			ImplementationBean existingImplementation = dependencies.get(patternInterface.getName());
			
			/* No dependency exists, create one */
			if(existingImplementation == null)
			{
				existingImplementation = new ImplementationBean(implementation, priority, singleton);
				dependencies.put(patternInterface.getName(), existingImplementation);
			}
			/* If new implementation has greater priority then the previous one, exchange them */
			else if(existingImplementation.getPriority() <= priority)
			{
				existingImplementation.setClassInstance(implementation);
				existingImplementation.setPriority(priority);
				existingImplementation.setSingleton(singleton);
			}
			else
			{
				logger.warning("New implementation ["+implementation+"] of api "+patternInterface+" has lower priority ["+priority+
						"] then the previus one [" +existingImplementation.getClassInstance()+"]");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the implementation of given api
	 * 
	 * @param patternInterface api for which the implementation is searched
	 * @return class object with specified implementation
	 * @throws IllegalArgumentException thrown if there is no implementation of given api
	 */
	public <T> Class<? extends T> getImplementation(Class<T> patternInterface) throws IllegalArgumentException
	{
		ImplementationBean existingImplementation = getImplementationBean(patternInterface);

		try 
		{
			ClassLoader classLoader = existingImplementation.getClassInstance().getClassLoader();
			
			Class<? extends T> clazz = (Class<? extends T>) classLoader.loadClass(existingImplementation.getClassInstance().getName());
			
			return clazz;
		} 
		catch (ClassNotFoundException e) 
		{
			throw new IllegalArgumentException("There is no accessible implementation for api: "+patternInterface, e);
		}
	}

	private <T> ImplementationBean getImplementationBean(Class<T> patternInterface) {
	/* Find implementaton for provided api */
		ImplementationBean existingImplementation = dependencies.get(patternInterface.getName());

		if(existingImplementation == null)
			throw new IllegalArgumentException("There is no implementation for given api: "+patternInterface);
		return existingImplementation;
	}

	public synchronized <T> T create(Class<T> patternInterface, Object[] constructorArguments) {
		Class<? extends T> implementationClass = getImplementation(patternInterface);

		if (getImplementationBean(patternInterface).isSingleton()) {
			T instance = (T)singletons.get(patternInterface);

			if (instance == null) {
				instance = newInstance(implementationClass, constructorArguments);
				singletons.put(patternInterface, instance);
			}
			return instance;
		}
		return newInstance(implementationClass, constructorArguments);
	}

	private  <T> T newInstance(Class<? extends T> implementationClass, Object[] constructorArguments) {
		/* Initilize constuctor with arguments */
		Class<?>[] argumentsClasses = new Class<?>[constructorArguments.length];

		for (int i=0; i<constructorArguments.length; i++) {
			argumentsClasses[i] = constructorArguments[i].getClass();
		}

		try
		{
			/* There is no arguments for constructor, invoke default constructor */
			if (constructorArguments.length == 0)
			{
				return implementationClass.newInstance();
			}

			/* Create new class instance */
			Constructor<? extends T> contructor = implementationClass.getConstructor(argumentsClasses);

			return contructor.newInstance(constructorArguments);
		}
		catch (InstantiationException e)
		{
			throw new IllegalArgumentException("Problem during creation of dependency injected class", e);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalArgumentException("Problem during creation of dependency injected class. Maybe osgi depndencies are not properly exported / imported? ", e);
		}
		catch (InvocationTargetException e)
		{
			throw new IllegalArgumentException("Problem during creation of dependency injected class", e);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Problem during creation of dependency injected class. Maybe osgi depndencies are not properly exported / imported?", e);
		}
		catch (SecurityException e)
		{
			throw new IllegalArgumentException("Problem during creation of dependency injected class", e);
		}
	}
	
	void clear()
	{
		dependencies.clear();
	}
	
	/**
	 * Dependency Injection bean with priority
	 */
	private static class ImplementationBean
	{
		private Class<?> classInstance;
		private int priority = 0;
		private boolean singleton;
		
		public ImplementationBean(Class<?> classInstance, int priority, boolean singleton)
		{
			this.classInstance = classInstance;
			this.priority = priority;
			this.singleton = singleton;
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

		private boolean isSingleton() {
			return singleton;
		}

		private void setSingleton(boolean singleton) {
			this.singleton = singleton;
		}
	}
}
