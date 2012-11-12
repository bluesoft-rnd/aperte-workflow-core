package pl.net.bluesoft.rnd.pt.utils.cmis;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import pl.net.bluesoft.util.lang.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class CmisAtomSessionFacade {
	private String repositoryUser;
	private String repositoryPassword;
	private String repositoryAtomUrl;
	private String repositoryId;
	private Session session;

	public CmisAtomSessionFacade(String repositoryUser, String repositoryPassword, String repositoryAtomUrl, String repositoryId) {
		this.repositoryUser = repositoryUser;
		this.repositoryPassword = repositoryPassword;
		this.repositoryAtomUrl = repositoryAtomUrl;
		this.repositoryId = repositoryId;

		this.session = createCmisSession();
	}

	public Folder getFolderById(String id) {
		try {
			return (Folder) session.getObject(session.createObjectId(id));
		}
		catch (CmisObjectNotFoundException e) { //great idea Chemistry developers - use RUNTIME exceptions to control the flow
			return null;
		}
	}

	public Document uploadDocument(final String filename, Folder folder, final byte[] bytes, final String MIMEType, Map<String, String> newProperties) {
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(PropertyIds.NAME, filename);
		properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID);
		if(newProperties != null){
			properties.putAll(newProperties);
		}

		return folder.createDocument(properties, new ContentStream() {

			@Override
			public long getLength() {
				return bytes.length;
			}

			@Override
			public BigInteger getBigLength() {
				return BigInteger.valueOf(bytes.length);
			}

			@Override
			public String getMimeType() {
				return MIMEType;
			}

			@Override
			public String getFileName() {
				return filename;
			}

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(bytes);
			}

			@Override
			public List<CmisExtensionElement> getExtensions() {
				return null;
			}

			@Override
			public void setExtensions(List<CmisExtensionElement> extensions) {

			}
		},
		VersioningState.MAJOR);
	}
	public Folder createFolderIfNecessary(String name, String parentPath) {
		Folder folder = (Folder) getObjectByPath(parentPath +
				                                         (parentPath.equals("/") ? "" : "/") +
				                                         name);
		if (folder == null) {
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
			HashMap props = new HashMap();
			props.put(PropertyIds.NAME, name);
			props.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.FOLDER_BASETYPE_ID);
			folder = parent.createFolder(props);
		}
		return folder;
	}

	public CmisObject getObjectByPath(String path) {
		try {
			return session.getObjectByPath(path);
		}
		catch (CmisObjectNotFoundException e) { //great idea Chemistry developers - use RUNTIME exceptions to control the flow
			return null;
		}
	}
	
	public CmisObject getObject(ObjectId objectId) {
		try {
			return session.getObject(objectId);
		}
		catch (CmisObjectNotFoundException e) { //great idea Chemistry developers - use RUNTIME exceptions to control the flow
			return null;
		}
	}
	
	private Session createCmisSession() {
		SessionFactory cmisSessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, repositoryUser);
		parameter.put(SessionParameter.PASSWORD, repositoryPassword);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, repositoryAtomUrl);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, repositoryId);

		return cmisSessionFactory.createSession(parameter);
	}

}
