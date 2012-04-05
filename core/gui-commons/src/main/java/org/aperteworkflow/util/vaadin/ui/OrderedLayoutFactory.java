package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.ui.AbstractOrderedLayout;

/**
 * @author amichalak@bluesoft.net.pl
 */
public interface OrderedLayoutFactory<T extends AbstractOrderedLayout> extends LayoutFactory<T> {
    T create();
}
