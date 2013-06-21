package pl.net.bluesoft.rnd.processtool.web.domain;

import pl.net.bluesoft.rnd.processtool.event.beans.ErrorBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract result bean 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class AbstractResultBean implements Serializable
{
	private static final long serialVersionUID = 6474043311954264702L;
	
	/** Errors which occur during request processing */
	private List<ErrorResultBean> errors = new ArrayList<ErrorResultBean>();
	
	public List<ErrorResultBean> getErrors() {
		return errors;
	}
	
	public boolean hasErrors()
	{
		return !this.errors.isEmpty();
	}
	
	public void addError(ErrorBean errorBean) 
	{
		addError(errorBean.getSource(), errorBean.getMessage());
	}
	
	public void addError(ErrorResultBean errorBean) 
	{
		addError(errorBean.getSource(), errorBean.getMessage());
	}
	
	public void addError(String source, String message)
	{
		ErrorResultBean errorBean = new ErrorResultBean();
		errorBean.setSource(source);
		errorBean.setMessage(message);
		
		this.errors.add(errorBean);
	}
	
	public void copyErrors(AbstractResultBean resultBean) 
	{
		this.errors.addAll(resultBean.getErrors());
		
	}
}
