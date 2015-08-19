package pl.net.bluesoft.casemanagement.dao;

import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;
import pl.net.bluesoft.casemanagement.model.FilesRepositoryCaseStageAttribute;

/**
 * Created by Dominik Debowczyk on 2015-08-14.
 */
public class FilesRepositoryCaseStageAttributeFactoryImpl extends FilesRepositoryAttributeFactory{
    public static final FilesRepositoryAttributeFactory INSTANCE = new FilesRepositoryCaseStageAttributeFactoryImpl();
    @Override
    public IFilesRepositoryAttribute create() {
        return new FilesRepositoryCaseStageAttribute();
    }
}