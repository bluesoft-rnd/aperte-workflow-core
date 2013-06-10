package pl.net.bluesoft.rnd.processtool.plugins.util;

/**
 * Interface for text decorators 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWriterTextDecorator 
{
	/**
	 * Add new text line
	 * @param text message in new line
	 */
	void addText(String text);
	
	/** Get the servlet out text 
	 * 
	 * @return processed output
	 */
	String getOutput();
}
