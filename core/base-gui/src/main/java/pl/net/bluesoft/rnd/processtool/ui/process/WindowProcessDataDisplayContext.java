package pl.net.bluesoft.rnd.processtool.ui.process;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Window;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class WindowProcessDataDisplayContext implements ProcessDataDisplayContext {
    private Window window;

    public WindowProcessDataDisplayContext(final Window window) {
        this.window = window;
        window.addAction(new ShortcutListener("Close window", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                window.getParent().removeWindow(window);
            }
        });
    }

    @Override
    public void hide() {
        window.getParent().removeWindow(window);
    }

    @Override
    public void setCaption(String newCaption) {
        window.setCaption(newCaption);
    }
}
