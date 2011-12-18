package pl.net.bluesoft.rnd.pt.ext.widget;

import java.util.Map;

public interface IDocumentStorageFacade {

    public void uploadDocument(String filename, String folder, final byte[] bytes, final String MIMEType, Map<String, String> newProperties);

}