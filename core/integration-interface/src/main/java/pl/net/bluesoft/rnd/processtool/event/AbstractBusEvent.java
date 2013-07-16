package pl.net.bluesoft.rnd.processtool.event;

import java.util.ArrayList;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.event.beans.ErrorBean;

/**
 * Event bus for Spring MVC
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class AbstractBusEvent 
{
	/** Errors which occur during request processing */
	private List<ErrorBean> errors = new ArrayList<ErrorBean>();
	
	public List<ErrorBean> getErrors() {
		return errors;
	}
	
	public boolean hasErrors()
	{
		return !this.errors.isEmpty();
	}
	
	public void addError(String source, String message)
	{
		ErrorBean errorBean = new ErrorBean();
		errorBean.setSource(source);
		errorBean.setMessage(message);
		
		this.errors.add(errorBean);
	}

}
