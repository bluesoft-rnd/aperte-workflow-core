package pl.net.bluesoft.rnd.processtool.di;

import junit.framework.TestCase;

/**
 * Tests for {@link ObjectFactory} class
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ObjectFactoryTest extends TestCase 
{
	/** Test: Try to create object by given implementation */
	@SuppressWarnings("unused")
	public void testObjectFactory_1()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		/* Inject class1 with priority 0 (default). Should initialize first implementation */
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass1.class);
		
		TestDepenendencyInterface interfaceObject = ObjectFactory.create(TestDepenendencyInterface.class);
	}
	
	/** Test: Try to create object without given implementation */
	@SuppressWarnings("unused")
	public void testObjectFactory_2()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		try
		{
			TestDepenendencyInterface interfaceObject = ObjectFactory.create(TestDepenendencyInterface.class);
			
			fail("There should be IllegalArgumentException here, becouse there is no implementation");
		}
		catch(IllegalArgumentException ex)
		{
			
		}
	}
	
	/** Test: Try to create object with parameters */
	public void testObjectFactory_3()
	{
		ClassDependencyManager dependencyManager = ClassDependencyManager.getInstance();
		dependencyManager.clear();
		
		/* Inject class1 with priority 0 (default). Should initialize first implementation */
		dependencyManager.injectImplementation(TestDepenendencyInterface.class, TestDependnecyClass1.class);
		
		TestDepenendencyInterface interfaceObject = ObjectFactory.create(TestDepenendencyInterface.class);
		
		assertEquals(interfaceObject.getTestInteger().intValue(), 0);
		
		interfaceObject = ObjectFactory.create(TestDepenendencyInterface.class, 2);
		
		assertEquals(interfaceObject.getTestInteger().intValue(), 2);
		
		try
		{
			interfaceObject = ObjectFactory.create(TestDepenendencyInterface.class, 2, 2);
			
			fail("There should be IllegalArgumentException here, becouse there is no constructor with 2 arguments");
		}
		catch(IllegalArgumentException ex)
		{
			
		}
	}
	
	static interface TestDepenendencyInterface {Integer getTestInteger();}
	static class TestDependnecyClass1 implements TestDepenendencyInterface 
	{ 
		Integer testInteger = 0;
		public TestDependnecyClass1() {}
		public TestDependnecyClass1(Integer i) {testInteger = i;}
		
		public Integer getTestInteger() { return testInteger; }
	}
}
