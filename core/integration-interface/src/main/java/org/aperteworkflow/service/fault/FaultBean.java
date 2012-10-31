package org.aperteworkflow.service.fault;
/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public class FaultBean {
	

	private Integer errorCode;
    
	public FaultBean(Integer errorCode) {
		super();
		this.errorCode = errorCode;
	}
	

    public Integer getErrorCode() {
		return errorCode;
	}

}
