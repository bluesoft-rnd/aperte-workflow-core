package org.aperteworkflow.widgets.doclist;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "CmisDocumentList")
public class DocumentListWidget extends BaseProcessToolVaadinWidget implements ProcessToolDataWidget {

	@AutoWiredProperty
	private String rootFolderPath = "/docs";

	@AutoWiredProperty
	private String subFolder = "subFolder";

	@AutoWiredProperty
	private String newFolderPrefix = "/aw_";

	@AutoWiredProperty
	private String mode = "normal";
	
	@AutoWiredProperty
	private String hideMatching = null;

	@AutoWiredProperty
	private boolean required = false;

	private String folderId;
	private String login;
    
    private DocumentRepository repo;
    private String path;

	@Override
	public void setContext(ProcessStateConfiguration state, ProcessStateWidget configuration, I18NSource i18NSource,
			ProcessToolBpmSession bpmSession, Application application, Set<String> permissions, boolean isOwner) {
		super.setContext(state, configuration, i18NSource, bpmSession, application, permissions, isOwner);
		login = bpmSession.getUserLogin();
	}

	@Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {

		if (required && !repo.getEntries(path).iterator().hasNext()) {
			return Arrays.asList("pt.ext.cmis.list.document.validate.upload.document");
		} else {
			return null;
		}
	}

	@Override
	public void saveData(BpmTask task) {
		// setSimpleAttribute(folderAttributeName, folderId, processInstance);
	}

	@Override
	public void loadData(BpmTask task) {

        String path = rootFolderPath + newFolderPrefix + task.getProcessInstance().getInternalId();
		if (StringUtil.hasText(subFolder))
			path = path + subFolder;
	}

	VerticalLayout vl;
    private Component documentListComponent;

	@Override
	public Component render() {
//		vl = new VerticalLayout();
//        Button refreshDocumentList = new Button(getMessage("pt.ext.cmis.list.refresh"));
//        refreshDocumentList.setIcon(new ClassResource(CmisDocumentListWidget.class, "/img/load-repository.png", getApplication()));
//        refreshDocumentList.setImmediate(true);
//        refreshDocumentList.setStyleName(BaseTheme.BUTTON_LINK);
//        refreshDocumentList.addListener(new ClickListener() {
//            @Override
//            public void buttonClick(ClickEvent event) {
//                reload();
//            }
//        });
//        vl.addComponent(refreshDocumentList);
//		reload();
//		return vl;
        return null;
	}

	private void reload() {
        if (documentListComponent != null) {
            vl.removeComponent(documentListComponent);
        }
        if (hasPermission("EDIT", "VIEW")) {
//            vl.addComponent(documentListComponent = getDocumentList());
        }
    }

//	private Component getDocumentList() {
//		VerticalLayout vl = new VerticalLayout();
//		vl.setWidth("100%");
//		ItemIterable<CmisObject> cmisObjectItemIterable = mainFolder.getChildren();
//		boolean hasAnyDocuments = false;
//		for (CmisObject co : cmisObjectItemIterable) {
//			if (co instanceof Document) {
//				hasAnyDocuments = true;
//				String name = co.getName();
//				boolean popup = false;
//
//				if (name.contains("__POPUP_ONCE__")) {
//					popup = true;
//					name = name.replaceFirst("__POPUP_ONCE__", "");
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put(PropertyIds.NAME, name);
//					co.updateProperties(map, true);
//				} else if (name.contains("__POPUP_ALWAYS__")) {
//					popup = true;
//					name = name.replaceFirst("__POPUP_ALWAYS__", "");
//				}
//
//				if(hideMatching != null && !"".equals(hideMatching) && name.matches(".*"+hideMatching+".*")){
//					continue;
//				}
//
//				final Document doc = (Document) co;
//				HorizontalLayout hl = new HorizontalLayout();
//				hl.setWidth("100%");
//				hl.setSpacing(true);
//				Label label = new Label(name);
//				label.setWidth("100%");
//				hl.addComponent(label);
//				hl.setExpandRatio(label, 1.0f);
//				final StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
//					@Override
//					public InputStream getStream() {
//						ContentStream cs = doc.getContentStream();
//						return cs.getStream();
//					}
//				}, name, getApplication());
//
//				if (popup) {
//					getApplication().getMainWindow().open(resource, "_blank");
//				}
//
//				Link downloadLink = new Link(getI18NSource().getMessage("pt.ext.cmis.list.document.download"), resource);
//				downloadLink.setTargetName("_blank");
//
//				hl.addComponent(downloadLink);
//				if (hasPermission("EDIT")) {
//					Upload upload = new Upload();
//					upload.setButtonCaption(getI18NSource().getMessage("pt.ext.cmis.list.update.button"));
//					upload.setReceiver(new UpdateReceiver(doc));
//					upload.setImmediate(true);
//
//					hl.addComponent(upload);
//
//				}
//				vl.addComponent(hl);
//			}
//			if ("simple".equalsIgnoreCase(mode)) {
//				break;
//			}
//
//		}
//
//		if (!hasAnyDocuments) {
//			vl.addComponent(new Label(getI18NSource().getMessage("pt.ext.cmis.list.no-documents")));
//		}
//		if (hasPermission("EDIT")) {
//			if ("normal".equalsIgnoreCase(mode) || !hasAnyDocuments) {
//				vl.addComponent(new Label(getI18NSource().getMessage("pt.ext.cmis.list.upload")));
//				Upload upload = new Upload();
//				upload.setImmediate(true);
//				upload.setButtonCaption(getI18NSource().getMessage("pt.ext.cmis.list.upload.button"));
//				upload.setReceiver(new Upload.Receiver() {
//					@Override
//					public OutputStream receiveUpload(final String filename, final String MIMEType) {
//						return new ByteArrayOutputStream() {
//							@Override
//							public void close() throws IOException {
//								super.close();
//								sessionFacade.uploadDocument(filename, mainFolder, toByteArray(), MIMEType, null);
//								reload();
//							}
//						};
//					}
//				});
//				vl.addComponent(upload);
//			}
//		}
//
//		return vl;
//	}
//
	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("not supported");
	}
