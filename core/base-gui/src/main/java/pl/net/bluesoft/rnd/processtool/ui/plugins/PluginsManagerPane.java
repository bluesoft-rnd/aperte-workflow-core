package pl.net.bluesoft.rnd.processtool.ui.plugins;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.plugins.PluginInformation;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.*;

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
                        PluginManager pluginManager = ProcessToolContext.Util.getProcessToolContextFromThread().getRegistry().getPluginManager();
                        pluginManager.registerPlugin(filename,
                                new ByteArrayInputStream(bytes));
                        displayBundleList();
                        getApplication().getMainWindow().showNotification(getLocalizedMessage("plugins.console.upload.success"));
                    }
                };
            }
        });
        upload.setImmediate(true);


        addComponent(new Label(getLocalizedMessage("plugins.console.info"), Label.CONTENT_XHTML));


        addComponent(upload);
        setComponentAlignment(upload, Alignment.BOTTOM_RIGHT);

        bundleList.setSpacing(true);

        addComponent(horizontalLayout(
                styled(new Label(getLocalizedMessage("plugins.console.title")), "h2"),
                refreshIcon(application, new HasRefreshButton() {
                    @Override
                    public void refreshData() {
                        displayBundleList();
                    }
                })));
        addComponent(bundleList);
        displayBundleList();

    }

    private void displayBundleList() {
        bundleList.removeAllComponents();
        final PluginManager pluginManager = ProcessToolContext.Util.getProcessToolContextFromThread().getRegistry().getPluginManager();
        List<PluginInformation> registeredPlugins = new ArrayList<PluginInformation>(pluginManager.getRegisteredPlugins());
        Collections.sort(registeredPlugins);

        for (final PluginInformation pi : registeredPlugins) {
            HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setSpacing(true);

            if (pi.isCanEnable()) {
                buttonLayout.addComponent(linkButton(getLocalizedMessage("plugins.console.enable"), new Runnable() {
                    @Override
                    public void run() {
                        ProcessToolContext.Util.getProcessToolContextFromThread().getRegistry().getPluginManager().enablePlugin(pi);
                        getApplication().getMainWindow().showNotification(getLocalizedMessage("plugins.console.enable.success"));
                        displayBundleList();
                    }
                }));
            }
            if (pi.isCanDisable()) {
                buttonLayout.addComponent(linkButton(getLocalizedMessage("plugins.console.disable"), new Runnable() {
                    @Override
                    public void run() {
                        ProcessToolContext.Util.getProcessToolContextFromThread().getRegistry().getPluginManager().disablePlugin(pi);
                        getApplication().getMainWindow().showNotification(getLocalizedMessage("plugins.console.disable.success"));
                        displayBundleList();

                    }
                }));
            }
            if (pi.isCanUninstall()) {
                buttonLayout.addComponent(linkButton(getLocalizedMessage("plugins.console.uninstall"), new Runnable() {
                    @Override
                    public void run() {
                        ProcessToolContext.Util.getProcessToolContextFromThread().getRegistry().getPluginManager().uninstallPlugin(pi);
                        getApplication().getMainWindow().showNotification(getLocalizedMessage("plugins.console.uninstall.success"));
                        displayBundleList();
                    }
                }));
            }

//            for (int i =0; i < buttonLayout.getComponentCount(); i++) {
//                buttonLayout.setComponentAlignment(buttonLayout.getComponent(i), Alignment.BOTTOM_LEFT);
//            }
            if (pi.getHomepageUrl() != null) {
                Link c = new Link(getLocalizedMessage("plugins.console.plugin.homepage"), new ExternalResource(pi.getHomepageUrl()));
                c.setTargetName("_blank");
                buttonLayout.addComponent(c);
            }
            if (pi.getDocumentationUrl() != null) {
                Link c = new Link(getLocalizedMessage("plugins.console.plugin.documentation"),
                        new ExternalResource(pi.getDocumentationUrl()));
                c.setTargetName("_blank");
                buttonLayout.addComponent(c);
            }
            bundleList.addComponent(verticalLayout(
                    styled(new Label(pi.getId() + ": " + pi.getName() + " (" + pi.getVersion() + ")"), "h2"),
                    styled(new Label(getLocalizedMessage("plugins.console.status") +
                            " " +
                            getLocalizedMessage(pi.getStatusDescription())), "small"),
                    new Label(pi.getDescription()),
                    buttonLayout
            ));
        }
    }
}
