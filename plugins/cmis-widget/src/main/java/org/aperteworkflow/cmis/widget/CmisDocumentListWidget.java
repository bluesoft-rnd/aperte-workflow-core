package org.aperteworkflow.cmis.widget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "CmisDocumentList")
@WidgetGroup("cmis-widget")
public class CmisDocumentListWidget extends BaseProcessToolVaadinWidget implements ProcessToolDataWidget, ProcessToolVaadinRenderable {

    private static Logger logger = Logger.getLogger(CmisDocumentListWidget.class.getName());

	@AutoWiredProperty
	private String newFolderPrefix = "pt_";

	@AutoWiredProperty
	private String mode = "normal";
	
	@AutoWiredProperty
	private String hideMatching = null;

	@AutoWiredProperty
	private boolean required = false;

	private String folderId;
	private CmisAtomSessionFacade sessionFacade;
	private Folder mainFolder;
	private String login;
	
	private String errorMessage = null;

	@Override
	public void setContext(ProcessStateConfiguration state, ProcessStateWidget configuration, I18NSource i18NSource,
			ProcessToolBpmSession bpmSession, Application application, Set<String> permissions, boolean isOwner) {
		super.setContext(state, configuration, i18NSource, bpmSession, application, permissions, isOwner);
		login = bpmSession.getUserLogin();
	}