//
//	public String getRepositoryAtomUrl() {
//		return repositoryAtomUrl;
//	}
//
//	public void setRepositoryAtomUrl(String repositoryAtomUrl) {
//		this.repositoryAtomUrl = repositoryAtomUrl;
//	}
//
//	public String getRepositoryId() {
//		return repositoryId;
//	}
//
//	public void setRepositoryId(String repositoryId) {
//		this.repositoryId = repositoryId;
//	}
//
//	public String getRepositoryUser() {
//		return repositoryUser;
//	}
//
//	public void setRepositoryUser(String repositoryUser) {
//		this.repositoryUser = repositoryUser;
//	}
//
//	public String getRepositoryPassword() {
//		return repositoryPassword;
//	}
//
//	public void setRepositoryPassword(String repositoryPassword) {
//		this.repositoryPassword = repositoryPassword;
//	}
//
//	public String getRootFolderPath() {
//		return rootFolderPath;
//	}
//
//	public void setRootFolderPath(String rootFolderPath) {
//		this.rootFolderPath = rootFolderPath;
//	}
//
//	public String getNewFolderPrefix() {
//		return newFolderPrefix;
//	}
//
//	public void setNewFolderPrefix(String newFolderPrefix) {
//		this.newFolderPrefix = newFolderPrefix;
//	}
//
//	public boolean isRequired() {
//		return required;
//	}
//
//	public void setRequired(boolean required) {
//		this.required = required;
//	}
//
//	public String getSubFolder() {
//		return subFolder;
//	}
//
//	public void setSubFolder(String subFolder) {
//		this.subFolder = subFolder;
//	}
//
//	public String getMode() {
//		return mode;
//	}
//
//	public void setMode(String mode) {
//		this.mode = mode;
//	}
//
//	public String getFolderId() {
//		return folderId;
//	}
//
//	public void setFolderId(String folderId) {
//		this.folderId = folderId;
//	}
//
//	private final class UpdateReceiver implements Upload.Receiver {
//		private final Document doc;
//
//		private UpdateReceiver(Document doc) {
//			this.doc = doc;
//		}
//
//		@Override
//		public OutputStream receiveUpload(final String filename, final String MIMEType) {
//			return new ByteArrayOutputStream() {
//				@Override
//				public void close() throws IOException {
//					super.close();
//					final byte[] bytes = toByteArray();
//					Map<String, String> properties = new HashMap();
//					properties.put(PropertyIds.NAME, filename);
//					properties.put(PropertyIds.LAST_MODIFIED_BY, login);
//					ContentStream contentStream = new ContentStream() {
//
//						@Override
//						public long getLength() {
//							return bytes.length;
//						}
//
//						@Override
//						public BigInteger getBigLength() {
//							return BigInteger.valueOf(bytes.length);
//						}
//
//						@Override
//						public String getMimeType() {
//							return MIMEType;
//						}
//
//						@Override
//						public String getFileName() {
//							return filename;
//						}
//
//						@Override
//						public InputStream getStream() {
//							return new ByteArrayInputStream(bytes);
//						}
//
//						@Override
//						public List<CmisExtensionElement> getExtensions() {
//							return null;
//						}
//
//						@Override
//						public void setExtensions(List<CmisExtensionElement> extensions) {
//
//						}
//					};
//					// doc.checkOut();
//					doc.setContentStream(contentStream, true);
//					doc.checkIn(true, properties, contentStream, "");
//					reload();
//				}
//			};
//		}
//	}

	public void setHideMatching(String hideMatching) {
		this.hideMatching = hideMatching;
	}
}
