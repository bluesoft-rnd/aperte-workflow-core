package org.aperteworkflow.webapi;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.URLTemplateLoader;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.plugins.GuiRegistry;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: MZU
 * Date: 12.01.14
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class FreemarkerOsgiTemplateLoader extends URLTemplateLoader {

    private static Logger logger = Logger.getLogger(FreemarkerOsgiTemplateLoader.class.getName());

    private String controllerName;

    private Class className;

    private String path;


    @Autowired
    private GuiRegistry guiRegistry;


    protected boolean findClassSource(){
        IOsgiWebController servletController = guiRegistry.getWebController(controllerName);
        if(servletController==null){
               logger.log(Level.WARNING,"Osgi Controller class not found: " + controllerName);
            return false;
        }
        className = servletController.getClass();
        return true;
    }

    @Override
    protected URL getURL(String name) {
        try{
            if(className==null){
                if(!findClassSource()){
                    logger.log(Level.WARNING,"Template loader not initialized");
                    return null;
                }
            }

            return className.getResource(path + name);
        }catch(Exception e){
            logger.log(Level.WARNING,"Error getting template",e);
            className = null;
            return null;
        }
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public GuiRegistry getGuiRegistry() {
        return guiRegistry;
    }

    public void setGuiRegistry(GuiRegistry guiRegistry) {
        this.guiRegistry = guiRegistry;
    }

    public Class getClassName() {
        return className;
    }

    public void setClassName(Class className) {
        this.className = className;
    }
}
