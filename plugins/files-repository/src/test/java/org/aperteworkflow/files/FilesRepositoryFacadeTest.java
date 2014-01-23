package org.aperteworkflow.files;

import org.apache.commons.io.IOUtils;
import org.aperteworkflow.files.dao.FilesRepositoryItemDAO;
import org.aperteworkflow.files.dao.FilesRepositoryItemDAOImpl;
import org.aperteworkflow.files.dao.FilesRepositoryStorageDAO;
import org.aperteworkflow.files.dao.FilesRepositoryStorageDAOImpl;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactory;
import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.*;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceDAOImpl;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.naming.NamingException;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryFacadeTest {
    private static final Logger logger = Logger.getLogger(FilesRepositoryFacadeTest.class.getName());

    private static final String CREATOR_LOGIN = "test";

    private static Properties properties;
    private static FilesRepositoryStorageConfig config;
    private static FilesRepositoryConfigFactory configFactory;

    private static SessionFactory sessionFactory;

    private Session session;

    private Transaction tx;
    private FilesRepositoryItemDAO frItemDAO;
    private FilesRepositoryStorageDAO frStorageDAO;

    private IFilesRepositoryFacade filesRepoFacade;
    private ProcessInstanceDAO processInstanceDAO;
    private ProcessInstance exProcessInstance;

    public static Properties getProperties() {
        return properties;
    }

    @BeforeClass
    public static void beforeClass() throws NamingException {
        configFactory = new FilesRepositoryConfigFactory() {
            @Override
            public FilesRepositoryStorageConfig createFilesRepositoryStorageConfig() {
                return new FilesRepositoryStorageConfig() {
                    @Override
                    public String getStorageRootDirPath() {
                        return "C:\\temp";
                    }
                };
            }
        };
        config = configFactory.createFilesRepositoryStorageConfig();

        // Create hibernate session factory
        Configuration configuration = new Configuration();
        configuration.configure("test.hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
    }

    @Before
    public void setUp() throws NamingException {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        processInstanceDAO = new ProcessInstanceDAOImpl(session);
        exProcessInstance = processInstanceDAO.findAll().get(0);
        frItemDAO = new FilesRepositoryItemDAOImpl(session, processInstanceDAO);
        frStorageDAO = new FilesRepositoryStorageDAOImpl(config);
        filesRepoFacade = new FilesRepositoryFacade(session, configFactory, processInstanceDAO);
    }

    @After
    public void tearDown() {
        if (tx != null) tx.rollback();
        if (session != null && session.isOpen()) session.close();
    }

    @Test
    public void testLoadConfig() {
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getStorageRootDirPath());
    }

    @Test
    public void testUploadFile() throws UploadFileException, IOException {
        FilesRepositoryItem item1 = new FilesRepositoryItem();
        item1.setProcessInstance(exProcessInstance);
        item1.setName("ExampleFile.txt");
        item1.setDescription("Description of ExampleFile.txt");
        item1.setContentType("text/plain");
        InputStream inputStream = IOUtils.toInputStream("File content");
        Long newItemId = filesRepoFacade.uploadFile(inputStream, item1.getContentType(), item1.getProcessInstance().getId(), item1.getName(), item1.getDescription(), CREATOR_LOGIN).getId();
        IOUtils.closeQuietly(inputStream);

        FilesRepositoryItem newItem = frItemDAO.getItemById(newItemId);
        FileItemContent content = frStorageDAO.loadFileFromStorage(newItem.getRelativePath());

        Assert.assertArrayEquals("Old and new item properties doesn't equals", new String[]{item1.getName(), item1.getDescription()}
                , new String[]{newItem.getName(), newItem.getDescription()});
        Assert.assertNotNull(newItem.getRelativePath());
        Assert.assertNotNull("CreatorLogin of new item has been not set", newItem.getCreatorLogin());
        Assert.assertNotNull("CreatedDate of new item has been not set", newItem.getCreateDate());
        // TODO Assert.assertArrayEquals("File content is not the same!", content.getBytes(), IOUtils.toByteArray(inputStream));
    }

    @Test
    public void testDeleteFile() {
        // TODO
    }

    @Test
    public void testDownloadFile() {
        // TODO
    }

}
