package org.aperteworkflow.util;

import org.aperteworkflow.service.fault.AperteWsIllegalArgumentException;

public class AperteErrorCheckUtil {
	
	public static void checkCorrectnessOfArgument (Object object, AperteIllegalArgumentCodes aperteIllegalArgumentCodes) throws AperteWsIllegalArgumentException{
		if(object== null){
			aperteIllegalArgumentCodes.throwAperteWebServiceException();
		}	
		if(object instanceof String ){
			String string = (String) object;
			if(string.isEmpty()){
				aperteIllegalArgumentCodes.throwAperteWebServiceException();	
			}	
		}
	}

}
