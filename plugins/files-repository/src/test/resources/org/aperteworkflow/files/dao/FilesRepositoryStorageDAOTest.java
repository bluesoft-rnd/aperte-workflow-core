package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.dao.config.PropertiesFilesRepositoryStorageConfig;
import org.junit.*;
import pl.net.bluesoft.util.lang.Classes;

import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryStorageDAOTest {
    private static final Logger logger = Logger.getLogger(FilesRepositoryStorageDAOTest.class.getName());

    private static Properties properties;
    private static FilesRepositoryStorageConfig config;

    private FilesRepositoryStorageDAO dao;

    public static Properties getProperties() {
        return properties;
    }

    @BeforeClass
    public static void beforeClass() throws NamingException {
        // Load properties
        properties = Classes.loadProperties(FilesRepositoryStorageDAOTest.class, "test.plugin.properties");
        config = new PropertiesFilesRepositoryStorageConfig(getProperties());
    }

    @Before
    public void setUp() throws NamingException {
        dao = new FilesRepositoryStorageDAOImpl();
    }

    @Test
    public void testLoadConfig() {
        String path = config.getStorageRootDirPath();
        Assert.assertNotNull(path);
        Assert.assertTrue(path.length() > 0);
    }

}
