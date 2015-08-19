package pl.net.bluesoft.casemanagement.dao;

import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;
import pl.net.bluesoft.casemanagement.model.FilesRepositoryCaseAttribute;

/**
 * Created by pkuciapski on 2014-05-14.
 */
public class FilesRepositoryCaseAttributeFactoryImpl extends FilesRepositoryAttributeFactory {
	public static final FilesRepositoryCaseAttributeFactoryImpl INSTANCE = new FilesRepositoryCaseAttributeFactoryImpl();

    @Override
    public IFilesRepositoryAttribute create() {
        return new FilesRepositoryCaseAttribute();
    }
}
