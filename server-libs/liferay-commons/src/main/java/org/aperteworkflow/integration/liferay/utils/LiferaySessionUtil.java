package org.aperteworkflow.integration.liferay.utils;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
/*****
 <pre>* RUTVIJ SHAH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. RUTVIJ SHAH SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * THIS SOFTWARE IS IN AS-IS FORM, YOU ARE FREE TO RE-DISTRIBUTE/CHANGE WITHOUT ANY NOTIFICATION TO AUTHOR.
 ******/


import com.liferay.portal.kernel.util.PropsUtil;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rutvij Shah ( rutvij.shah@yahoo.com )
 * LiferaySessionUtil provides helper methods to share your Session attributes
 * across WARs.
 *
 */
public class LiferaySessionUtil {
    /***
     * This prefix is used by Liferay Portal to detect Session Attributes
     * for sharing betweeen WARs.
     */

    private static final String LIFERAY_SHARED_SESSION_PREFIX=getSharedSessionPrefix();
    private static final String LIFERAY_SHARED_SESSION_PREFIX_DEFAULT="LIFERAY_SHARED_";
    /****
     * It stores attribute as 'Shared' Attribute and will be available to other
     * portlets in different WARs.
     *
     * Attributes shared by this method will be available to only Portlets,
     * not other Web components i.e Servlet.
     *
     * @param key session Key to store value
     * @param value
     * @param request PortletRequest
     */
    public static final void setGlobalSessionAttribute(String key,Object value,PortletRequest request){
        if(key!=null){
            String globalKey=getGlobalKey(key);
            PortletSession portletSession=request.getPortletSession();
            portletSession.setAttribute(globalKey,value,PortletSession.APPLICATION_SCOPE);
        }
    }
    /****
     *
     * It provides access to get shared session attributes from other portles
     * from diffrent WARs.
     *
     * @param key
     * @param request
     * @return
     */
    public static final Object getGlobalSessionAttribute(String key,PortletRequest request){
        Object value=null;
        if(key!=null){
            String globalKey=getGlobalKey(key);
            PortletSession portletSession=request.getPortletSession();
            value=portletSession.getAttribute(globalKey,PortletSession.APPLICATION_SCOPE);
        }
        return value;
    }

    /******
     *
     * It provides a way to further share 'Shared'Session Attributes from
     * Portlet to other Web Components i.e Servlets .
     *
     * @param key
     * @param request
     */
    public static final void shareGlobalSessionAttribute(String key,PortletRequest request){
        if(key!=null){
            Object value=getGlobalSessionAttribute(key, request);
            PortletSession portletSession=request.getPortletSession();
            portletSession.setAttribute(key,value,PortletSession.APPLICATION_SCOPE);
        }
    }

    /***
     * Helper method to generate Global key using Liferay shared prefix
     * @param key
     * @return
     */
    private static final String getGlobalKey(String key){
        return LIFERAY_SHARED_SESSION_PREFIX+key;
    }

    /********
     * Helper method to get Liferay's Session Sharing prefix
     * Useful when Liferay is customized to use different prefix other than Default
     *
     * @return
     */
    private static final String getSharedSessionPrefix(){
        String value=null;
        try {
/**
 * Getting value from portal.properties
 */
            value = PropsUtil.get("session.shared.attributes");
        } catch (Exception ex) {
            Logger.getLogger(LiferaySessionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(value !=null){
            if(value.contains(LIFERAY_SHARED_SESSION_PREFIX_DEFAULT)){
//if default prefix is configured use it
                value=LIFERAY_SHARED_SESSION_PREFIX_DEFAULT;
            }else{
//use first one from the list of prefix configured
                value=value.split(",")[0];
            }
        }else{
/**
 * If none of the value configured use default one
 * Note: Session Sharing may not work as none of the value configured.
 */
            value=LIFERAY_SHARED_SESSION_PREFIX_DEFAULT;
        }
        return value;
    }

}