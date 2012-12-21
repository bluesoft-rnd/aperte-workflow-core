package pl.net.bluesoft.rnd.processtool.ui.plugins;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
/**
 * @author tlipski@bluesoft.net.pl
 */
public class PluginsManagerPane extends VerticalLayout {

    //    TextField filterField = new TextField();
    Upload upload = new Upload();

    VerticalLayout bundleList = new VerticalLayout();

    public PluginsManagerPane(Application application) {

        setWidth("100%");
        setSpacing(true);

//        filterField.setWidth("100%");
//        filterField.setInputPrompt(getLocalizedMessage("plugins.console.filter.prompt"));

        upload.setButtonCaption(getLocalizedMessage("plugins.console.upload"));
        upload.setReceiver(new Upload.Receiver() {
            @Override
            public OutputStream receiveUpload(final String filename, String mimeType) {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        final byte[] bytes = toByteArray();
                        PluginManager pluginManager = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().getPluginManager();
                        pluginManager.registerPlugin(filename,
                                new ByteArrayInputStream(bytes));
                        displayBundleList();
                        String msg = getLocalizedMessage("plugins.console.upload.success");
                        Window.Notification n = new Window.Notification(msg);
                        n.setDelayMsec(-1);
                        getApplication().getMainWindow().showNotification(n);
                    }
                };
            }
        });
        upload.setImmediate(true);


        addComponent(new Label(getLocalizedMessage("plugins.console.info"), Label.CONTENT_XHTML));


        addComponent(upload);
        setComponentAlignment(upload, Alignment.BOTTOM_RIGHT);

        bundleList.setSpacing(true);

        addComponent(width(horizontalLayout(
                refreshIcon(application, new Refreshable() {
                    @Override
                    public void refreshData() {
                        displayBundleList();
                    }
                }),
                styled(new Label(getLocalizedMessage("plugins.console.title")), "h2")),
                null));

        addComponent(bundleList);
        displayBundleList();

    }

    private void displayBundleList() {
        bundleList.removeAllComponents();
        final PluginManager pluginManager = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().getPluginManager();
        List<PluginMetadata> registeredPlugins = new ArrayList<PluginMetadata>(pluginManager.getRegisteredPlugins());
        Collections.sort(registeredPlugins);

        for (final PluginMetadata metadata : registeredPlugins) {
            HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setSpacing(true);

            if (metadata.isCanEnable()) {
                buttonLayout.addComponent(linkButton(getLocalizedMessage("plugins.console.enable"), new Runnable() {
                    @Override
                    public void run() {
                        ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().getPluginManager().enablePlugin(metadata);
                        String msg = getLocalizedMessage("plugins.console.enable.success");
                                                Window.Notification n = new Window.Notification(msg);
                                                n.setDelayMsec(-1);
                                                getApplication().getMainWindow().showNotification(n);
                        displayBundleList();
                    }
                }));
            }
            if (metadata.isCanDisable()) {
                buttonLayout.addComponent(linkButton(getLocalizedMessage("plugins.console.disable"), new Runnable() {
                    @Override
                    public void run() {
                        ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().getPluginManager().disablePlugin(metadata);
                        String msg = getLocalizedMessage("plugins.console.disable.success");
                        Window.Notification n = new Window.Notification(msg);
                        n.setDelayMsec(-1);
                        getApplication().getMainWindow().showNotification(n);
                        displayBundleList();

                    }
                }));
            }
            if (metadata.isCanUninstall()) {
                buttonLayout.addComponent(linkButton(getLocalizedMessage("plugins.console.uninstall"), new Runnable() {
                    @Override
                    public void run() {
                        ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().getPluginManager().uninstallPlugin(metadata);
                        String msg = getLocalizedMessage("plugins.console.uninstall.success");
                        Window.Notification n = new Window.Notification(msg);
                        n.setDelayMsec(-1);
                        getApplication().getMainWindow().showNotification(n);                        
                        displayBundleList();
                    }
                }));
            }

//            for (int i =0; i < buttonLayout.getComponentCount(); i++) {
//                buttonLayout.setComponentAlignment(buttonLayout.getComponent(i), Alignment.BOTTOM_LEFT);
//            }
            if (metadata.getHomepageUrl() != null) {
                Link c = new Link(getLocalizedMessage("plugins.console.plugin.homepage"), new ExternalResource( metadata.getHomepageUrl()));
                c.setTargetName("_blank");
                buttonLayout.addComponent(c);
            }
            if (metadata.getDocumentationUrl() != null) {
                Link c = new Link(getLocalizedMessage("plugins.console.plugin.documentation"),
                        new ExternalResource( metadata.getDocumentationUrl()));
                c.setTargetName("_blank");
                buttonLayout.addComponent(c);
            }
            bundleList.addComponent(verticalLayout(
                    styled(new Label(metadata.getId() + ": " + metadata.getName() + " (" + metadata.getVersion() + ")"), "h2"),
                    styled(new Label(getLocalizedMessage("plugins.console.status") +
                            " " +
                            getLocalizedMessage(metadata.getStateDescription())), "small"),
                    new Label(metadata.getDescription()),
                    buttonLayout
            ));
        }
    }
}
