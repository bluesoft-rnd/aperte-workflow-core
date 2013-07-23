package pl.net.bluesoft.rnd.processtool.di;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;

/**
 *  Factory based on class dependency injection
 *  
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ObjectFactory 
{
    private static final Logger logger = Logger.getLogger(ObjectFactory.class.getName());
    
	public static <T> T create(Class<T> clazz, Object ... constructorArguments) throws IllegalArgumentException
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();

		return dependencyManager.create(clazz, constructorArguments);
	}

	/**
	 * This method perform auto dependency injection for all fields with {@link AutoInject} annotation
	 * @param object
	 */
	public static void inject(Object object)
	{
		injectDependency(object.getClass(), object);
	}
	
	/**
	 * This method perform auto dependency injection for all static fields with {@link AutoInject} annotation for class
	 * 
	 * @param object
	 */
	public static void inject(Class<?> objectClass)
	{
		injectDependency(objectClass, null);
	}
	
	private static void injectDependency(Class<?> objectClass, Object object)
	{		
		/* recursive dependencies injection for subclasses */
		if(objectClass.getSuperclass() != null)
			injectDependency(objectClass.getSuperclass(), object);
		
		/* Search all fields for autoinject annotation */
		for (Field field : objectClass.getDeclaredFields()) 
		{
			if (field.isAnnotationPresent(AutoInject.class)) 
			{
				/* If object is null and field is not static, ignore it */
				if(!Modifier.isStatic(field.getModifiers()) && object == null)
					continue;
				
				try
				{
					Class<?> declaratedInterface = field.getType();
					
					/* Create new object */
					Object createdObject = create(declaratedInterface);
					
					field.setAccessible(true);
					
					field.set(object, createdObject);
				}
				catch(SecurityException ex)
				{
					logger.log(Level.SEVERE, "Field access problem during dependency injection. Field name: "+field.getName(), ex);
				}
				catch(IllegalAccessException ex)
				{
					logger.log(Level.SEVERE, "Field access problem during dependency injection. Field name: "+field.getName(), ex);
				}
				catch(IllegalArgumentException ex)
				{
					logger.log(Level.SEVERE, "Object creation problem during dependency injection. Maybe osgi depndencies are not properly exported / imported? Field name: "+field.getName(), ex);
				}
			}
		}
	}
}
