package org.aperteworkflow.custom;

import java.text.SimpleDateFormat;
import java.util.Set;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserServiceUtil;
import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;

@AliasName(name = "Sample Widget")
@ChildrenAllowed(value = false)
public class SampleWidget extends BaseProcessToolVaadinWidget {

	private static final SimpleDateFormat DF = new SimpleDateFormat("dd-MM-yyyy hh:mm");
	private User liferayUser;

	@Override
	public void setContext(ProcessStateConfiguration state,
			ProcessStateWidget configuration, I18NSource i18nSource,
			ProcessToolBpmSession bpmSession, Application application,
			Set<String> permissions, boolean isOwner) {

//		get user from context
		UserData user = bpmSession.getUser(ProcessToolContext.Util
				.getThreadProcessToolContext());
		try {
			
//		load associated liferay user 
			liferayUser = UserServiceUtil.getUserByEmailAddress(
					user.getCompanyId(), user.getEmail());

		} catch (SystemException e) {
			e.printStackTrace();
		} catch (PortalException e) {
			e.printStackTrace();
		}
		super.setContext(state, configuration, i18nSource, bpmSession,
				application, permissions, isOwner);
	}

	@Override
	public Component render() {
		return new SampleWidgetComponent() {

			
			
			@Override
			protected void loadData() {
//				fill labels with user data
				label_1.setValue(liferayUser.getFullName());
				label_2.setValue(liferayUser.getEmailAddress());
				label_3.setValue(liferayUser.getScreenName());
				label_4.setValue(liferayUser.getGreeting());
				label_5.setValue(DF.format(liferayUser.getLastLoginDate()));
//				fill table with user roles
				try {
					Container container = new BeanItemContainer<Role>(Role.class, liferayUser.getRoles());
					table_1.setContainerDataSource(container);
					Object[] visibleColumns = {"descriptiveName"};
					table_1.setVisibleColumns(visibleColumns );
					String[] columnHeaders = {"Role name"};
					table_1.setColumnHeaders(columnHeaders );
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			}
		};
	}


	@Override
	public void addChild(ProcessToolWidget child) {
		throw new UnsupportedOperationException();

	}

}
