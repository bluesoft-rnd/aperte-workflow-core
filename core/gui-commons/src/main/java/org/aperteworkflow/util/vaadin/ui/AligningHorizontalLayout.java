package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.util.Iterator;

/**
 * @author amichalak@bluesoft.net.pl
 */
public class AligningHorizontalLayout extends HorizontalLayout {
    private Alignment alignment;
    private boolean alwaysCalculateExpandRatios = false;

    public AligningHorizontalLayout(Alignment alignment, boolean alwaysCalculateExpandRatios) {
        this.alignment = alignment;
        this.alwaysCalculateExpandRatios = alwaysCalculateExpandRatios;
        setSpacing(true);
    }

    public AligningHorizontalLayout(Alignment alignment, Component... components) {
        this.alignment = alignment;
        setSpacing(true);
        addComponents(components);
    }

    public void addComponents(Component[] components) {
        if (components != null && components.length > 0) {
            boolean current = alwaysCalculateExpandRatios;
            alwaysCalculateExpandRatios = false;
            for (Component c : components) {
                addComponent(c);
            }
            alwaysCalculateExpandRatios = current;
        }
        recalculateExpandRatios();
    }

    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        setComponentAlignment(c, alignment);
        if (alwaysCalculateExpandRatios) {
            recalculateExpandRatios();
        }
    }

    @Override
    public void addComponentAsFirst(Component c) {
        super.addComponentAsFirst(c);
        setComponentAlignment(c, alignment);
        if (alwaysCalculateExpandRatios) {
            recalculateExpandRatios();
        }
    }

    public void recalculateExpandRatios() {
        for (Iterator<Component> it = getComponentIterator(); it.hasNext(); ) {
            setExpandRatio(it.next(), 0.0f);
        }
        if (getComponentCount() > 0) {
            if (alignment.isLeft()) {
                setExpandRatio(getComponent(getComponentCount() - 1), 1.0f);
            }
            else if (alignment.isRight()) {
                setExpandRatio(getComponent(0), 1.0f);
            }
        }
    }
}
