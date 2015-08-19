package pl.net.bluesoft.casemanagement.ui;

import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2014-06-24
 */
public class CasePrivilegeHandlers {
	public static final CasePrivilegeHandlers INSTANCE = new CasePrivilegeHandlers();

	private List<CasePrivilegeHandler> handlers = new ArrayList<CasePrivilegeHandler>();

	public void add(CasePrivilegeHandler handler) {
		handlers.add(handler);
	}

	public void remove(CasePrivilegeHandler handler) {
		handlers.remove(handler);
	}

	public void handleEdit(Case caseInstance, UserData user, Collection<String> privileges) {
		for (CasePrivilegeHandler handler : handlers) {
			handler.handleEdit(caseInstance, user, privileges);
		}
	}


    public void handleView(Case caseInstance, UserData user, Collection<String> privileges) {
        for (CasePrivilegeHandler handler : handlers) {
            handler.handView(caseInstance, user, privileges);
        }
    }
}
