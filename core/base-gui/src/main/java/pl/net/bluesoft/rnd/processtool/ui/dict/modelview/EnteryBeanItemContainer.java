package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import com.vaadin.data.util.BeanItemContainer;

public class EnteryBeanItemContainer<T> extends BeanItemContainer<T> 
{
	public EnteryBeanItemContainer(Class<? super T> type)
			throws IllegalArgumentException {
		super(type);
	}
	
	public void refresh()
	{
		fireItemSetChange();
	}
}