	@Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {
		if (required && !mainFolder.getChildren().iterator().hasNext()) {
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
	public void loadData(BpmTask task) 
	{
		try
		{
			sessionFacade = new CmisAtomSessionFacade();
		
			/* Get folder for the current process instance */
			mainFolder = sessionFacade.getFolderForProcessInstance(task.getProcessInstance());

			folderId = mainFolder.getId();
		}
		catch(CmisConnectionException ex)
		{
			errorMessage = "Couldn't connect to Alfresco server: "+ex.getMessage();
			logger.log(Level.SEVERE, errorMessage, ex);
		}
	}
	


	VerticalLayout vl;
    private Component documentListComponent;

	@Override
	public Component render() 
	{
		vl = new VerticalLayout();
		
		if(errorMessage != null)
		{
			vl.addComponent(new Label(errorMessage));
			return vl;
		}

        Button refreshDocumentList = new Button(getMessage("pt.ext.cmis.list.refresh"));
        refreshDocumentList.setIcon(new ClassResource(CmisDocumentListWidget.class, "/img/load-repository.png", getApplication()));
        refreshDocumentList.setImmediate(true);
        refreshDocumentList.setStyleName(BaseTheme.BUTTON_LINK);
        refreshDocumentList.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                reload();
            }
        });
        vl.addComponent(refreshDocumentList);
        vl.setSizeFull();
		reload();
		return vl;
	}

	private void reload() {
        if (documentListComponent != null) {
            vl.removeComponent(documentListComponent);
        }
        if (hasPermission("EDIT", "VIEW")) {
            vl.addComponent(documentListComponent = getDocumentList());
        }
    }

	private Component getDocumentList() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		
		/* Refresh content */
		mainFolder.refresh();
		
		ItemIterable<CmisObject> cmisObjectItemIterable = mainFolder.getChildren();
		boolean hasAnyDocuments = false;
		
		for (CmisObject co : cmisObjectItemIterable) 
		{
			if (co instanceof Document) 
			{
				hasAnyDocuments = true;
				String name = co.getName();
				boolean popup = false;
				
				if (name.contains("__POPUP_ONCE__")) {
					popup = true;
					name = name.replaceFirst("__POPUP_ONCE__", "");
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(PropertyIds.NAME, name);
					co.updateProperties(map, true);
				} else if (name.contains("__POPUP_ALWAYS__")) {
					popup = true;
					name = name.replaceFirst("__POPUP_ALWAYS__", "");
				}
				
				if(hideMatching != null && !"".equals(hideMatching) && name.matches(".*"+hideMatching+".*")){
					continue;
				}

				final Document doc = (Document) co;
				HorizontalLayout hl = new HorizontalLayout();
				hl.setWidth("100%");
				hl.setSpacing(true);
				
				Date lastModificationDate = co.getLastModificationDate().getTime();
				String lastModificationString = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(lastModificationDate);
				
				/* Create file name */
				StringBuilder fileNameBuilder = new StringBuilder();
				fileNameBuilder.append(name);
				fileNameBuilder.append(" [");
				fileNameBuilder.append(getI18NSource().getMessage("pt.ext.cmis.list.last.modification.date", "", lastModificationString));
				fileNameBuilder.append("]");
				
				Label label = new Label(fileNameBuilder.toString());
				label.setWidth("100%");
				hl.addComponent(label);
				hl.setExpandRatio(label, 1.0f);
				final StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
					@Override
					public InputStream getStream() {
						ContentStream cs = doc.getContentStream();
						return cs.getStream();
					}
				}, name, getApplication());

				if (popup) {
					getApplication().getMainWindow().open(resource, "_blank");
				}

				Link downloadLink = new Link(getI18NSource().getMessage("pt.ext.cmis.list.document.download"), resource);
				downloadLink.setTargetName("_blank");

				hl.addComponent(downloadLink);
				if (hasPermission("EDIT")) {
					Upload upload = new Upload();
					upload.setButtonCaption(getI18NSource().getMessage("pt.ext.cmis.list.update.button"));
					upload.setReceiver(new UpdateReceiver(doc));
					upload.setImmediate(true);

					hl.addComponent(upload);

				}
				vl.addComponent(hl);
			}
			if ("simple".equalsIgnoreCase(mode)) {
				break;
			}

		}

		if (!hasAnyDocuments) {
			vl.addComponent(new Label(getI18NSource().getMessage("pt.ext.cmis.list.no-documents")));
		}
		if (hasPermission("EDIT")) {
			if ("normal".equalsIgnoreCase(mode) || !hasAnyDocuments) {
				vl.addComponent(new Label(getI18NSource().getMessage("pt.ext.cmis.list.upload")));
				Upload upload = new Upload();
				upload.setImmediate(true);
				upload.setButtonCaption(getI18NSource().getMessage("pt.ext.cmis.list.upload.button"));
				upload.setReceiver(new Upload.Receiver() {
					@Override
					public OutputStream receiveUpload(final String filename, final String MIMEType) {
						return new ByteArrayOutputStream() {
							@Override
							public void close() throws IOException {
								super.close();
								sessionFacade.uploadDocument(filename, mainFolder, toByteArray(), MIMEType, null);
								reload();
							}
						};
					}
				});
				vl.addComponent(upload);
			}
		}

		return vl;
	}

	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("not supported");
	}



	public String getNewFolderPrefix() {
		return newFolderPrefix;
	}

	public void setNewFolderPrefix(String newFolderPrefix) {
		this.newFolderPrefix = newFolderPrefix;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	private final class UpdateReceiver implements Upload.Receiver {
		private final Document doc;

		private UpdateReceiver(Document doc) {
			this.doc = doc;
		}

		@Override
		public OutputStream receiveUpload(final String filename, final String MIMEType) {
			return new ByteArrayOutputStream() {
				@Override
				public void close() throws IOException {
					super.close();
					final byte[] bytes = toByteArray();
					Map<String, String> properties = new HashMap<String, String>();
					properties.put(PropertyIds.NAME, filename);
					properties.put(PropertyIds.LAST_MODIFIED_BY, login);
					ContentStream contentStream = new ContentStream() {

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
					};
					// doc.checkOut();
					doc.setContentStream(contentStream, true);
					doc.checkIn(true, properties, contentStream, "");
					reload();
				}
			};
		}
	}

	public void setHideMatching(String hideMatching) {
		this.hideMatching = hideMatching;
	}
}
