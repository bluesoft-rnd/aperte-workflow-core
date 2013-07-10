package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Collection;

/**
 * Widget whitch provides html code for its own generation
 *
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IWidgetHtmlCodeProvider
{
    /** Generate widget html code with given widget children */
    String generate(Collection<IWidgetHtmlCodeProvider> children);
}
