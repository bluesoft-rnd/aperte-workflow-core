package pl.net.bluesoft.rnd.processtool.plugins.util;


/**
 * Simple mock class decorator. It returns the same text as given
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class PlainTextDecorator implements IWriterTextDecorator 
{
	private StringBuilder stringBuilder = new StringBuilder();

	@Override
	public void addText(String text) 
	{
		stringBuilder.append(text);
		stringBuilder.append("\n");
		
	}

	@Override
	public String getOutput() {
		return stringBuilder.toString();
	}

}
