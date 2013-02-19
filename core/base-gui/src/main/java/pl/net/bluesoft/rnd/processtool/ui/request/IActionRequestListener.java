package pl.net.bluesoft.rnd.processtool.ui.request;

/**
 * Interface to class which listens to action request 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IActionRequestListener 
{
	/** Handle action request */
	void handleActionRequest(IActionRequest actionRequest);

}
