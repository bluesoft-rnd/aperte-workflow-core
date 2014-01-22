package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.*;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceDAOImpl;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.util.lang.Classes;

import javax.naming.NamingException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryItemDAOTest {
    private static final Logger logger = Logger.getLogger(FilesRepositoryItemDAOTest.class.getName());

    private static final String CREATOR_LOGIN = "test";

    private static SessionFactory sessionFactory;

    private Session session;
    private Transaction tx;

    private FilesRepositoryItemDAO dao;
    private ProcessInstanceDAO processInstanceDAO;

    private ProcessInstance exProcessInstance;

    @BeforeClass
    public static void beforeClass() throws NamingException {
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
        dao = new FilesRepositoryItemDAOImpl(session, processInstanceDAO);
    }

    @After
    public void tearDown() {
        if (tx != null) tx.rollback();
        if (session != null && session.isOpen()) session.close();
    }

    @Test
    public void testLoadConfig() {
    }

    @Test
    public void testAddItemByProperties() {
        FilesRepositoryItem item1 = new FilesRepositoryItem();
        item1.setProcessInstance(exProcessInstance);
        item1.setName("ExampleFile.txt");
        item1.setRelativePath("ExampleFile_relativePath.txt");
        item1.setDescription("Description of ExampleFile.txt");
        FilesRepositoryItem newItem = dao.addItem(item1.getProcessInstance().getId(), item1.getName(), item1.getDescription(), item1.getRelativePath(), item1.getContentType(), CREATOR_LOGIN);
        Assert.assertArrayEquals("Old and new item properties doesn't equals", new String[]{item1.getName(), item1.getRelativePath(), item1.getDescription()}
                , new String[]{newItem.getName(), newItem.getRelativePath(), newItem.getDescription()});
        Assert.assertNotNull("CreatorLogin of new item has been not set", newItem.getCreatorLogin());
        Assert.assertNotNull("CreatedDate of new item has been not set", newItem.getCreateDate());
    }

    @Test
    public void testGetItems() {

        FilesRepositoryItem newItem1 = dao.addItem(exProcessInstance.getId(), "1.txt", "1_relativePath.txt", "Description of 1.txt", "text/plain", CREATOR_LOGIN);
        FilesRepositoryItem newItem2 = dao.addItem(exProcessInstance.getId(), "2.txt", "2_relativePath.txt", "Description of 2.txt", "text/plain", CREATOR_LOGIN);
        FilesRepositoryItem newItem3 = dao.addItem(exProcessInstance.getId(), "3.txt", "3_relativePath.txt", "Description of 3.txt", "text/plain", CREATOR_LOGIN);

        Collection<FilesRepositoryItem> retItems = dao.getItemsFor(exProcessInstance.getId());

        Assert.assertEquals("Wrong returned items collection size", retItems.size(), 3);
        Assert.assertArrayEquals("Identifiers don't match", new Long[]{retItems.toArray(new FilesRepositoryItem[]{})[0].getId()
                , retItems.toArray(new FilesRepositoryItem[]{})[1].getId()
                , retItems.toArray(new FilesRepositoryItem[]{})[2].getId()}, new Long[]{newItem1.getId(), newItem2.getId(), newItem3.getId()});
    }

    @Test
    public void testDeleteById() {
        FilesRepositoryItem newItem1 = dao.addItem(exProcessInstance.getId(), "1.txt", "1_relativePath.txt", "Description of 1.txt", "text/plain", CREATOR_LOGIN);
        FilesRepositoryItem newItem2 = dao.addItem(exProcessInstance.getId(), "2.txt", "2_relativePath.txt", "Description of 2.txt", "text/plain", CREATOR_LOGIN);
        FilesRepositoryItem newItem3 = dao.addItem(exProcessInstance.getId(), "3.txt", "3_relativePath.txt", "Description of 3.txt", "text/plain", CREATOR_LOGIN);

        dao.deleteById(newItem2.getId());

        Collection<FilesRepositoryItem> retItems = dao.getItemsFor(exProcessInstance.getId());

        Assert.assertEquals("Returned items after delete one element doesn't match", retItems.size(), 2);
        Assert.assertEquals("Bad identifier of returned item after delete by id", retItems.toArray(new FilesRepositoryItem[]{})[0].getId(), newItem1.getId());
        Assert.assertEquals("Bad identifier of returned item after delete by id", retItems.toArray(new FilesRepositoryItem[]{})[1].getId(), newItem3.getId());
    }

    @Test
    public void testUpdateDescriptionById() {
        FilesRepositoryItem newItem1 = dao.addItem(exProcessInstance.getId(), "1.txt", "1_relativePath.txt", "Description of 1.txt", "text/plain", CREATOR_LOGIN);

        dao.updateDescriptionById(newItem1.getId(), "New Description for 1.txt");

        FilesRepositoryItem updatedNewItem1 = dao.getItemById(newItem1.getId());

        Assert.assertEquals("Updated description doesn't match with expected", updatedNewItem1.getDescription(), newItem1.getDescription());
    }
}
