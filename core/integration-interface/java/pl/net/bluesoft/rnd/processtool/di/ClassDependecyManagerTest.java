package pl.net.bluesoft.rnd.processtool.di;

import junit.framework.TestCase;

/**
 * Test for the {@link ClassDependencyManager} class
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ClassDependecyManagerTest extends TestCase 
{
	/**
	 * Test: provide implementation for given interface, check if it is propertyle returned
	 */
	public void testInjection_1()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		/* Inject class1 with priority 0 (default). Should initialize first implementation */
		
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass1.class);
		
		Class<? extends TestDepenendencyInterface> class1 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
		
		if(!class1.getName().equals(TestDependnecyClass1.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1.class.getName());
	}
	
	/**
	 * Test: do not provide implementation for given interface, check if exception is thrown
	 */
	public void testInjection_2()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		try
		{
			Class<? extends TestDepenendencyInterface> class1 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
			
			fail("Error. No exception is thrown, manager returned class: "+class1.getName());
		}
		catch(IllegalArgumentException ex)
		{
			
		}
	}
	
	/**
	 * Test: provide implementation 1 for given interface with priority 0, then replace it with another, with priority 1, then try
	 * to replace it with implementation with lower priority
	 */
	public void testInjection_3()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		/* Inject class1 with priority 0. Should initialize first implementation */
		
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass1.class, 0);
		
		Class<? extends TestDepenendencyInterface> class1 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
		
		if(!class1.getName().equals(TestDependnecyClass1.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1.class.getName());
		
		/* Inject class2 with priority 1. Should replace previous implementation */
		
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass2.class, 1);
		
		Class<? extends TestDepenendencyInterface> class2 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
		
		if(!class2.getName().equals(TestDependnecyClass2.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1.class.getName());
		
		/* Inject class1 with priority 0. Should not replace previous implementation */
		
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass1.class, 0);
		
		Class<? extends TestDepenendencyInterface> class1_2 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
		
		if(!class1_2.getName().equals(TestDependnecyClass2.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1.class.getName());
	}
	
	/**
	 * Test: provide implementation #1 for given interface #1 and implementation #2 for interface #2
	 */
	public void testInjection_4()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		/* Inject class #1 for interface #1 with priority 0. Check if class #1 is returned*/
		
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass1.class, 0);
		
		Class<? extends TestDepenendencyInterface> class1 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
		
		if(!class1.getName().equals(TestDependnecyClass1.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1.class.getName());
		
		/* Inject class #2 for interface #2 with priority 0. Check if class #2 is returned*/
		
		dependencyManager.injectImplementation(TestDepenendencyInterface_2.class, TestDependnecyClass1_2.class, 1);
		
		Class<? extends TestDepenendencyInterface_2> class2 = dependencyManager.getImplementation(TestDepenendencyInterface_2.class);
		
		if(!class2.getName().equals(TestDependnecyClass1_2.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1_2.class.getName());
		
		class1 = dependencyManager.getImplementation(TestDepenendencyInterface.class);
		if(!class1.getName().equals(TestDependnecyClass1.class.getName()))
			fail("Error. Returned class: "+class1.getName()+", expected: "+TestDependnecyClass1.class.getName());
		
	}
	
	/** Test classes */
	
	private interface TestDepenendencyInterface {}
	private class TestDependnecyClass1 implements TestDepenendencyInterface { }
	private class TestDependnecyClass2 implements TestDepenendencyInterface { }
	
	private interface TestDepenendencyInterface_2 {}
	private class TestDependnecyClass1_2 implements TestDepenendencyInterface_2 {}

}
