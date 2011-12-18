package pl.net.bluesoft.rnd.pt.ext.widget;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.service.DLFileEntryServiceUtil;

public class LiferayDocumentStorageFacade implements IDocumentStorageFacade {

    private static Logger LOGGER = Logger.getLogger(LiferayDocumentStorageFacade.class.getName());

    @Override
    public void uploadDocument(String filename, String folder, byte[] bytes,
                               String MIMEType, Map<String, String> newProperties) {

        try {
            List<DLFileEntry> entries = DLFileEntryServiceUtil.getFileEntries(10L, 10L);

            LOGGER.info("We have " + entries.size() + ": " + entries);

        } catch (PortalException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SystemException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

}