package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.controller;

import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmSettingsDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmSettingsDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mpawluczuk on 2014-11-17.
 */

@OsgiController(name = "settingscontroller")
public class SettingsController implements IOsgiWebController {
	public static final String ORDER_BY_FIELD = "key";

	@Autowired
	private ProcessToolRegistry processToolRegistry;

	@Autowired
	private ISettingsProvider settingsProvider;

	@ControllerMethod(action = "getSettings")
	public GenericResultBean getSettings(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();
		BpmSettingsDAO bpmSettingsDAO = new BpmSettingsDAO();
		List<ProcessToolSetting> bpmNotificationMailPropertiesList = bpmSettingsDAO.findAll(Order.asc(ORDER_BY_FIELD));

		List<BpmSettingsDTO> bpmNotificationMailPropertiesDtoList = new ArrayList<BpmSettingsDTO>(bpmNotificationMailPropertiesList.size());
		for (ProcessToolSetting dao : bpmNotificationMailPropertiesList){
			BpmSettingsDTO dto = new BpmSettingsDTO();
			dto.setKey(dao.getKey());
			dto.setValue(dao.getValue());
			dto.setDescription(dao.getDescription());
			bpmNotificationMailPropertiesDtoList.add(dto);
		}

		result.setData(bpmNotificationMailPropertiesList);
		return result;
	}

	@ControllerMethod(action = "setSettings")
	public GenericResultBean setSettings(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();
		HttpServletRequest request = invocation.getRequest();

		BpmSettingsDAO bpmSettingsDAO = new BpmSettingsDAO();
		List<ProcessToolSetting> bpmNotificationMailPropertiesList = bpmSettingsDAO.findAll();

		for (ProcessToolSetting dao : bpmNotificationMailPropertiesList){
			String val = request.getParameter(dao.getKey());
			if (val!=null){
				dao.setValue(val);
			}
		}

		bpmSettingsDAO.saveOrUpdate(bpmNotificationMailPropertiesList);
		settingsProvider.invalidateCache();
		result.setData(true);

		return result;
	}
}
