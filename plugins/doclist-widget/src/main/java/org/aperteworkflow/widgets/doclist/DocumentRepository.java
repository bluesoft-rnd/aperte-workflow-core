package org.aperteworkflow.widgets.doclist;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface DocumentRepository {

    public Collection<DocumentEntry> getEntries(String path);
    public void uploadEntry(DocumentEntry entry, String path);
    public void createFolderIfNotExists(String path);

}
