package pl.net.bluesoft.rnd.util.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface TransactionProvider {
	void withTransaction(ProcessToolGuiCallback r);

	static class Helper {
		public static void withTransaction(Component c, ProcessToolGuiCallback r) {
			Application app = c.getApplication();
			TransactionProvider tc = (TransactionProvider) app;
			tc.withTransaction(r);
		}
	}
}
