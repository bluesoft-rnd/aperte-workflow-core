package pl.net.bluesoft.rnd.processtool.ui.admin;

import com.vaadin.Application;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.CriteriaConfigurer;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.service.ProcessToolUserService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

public class UserSearchForm extends VerticalLayout {

    private Application application = null;
    private AdminTaskTableItem taskTableItem = null;
    private ProcessToolBpmSession bpmSession;
    private I18NSource i18N;
    private Logger logger = Logger.getLogger(UserSearchForm.class.getName());

    private TextField emailField = new TextField();
    private TextField nameField = new TextField();
    private TextField surnameField = new TextField();
    private TextField mpkIdField = new TextField();
    private TextField companyIdField = new TextField();
    private Select users = new Select();
    private AdminMainPane adminMainPane;
    private static final String FIELD_WIDTH = "150px";

    public UserSearchForm(Application app, ProcessToolBpmSession session, I18NSource i18NSource, AdminTaskTableItem item, AdminMainPane amp) {

        application = app;
        bpmSession = session;
        taskTableItem = item;
        i18N = i18NSource;
        adminMainPane = amp;
        //setCaption(getMessage("admin.selectUser"));
        setSpacing(true);

        HorizontalLayout hLay = new HorizontalLayout();

        emailField.setCaption(getMessage("admin.email"));
        emailField.setWidth(FIELD_WIDTH);
        hLay.addComponent(emailField);

        nameField.setCaption(getMessage("admin.name"));
        nameField.setWidth(FIELD_WIDTH);
        hLay.addComponent(nameField);

        surnameField.setCaption(getMessage("admin.surname"));
        surnameField.setWidth(FIELD_WIDTH);
        hLay.addComponent(surnameField);

        mpkIdField.setCaption(getMessage("admin.mpkId"));
        mpkIdField.setWidth(FIELD_WIDTH);
        hLay.addComponent(mpkIdField);

        companyIdField.setCaption(getMessage("admin.companyId"));
        companyIdField.setWidth(FIELD_WIDTH);
        hLay.addComponent(companyIdField);

        hLay.setSpacing(true);
        addComponent(hLay);

        Button searchBtn = VaadinUtility.button(getMessage("admin.assign.search"), null, null);
        searchBtn.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                refreshUserSelect();
            }
        });
        addComponent(searchBtn);

        refreshUserSelect();
        addComponent(users);

        Button assignBtn = VaadinUtility.button(getMessage("admin.assign.assign"), null, null);
        assignBtn.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                UserData ud = (UserData) users.getValue();
                if (ud == null) {
                    application.getMainWindow().showNotification(getMessage("admin.selectUser"));
                    return;
                }

                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

                bpmSession.assignTaskToUser(ctx, taskTableItem.getId(), ud.getLogin());
                application.getMainWindow().showNotification(getMessage("admin.assign.performed"));
                adminMainPane.closeModalWindow();
            }
        });

        addComponent(assignBtn);
    }

    private String getMessage(String key) {
        return i18N.getMessage(key);
    }

    private void refreshUserSelect() {
        users.removeAllItems();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessToolUserService srv = ctx.getRegistry().getRegisteredService(ProcessToolUserService.class);

        List<UserData> tetaUsers = srv.findUsersByCriteria(getCriteriaConfigurer(
                (String) emailField.getValue(),
                (String) nameField.getValue(),
                (String) surnameField.getValue(),
                (String) companyIdField.getValue(),
                (String) mpkIdField.getValue()));
        users.setNullSelectionAllowed(false);
        users.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        users.setImmediate(true);
        users.setWidth("100%");

        List<UserData> liferayUsers = ctx.getUserDataDAO().findAll();

        for (UserData ud : tetaUsers) {
            String login;

            if ((login = isEmailOnList(liferayUsers, ud.getEmail())) != null) {
                if (!existInUserCombo(login)) {
                    ud.setLogin(login);
                    users.addItem(ud);
                    users.setItemCaption(ud, ud.getLogin() + " - " + ud.getEmail());
                }
            }

        }
    }

    private CriteriaConfigurer getCriteriaConfigurer(final String email, final String name, final String surname, final String companyId, final String mpkId) {
        return new CriteriaConfigurer() {
            @Override
            public void configure(DetachedCriteria criteria) {
                if (hasText(email)) {
                    criteria.add(Restrictions.ilike("email", email, MatchMode.ANYWHERE));
                }
                if (hasText(name)) {
                    criteria.add(Restrictions.ilike("name", name, MatchMode.ANYWHERE));
                }
                if (hasText(surname)) {
                    criteria.add(Restrictions.ilike("surname", surname, MatchMode.ANYWHERE));
                }
                if (hasText(companyId)) {
                    criteria.createCriteria("attributes")
                            .add(Restrictions.eq("key", "teta_company"))
                            .add(Restrictions.ilike("value", companyId, MatchMode.ANYWHERE));
                }
                // to trochę nie ma sensu: musimy zrobić multijoina po atrybutach
                else if (hasText(mpkId)) {
                    criteria.createCriteria("attributes")
                            .add(Restrictions.eq("key", "teta_mpk"))
                            .add(Restrictions.ilike("value", mpkId, MatchMode.ANYWHERE));
                }

                criteria.addOrder(Order.asc("login"));
                criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            }
        };

    }

    private String isEmailOnList(List<UserData> users, String email) {
        if (email == null) {
            return null;
        }

        for (UserData ud : users) {
            if (email.equals(ud.getEmail())) {
                return ud.getLogin();
            }
        }
        return null;
    }

    private boolean existInUserCombo(String login) {
        Iterator i = users.getItemIds().iterator();
        while (i.hasNext()) {
            UserData ud = (UserData) i.next();
            if (login.equals(ud.getLogin())) {
                return true;
            }
        }
        return false;
    }
}
