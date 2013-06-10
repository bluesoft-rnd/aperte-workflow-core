package org.aperteworkflow.editor.ui.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.aperteworkflow.editor.domain.Queue;
import org.aperteworkflow.editor.domain.QueueRolePermission;
import org.aperteworkflow.editor.vaadin.DataHandler;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class SingleQueueEditor extends GridLayout implements QueueRolePermissionBoxHandler, DataHandler {

    private static final Logger logger = Logger.getLogger(SingleQueueEditor.class.getName());
    
    private Queue queue;
    private QueueHandler handler;
    private TextField queueDescriptionField;
    private Label queueNameLabel;
    private Button removeQueueButton;
    private Label roleNameDescription;
    private RoleNameComboBox roleNameComboBox;
    private CssLayout rolePermissionLayout;
    
    public SingleQueueEditor(Queue queue, QueueHandler handler) {
        super(2, 4);
        this.handler = handler;
        this.queue = queue;
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
        queueDescriptionField = new TextField();
        queueDescriptionField.setWidth("75%");
        queueDescriptionField.setNullRepresentation("");
        queueDescriptionField.setInputPrompt(messages.getMessage("queue.editor.description.prompt"));
        queueDescriptionField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                queue.setDescription((String) queueDescriptionField.getValue());
            }
        });

        roleNameComboBox = new RoleNameComboBox();
        roleNameComboBox.setHandler(this);

        queueNameLabel = new Label("<h2>" + queue.getName() + "</h2>");
        queueNameLabel.setContentMode(Label.CONTENT_XHTML);
        
        removeQueueButton = VaadinUtility.button(messages.getMessage("  X  "), new Runnable() {
            @Override
            public void run() {
                handler.removeQueue(queue);
            }
        });
        
        roleNameDescription = new Label(messages.getMessage("queue.editor.assigned.roles"));
        
        rolePermissionLayout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof QueueRolePermissionBox) {
                    String basicCss = "float: left; margin: 3px; margin-bottom: 8px; padding: 3px; display: inline; font-weight: bold; border: 2px solid ";
                    return basicCss + "#287ece; -moz-border-radius: 5px; border-radius: 5px; padding-left: 6px; padding-right: 6px;";
                }
                return super.getCss(c);
            }
        };
        rolePermissionLayout.setWidth("100%");
    }

    private void initLayout() {
        setSpacing(true);
        setWidth("100%");

        addComponent(queueNameLabel, 0, 0);
        addComponent(queueDescriptionField, 0, 1);
        addComponent(removeQueueButton, 1, 0, 1, 1);
        addComponent(roleNameDescription, 0, 2);
        addComponent(roleNameComboBox, 1, 2);
        addComponent(rolePermissionLayout, 0, 3, 1, 3);

        setComponentAlignment(removeQueueButton, Alignment.TOP_RIGHT);
        setComponentAlignment(roleNameDescription, Alignment.MIDDLE_LEFT);
        setComponentAlignment(roleNameComboBox, Alignment.MIDDLE_RIGHT);

        setColumnExpandRatio(0, 1);
        setColumnExpandRatio(1, 0);
    }

    @Override
    public void addQueueRolePermissionBox(QueueRolePermissionBox box) {
        rolePermissionLayout.addComponent(box);
        
        if (queue.getRolePermissions() == null) {
            queue.setRolePermissions(new ArrayList<QueueRolePermission>());
        }
        if (!queue.getRolePermissions().contains(box.getQueueRolePermission())) {
            queue.getRolePermissions().add(box.getQueueRolePermission());
        }
        
        String roleName = box.getQueueRolePermission().getRoleName();
        if (roleNameComboBox.containsId(roleName)) {
            roleNameComboBox.removeItem(roleName);
        }
    }

    @Override
    public void removeQueueRolePermissionBox(QueueRolePermissionBox box) {
        rolePermissionLayout.removeComponent(box);

        roleNameComboBox.addItem(box.getQueueRolePermission().getRoleName());
        
        if (queue.getRolePermissions() != null) {
            queue.getRolePermissions().remove(box.getQueueRolePermission());
        }
    }

    @Override
    public void loadData() {
        roleNameComboBox.removeAllItems();
        roleNameComboBox.addItem(".*");
        
        IUserRolesManager rolesManager = ObjectFactory.create(IUserRolesManager.class);
		for (String roleName : rolesManager.getAllRolesNames()) {
			roleNameComboBox.addItem(roleName);
		}

        if (queue.getDescription() != null && !queue.getDescription().trim().isEmpty()) {
            queueDescriptionField.setValue(queue.getDescription());
        }
        
        if (queue.getRolePermissions() != null && !queue.getRolePermissions().isEmpty())  {
            for (QueueRolePermission rolePermission : queue.getRolePermissions()) {
                QueueRolePermissionBox box = new QueueRolePermissionBox(rolePermission, this);
                addQueueRolePermissionBox(box);
            }
        }
    }

    @Override
    public void saveData() {
//        List<QueueRolePermission> permissions = new ArrayList<QueueRolePermission>();
//
//        Iterator<Component> it = rolePermissionLayout.getComponentIterator();
//        while (it.hasNext()) {
//            Component c = it.next();
//            if (c instanceof QueueRolePermissionBox) {
//                QueueRolePermissionBox box = (QueueRolePermissionBox) c;
//                QueueRolePermission permission = box.getQueueRolePermission();
//                permissions.add(permission);
//            }
//        }
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
}
