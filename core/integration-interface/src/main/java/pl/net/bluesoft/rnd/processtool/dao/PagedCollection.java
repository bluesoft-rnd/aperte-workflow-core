package pl.net.bluesoft.rnd.processtool.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to page collections
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 * @param <T>
 */
public class PagedCollection<T> 
{
	private static final int DEFAULT_PAGE_LENGTH = 500;
	
	/** Collection iterator to speed up original collection processing */
	private Iterator<T> collectionIterator;
	
	/** Original collection */
	private Collection<T> collection;
	
	private int globalIndex = 0;
	
	private int pageLength = DEFAULT_PAGE_LENGTH;
	
	public PagedCollection(Collection<T> collection)
	{
		this.collection = collection;
		this.collectionIterator = collection.iterator();
	}
	
	/** Sets new page length. This can be called beetween {@link #getNextPage()} 
	 * invocations 
	 * @param newPageLegth new length of page
	 */
	public void setPageLength(int newPageLegth)
	{
		this.pageLength = newPageLegth;
	}
	
	/** Reset to the first page */
	public void reset()
	{
		this.globalIndex = 0;
		this.collectionIterator = collection.iterator();
	}
	
	/** Get collection representing next page */
	public Collection<T> getNextPage()
	{
		/* Collection of current page */
		List<T> page = new ArrayList<T>(pageLength);	
		
		for(int index = 0; (index<pageLength && collectionIterator.hasNext()); index++)
		{
			globalIndex++;
			page.add(collectionIterator.next());
		}
		
		return page;
	}
	
	/* If there are more elements in next page */
	public boolean hasMoreElements()
	{
		/* If there is at least one element in next page, return true */
		return collectionIterator.hasNext();
	}

}
