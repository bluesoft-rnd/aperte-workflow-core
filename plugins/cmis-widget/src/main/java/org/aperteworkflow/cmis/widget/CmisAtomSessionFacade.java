package org.aperteworkflow.cmis.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.aperteworkflow.cmis.settings.CmisSettingsProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.util.lang.StringUtil;

/**
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class CmisAtomSessionFacade 
{
	/** Connection timeout in miliseconds */
	private static final String CONNECTION_TIMEOUT_MS = "3000";
	

	private Session session;

	public CmisAtomSessionFacade() 
	{
		this.session = createCmisSession();
	}
	
	/** Get folder for the given process instance */
	public Folder getFolderForProcessInstance(ProcessInstance processInstance)
	{
		ProcessInstance mainProcess = processInstance;
		
		/* If process is subprocess, get the parent */
		if(processInstance.isSubprocess())
			mainProcess = processInstance.getParent();
		
		String folderName = "PT_"+mainProcess.getId();
		Folder mainFolder = getMainFolder();
		Folder processFolder = createFolderIfNecessary(folderName, mainFolder.getPath());
		
		return processFolder;
	}
	


	public Folder getFolderById(String id) throws CmisObjectNotFoundException
	{
		return (Folder) session.getObject(session.createObjectId(id));
	}
	
	/** Find document in given folder by it's name */
	private Document getDocumentByName(Folder folder, String documentName)
	{
		for(CmisObject folderObject: folder.getChildren())
			if(folderObject instanceof Document)
			{
				Document folderDocument = (Document)folderObject;
				if(folderDocument.getName().equals(documentName))
					return folderDocument;
			}
		
		/* No document in given folder was found, return null */
		return null;
	}

	/** Uploads document to the given folder. If document with given name already exists, its content is
	 * overwrite. Otherwise, the new document is created
	 * 
	 * @param filename name of the file
	 * @param folder folder where file will be stored
	 * @param bytes content of the file
	 * @param MIMEType MIME type of the document
	 * @param newProperties custom properties
	 * @return new document or updated existing one
	 */
	public Document uploadDocument(String filename, Folder folder, byte[] bytes, String MIMEType, Map<String, String> newProperties) 
	{
		/* Create new content stream */
		ContentStream contentStream = new DocumentContentStream(bytes, MIMEType, filename);
		
		/* Get document from repostitory */
		Document document = getDocumentByName(folder, filename);
		
		/* Document already exists, update it's content */
		if(document != null)
		{
			//document.checkOut();
//			InputStream stream = new ByteArrayInputStream(bytes);
//			ContentStream contentStream2 = new ContentStreamImpl(filename, new BigInteger(bytes), MIMEType, stream);
//			document.deleteContentStream(true);
			//document.appendContentStream(contentStream, true);
			document.checkIn(true, newProperties, contentStream, "");
			//document.setContentStream(contentStream2, true, true);
		}
		/* Create new one */
		else
		{
			HashMap<String, String> properties = new HashMap<String, String>();
			properties.put(PropertyIds.NAME, filename);
			properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID);

			if(newProperties != null){
				properties.putAll(newProperties);
			}
			
			document = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
		}
		
		return document;
	}
	

	
	
	public Folder getMainFolder()
	{
		return this.session.getRootFolder();
	}
	
	public Folder createFolderIfNecessary(String name, String parentPath) 
	{
		try
		{
			Folder folder = (Folder) getObjectByPath(parentPath +
					                                         (parentPath.equals("/") ? "" : "/") +
					                                         name);
			
			return folder;
		}
		catch(CmisObjectNotFoundException ex)
		{
			Folder parent;
			if (parentPath.equals("/") || parentPath.equals("")) {
				parent = session.getRootFolder();
			}
			else {
				parent = (Folder) getObjectByPath(parentPath);
			}
			if (parent == null) {
				String[] toks = parentPath.split("/");
				StringBuilder path = new StringBuilder("/");
				for (String t : toks) {
					if (!StringUtil.hasText(t)) continue;
					parent = createFolderIfNecessary(t, path.toString());
					if (path.length() > 1) {
						path.append("/").append(t);
					}
					else {
						path.append(t);
					}
				}
			}
			Map<String, String> props = new HashMap<String, String>();
			props.put(PropertyIds.NAME, name);
			props.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.FOLDER_BASETYPE_ID);
			Folder folder = parent.createFolder(props);

			return folder;
		}
	}

	public CmisObject getObjectByPath(String path) throws CmisObjectNotFoundException
	{
		return session.getObjectByPath(path);
	}
	
	public CmisObject getObject(ObjectId objectId) 
	{
		return session.getObject(objectId);
	}
	
	private Session createCmisSession() throws CmisConnectionException
	{
		BundleContext bundleContext = FrameworkUtil.getBundle(CmisAtomSessionFacade.class).getBundleContext();
		bundleContext = FrameworkUtil.getBundle(SessionFactory.class).getBundleContext();
		ServiceReference serviceReference = bundleContext.getServiceReference(SessionFactory.class.getName());

		SessionFactory cmisSessionFactory = (SessionFactory) bundleContext.getService(serviceReference); 
		
		//SessionFactory cmisSessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		
		
		/* Get settings from database */
		String repositoryUser = CmisSettingsProvider.getAtomRepostioryUsername();
		String repositoryPassword = CmisSettingsProvider.getAtomRepostioryPassword();
		String repositoryAtomUrl = CmisSettingsProvider.getAtomRepostioryUrl();
		//String repositoryUser = CmisSettingsProvider.getAtomRepostioryUsername();

		// user credentials
		parameter.put(SessionParameter.USER, repositoryUser);
		parameter.put(SessionParameter.PASSWORD, repositoryPassword);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, repositoryAtomUrl);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.CONNECT_TIMEOUT, CONNECTION_TIMEOUT_MS);
		

		List<Repository> repositories = cmisSessionFactory.getRepositories(parameter);
		
		if(repositories.isEmpty())
			throw new RuntimeException("Repository system do not have any repository configured!");
		
		parameter.put(SessionParameter.REPOSITORY_ID, repositories.get(0).getId());
		
		return cmisSessionFactory.createSession(parameter);

	}

}
