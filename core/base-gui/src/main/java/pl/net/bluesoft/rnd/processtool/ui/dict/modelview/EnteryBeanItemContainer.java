package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.Collection;

import com.vaadin.data.util.BeanItemContainer;

public class EnteryBeanItemContainer<T> extends BeanItemContainer<T> 
{
	public EnteryBeanItemContainer(Class<? super T> type,
			Collection<? extends T> collection) throws IllegalArgumentException {
		super(type, collection);
		// TODO Auto-generated constructor stub
	}

	public EnteryBeanItemContainer(Class<? super T> type)
			throws IllegalArgumentException {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	public void refresh()
	{
		fireItemSetChange();
	}

}
