package pl.net.bluesoft.rnd.processtool.plugins;

/**
 * Widget version provider 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetVersionProvider 
{
	/** Get widget version info */
	String getWidgetVersionInfo(Class<?> widgetClass);

}
