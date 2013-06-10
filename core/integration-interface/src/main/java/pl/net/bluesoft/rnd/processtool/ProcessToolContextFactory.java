package pl.net.bluesoft.rnd.processtool;

import org.hibernate.SessionFactory;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.io.InputStream;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolContextFactory {
    <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback);
    <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback);

	ProcessToolRegistry getRegistry();

	void updateSessionFactory(SessionFactory sf);
}
