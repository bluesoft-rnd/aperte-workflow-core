package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.Component;

import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface TransactionProvider {
	void withTransaction(ProcessToolGuiCallback r);
	<T> T withTransaction(ReturningProcessToolContextCallback<T> r);

	static class Helper {
		public static void withTransaction(Component c, ProcessToolGuiCallback r) {
			Application app = c.getApplication();
			TransactionProvider tc = (TransactionProvider) app;
			tc.withTransaction(r);
		}
	}
}
