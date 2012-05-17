package org.aperteworkflow.contrib.document.providers.manager;

import java.util.Collection;
import java.util.Map;

/**
 * Document provider hides and allows for multiple implementations of document service in Aperte Workflow.
 * But of course, it is possible to use this facility outside of Aperte Workflow mechanisms will little effort.
 *
 * @author tlipski@bluesoft.net.pl
 */
public interface DocumentProvider {


    String REPOSITORY_ID = "repositoryId";
    String ROOT_FOLDER_PATH = "rootFolderPath";
    String FOLDER_NAME = "folderName";
    String ATOM_URL = "repositoryAtomUrl";
    String PASS = "repositoryPassword";
    String USER = "repositoryUser";
    String NEW_FOLDER_PREFIX = "newFolderPrefix";
    String LOGIN = "login";
    String GROUP_ID = "groupId";
    String COMPANY_ID = "companyId";
    String GROUP_NAME = "groupName";

    /**
     * Set configuration - this method is called on a interface when it is supplied to service consumer (e.g.
     * document management widget)
     *
     * @param properties Configuration properties, for example CMIS access url and credentials
     */
    void configure(Map<String, String> properties);

    void uploadDocument(Document doc);
    Collection<Document> getDocuments(String path);
}
