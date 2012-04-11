package pl.net.bluesoft.rnd.pt.ext.usersubstitution.widget;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.*;
import org.aperteworkflow.util.liferay.LiferayBridge;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

import pl.net.bluesoft.util.lang.Formats;
import pl.net.bluesoft.util.lang.Maps;
import pl.net.bluesoft.util.lang.Strings;

import java.util.*;

/**
 * User: POlszewski
 * Date: 2011-09-02
 * Time: 14:07:11
 */
@AliasName(name = "UserSubstitutionRequest")
public class UserSubstitutionRequestWidget extends BaseProcessToolWidget implements ProcessToolVaadinRenderable, ProcessToolDataWidget {
    private static final String USER_SUBSTITUTE_LOGIN = "userSubstitute";
    private static final String DATE_FROM = "dateFrom";
    private static final String DATE_TO = "dateTo";

    @AutoWiredProperty
    private boolean requestMode = true;

    private Map<String, UserData> usersMap;
    private UserData user;
    private UserData userSubstitute = null;
    private Date dateFrom;
    private Date dateTo;

    @Override
    public void loadData(BpmTask task) {
        ProcessInstance pi = task.getProcessInstance();
        user = pi.getCreator();
        String userSubstituteLogin = pi.getSimpleAttributeValue(USER_SUBSTITUTE_LOGIN, null);
        dateFrom = parseShortDate(pi.getSimpleAttributeValue(DATE_FROM, null));
        dateTo = parseShortDate(pi.getSimpleAttributeValue(DATE_TO, null));
        usersMap = Maps.collectionToMap(LiferayBridge.getAllUsersByCurrentUser(pi.getCreator()), "login");
        usersMap.remove(user.getLogin());
        if (Strings.hasText(userSubstituteLogin)) {
            userSubstitute = usersMap.get(userSubstituteLogin);
        }
    }

    @Override
    public void saveData(BpmTask task) {
        ProcessInstance pi = task.getProcessInstance();
        pi.setSimpleAttribute(USER_SUBSTITUTE_LOGIN, userSubstitute.getLogin());
        pi.setSimpleAttribute(DATE_FROM, formatShortDate(dateFrom));
        pi.setSimpleAttribute(DATE_TO, formatShortDate(dateTo));
//        if (user.getSuperior() != null && user.getSuperior().length() > 0) {
//            processInstance.setSimpleAttribute("superior", user.getSuperior());
//        }
//        else {
//            processInstance.setSimpleAttribute("superior", user.getLogin());
//        }
    }

    @Override
    public Collection<String> validateData(BpmTask task, boolean skipRequired) {
        List<String> errors = new ArrayList<String>();
        if (userSubstitute == null) {
            errors.add(getMessage("usersubstitution.user.required"));
        }
        if (dateFrom == null) {
            errors.add(getMessage("usersubstitution.date.from.required"));
        }
        if (dateTo == null) {
            errors.add(getMessage("usersubstitution.date.to.required"));
        }
        if (requestMode && dateFrom != null && dateFrom.before(parseShortDate(formatShortDate(new Date())))) {
            errors.add(getMessage("usersubstitution.date.from.can.not.be.in.the.past"));
        }
        if (dateFrom != null && dateTo != null && !(dateFrom.equals(dateTo) || dateFrom.before(dateTo))) {
            errors.add(getMessage("usersubstitution.date.from.must.not.be.later.than.date.to"));
        }
        return errors;
    }

    @Override
    public Component render() {
        boolean readonly = !hasPermission("EDIT");
        FormLayout fl = new FormLayout();
        fl.addComponent(createLabel(getMessage("usersubstitution.substituted.user"), user.getRealName()));
        if (!readonly) {
            fl.addComponent(createUserSelectionBox());
        }
        else {
            fl.addComponent(createLabel(
                    getMessage("usersubstitution.substituting.user"),
                    userSubstitute != null ? userSubstitute.getRealName() : null
            ));
        }
        fl.addComponent(createDateField(getMessage("usersubstitution.date.from"), "dateFrom", readonly));
        fl.addComponent(createDateField(getMessage("usersubstitution.date.to"), "dateTo", readonly));
        return fl;
    }

    private Label createLabel(String caption, Object value) {
        Label label = new Label();
        label.setCaption(caption);
        label.setValue(value);
        return label;
    }

    private PopupDateField createDateField(String caption, String boundProperty, boolean readonly) {
        PopupDateField dateField = new PopupDateField(caption);
        dateField.setDateFormat("yyyy-MM-dd");
        dateField.setResolution(PopupDateField.RESOLUTION_DAY);
        dateField.setRequired(true);
//        dateField.setImmediate(true);
        dateField.setPropertyDataSource(new MethodProperty<Date>(this, boundProperty));
        dateField.setReadOnly(readonly);
        return dateField;
    }

    private ComboBox createUserSelectionBox() {
        final ComboBox userBox = new ComboBox(getMessage("usersubstitution.substituting.user"));
        userBox.setItemCaptionPropertyId("filteredName");
        userBox.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        userBox.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        userBox.setRequired(true);
        userBox.setImmediate(true);
        userBox.setNewItemsAllowed(false);

        BeanItemContainer<UserData> usersBIC = new BeanItemContainer<UserData>(UserData.class);
        usersBIC.addAll(usersMap.values());
        usersBIC.sort(new Object[] {"realName"}, new boolean[] {true});
        userBox.setContainerDataSource(usersBIC);
        userBox.setValue(userSubstitute);
        userBox.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                userSubstitute = (UserData) userBox.getValue();
            }
        });
        return userBox;
    }

    @Override
    public void addChild(ProcessToolWidget child) {
        throw new IllegalArgumentException("Not supported!");
    }

    private static String formatShortDate(Date date) {
        return date != null ? Formats.formatShortDate(date) : null;
    }

    private static Date parseShortDate(String date) {
        return date != null ? Formats.parseShortDate(date) : null;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public boolean isRequestMode() {
        return requestMode;
    }

    public void setRequestMode(boolean requestMode) {
        this.requestMode = requestMode;
    }
}
