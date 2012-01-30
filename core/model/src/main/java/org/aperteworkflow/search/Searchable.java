package org.aperteworkflow.search;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface Searchable {

    Collection<ProcessInstanceSearchAttribute> getAttributes();
}
