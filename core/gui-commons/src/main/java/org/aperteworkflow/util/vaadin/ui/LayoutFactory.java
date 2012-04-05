package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.ui.Layout;

/**
 * @author amichalak@bluesoft.net.pl
 */
public interface LayoutFactory<T extends Layout> {
    T create();
}
