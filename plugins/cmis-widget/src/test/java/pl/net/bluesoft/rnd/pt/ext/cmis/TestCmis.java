package pl.net.bluesoft.rnd.pt.ext.cmis;

import org.junit.Test;
import pl.net.bluesoft.rnd.pt.utils.cmis.CmisAtomSessionFacade;

public class TestCmis {
    @Test
    public void testAlfresco() {
        String repositoryUser = "awf";
        String repositoryPassword = "awf";
        String repositoryId = "6f37b3de-310d-46f8-a6f6-78ac16888c78";
        String repositoryURL = "http://dreihund:8080/alfresco/service/cmis";
        CmisAtomSessionFacade facade = new CmisAtomSessionFacade(repositoryUser, repositoryPassword, repositoryURL, repositoryId);
        System.out.println("OK");
    }
}
