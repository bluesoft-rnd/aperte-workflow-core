package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.*;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceDAOImpl;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.naming.NamingException;
import java.util.*;
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
    private FilesRepositoryAttributeFactory factory;

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
        exProcessInstance = processInstanceDAO.findAll().get(3);
        dao = new FilesRepositoryItemDAOImpl(session);
        factory = FilesRepositoryProcessAttributeFactoryImpl.INSTANCE;
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
        IFilesRepositoryItem item1 = new FilesRepositoryItem();
        // item1.setParentObject(exProcessInstance);
        item1.setName("ExampleFile.txt");
        item1.setRelativePath("ExampleFile_relativePath.txt");
        item1.setDescription("Description of ExampleFile.txt");
        item1.setContentType("testContentType");
        IFilesRepositoryItem newItem = dao.addItem(exProcessInstance, item1.getName(), item1.getDescription(), item1.getRelativePath(), item1.getContentType(), CREATOR_LOGIN, factory);
        Assert.assertArrayEquals("Old and new item properties doesn't equals", new String[]{item1.getName(), item1.getRelativePath(), item1.getDescription()}
                , new String[]{newItem.getName(), newItem.getRelativePath(), newItem.getDescription()});
        Assert.assertNotNull("CreatorLogin of new item has been not set", newItem.getCreatorLogin());
        Assert.assertNotNull("CreatedDate of new item has been not set", newItem.getCreateDate());
    }

    @Test
    public void testGetItems() {
        IFilesRepositoryItem newItem1 = dao.addItem(exProcessInstance, "1.txt", "1_relativePath.txt", "Description of 1.txt", "text/plain", CREATOR_LOGIN, factory);
        IFilesRepositoryItem newItem2 = dao.addItem(exProcessInstance, "2.txt", "2_relativePath.txt", "Description of 2.txt", "text/plain", CREATOR_LOGIN, factory);
        IFilesRepositoryItem newItem3 = dao.addItem(exProcessInstance, "3.txt", "3_relativePath.txt", "Description of 3.txt", "text/plain", CREATOR_LOGIN, factory);

        List<FilesRepositoryItem> retItems = new ArrayList<FilesRepositoryItem>(dao.getItemsFor(exProcessInstance));

        Collections.sort(retItems, new Comparator<FilesRepositoryItem>() {
            @Override
            public int compare(FilesRepositoryItem o, FilesRepositoryItem o2) {
                return o.getId().compareTo(o2.getId());
            }
        });

        Assert.assertEquals("Wrong returned items collection size", 3, retItems.size());
        Assert.assertArrayEquals("Identifiers don't match", new Long[]{newItem1.getId(), newItem2.getId(), newItem3.getId()}, new Long[]{retItems.toArray(new FilesRepositoryItem[]{})[0].getId()
                , retItems.toArray(new FilesRepositoryItem[]{})[1].getId()
                , retItems.toArray(new FilesRepositoryItem[]{})[2].getId()});
    }

    @Test
    public void testDeleteById() {
        IFilesRepositoryItem newItem1 = dao.addItem(exProcessInstance, "1.txt", "1_relativePath.txt", "Description of 1.txt", "text/plain", CREATOR_LOGIN, factory);
        IFilesRepositoryItem newItem2 = dao.addItem(exProcessInstance, "2.txt", "2_relativePath.txt", "Description of 2.txt", "text/plain", CREATOR_LOGIN, factory);
        IFilesRepositoryItem newItem3 = dao.addItem(exProcessInstance, "3.txt", "3_relativePath.txt", "Description of 3.txt", "text/plain", CREATOR_LOGIN, factory);

        dao.deleteById(exProcessInstance, newItem2.getId());

        List<FilesRepositoryItem> retItems = new ArrayList<FilesRepositoryItem>(dao.getItemsFor(exProcessInstance));
        Collections.sort(retItems, new Comparator<FilesRepositoryItem>() {
            @Override
            public int compare(FilesRepositoryItem i1, FilesRepositoryItem i2) {
                return i1.getId().compareTo(i2.getId());
            }
        });

        Assert.assertEquals("Returned items after delete one element doesn't match", 2, retItems.size());
        Assert.assertEquals("Bad identifier of returned item after delete by id", retItems.toArray(new FilesRepositoryItem[]{})[0].getId(), newItem1.getId());
        Assert.assertEquals("Bad identifier of returned item after delete by id", retItems.toArray(new FilesRepositoryItem[]{})[1].getId(), newItem3.getId());
    }

    @Test
    public void testUpdateDescriptionById() {
        IFilesRepositoryItem newItem1 = dao.addItem(exProcessInstance, "1.txt", "1_relativePath.txt", "Description of 1.txt", "text/plain", CREATOR_LOGIN, factory);
        final String newDesc = "New Description for 1.txt";
        dao.updateDescription(newItem1, newDesc);
        IFilesRepositoryItem updatedNewItem1 = dao.getItemById(newItem1.getId());

        Assert.assertEquals("Updated description doesn't match with expected", newDesc, updatedNewItem1.getDescription());
    }
}
