package pl.net.bluesoft.casemanagement.deployment;

import com.thoughtworks.xstream.XStream;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.casemanagement.dao.CaseDefinitionDAO;
import pl.net.bluesoft.casemanagement.dao.CaseDefinitionDAOImpl;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-29.
 */
public class CaseDeployer {
    private static final Logger logger = Logger.getLogger(CaseDeployer.class.getName());

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    public CaseDeployer() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public CaseDefinition unmarshallCaseDefinition(InputStream stream) {
        final XStream xstream = new XStream();
        xstream.aliasPackage("config", CaseDefinition.class
                .getPackage().getName());
        xstream.useAttributeFor(String.class);
        xstream.useAttributeFor(Boolean.class);
        xstream.useAttributeFor(Integer.class);

        final CaseDefinition definition = (CaseDefinition) xstream.fromXML(stream);
        adjustDefinition(definition);
        return definition;
    }

    private void adjustDefinition(CaseDefinition definition) {
		handleSimilarStates(definition);
        for (CaseStateDefinition csd : definition.getPossibleStates()) {
            csd.setDefinition(definition);
            adjustStateDefinition(csd);
        }
		if (definition.getInitialStateName() != null) {
			definition.setInitialState(definition.getState(definition.getInitialStateName()));
		}
    }

	private void handleSimilarStates(CaseDefinition definition) {
		List<CaseStateDefinition> statesToAdd = new ArrayList<CaseStateDefinition>();

		for (CaseStateDefinition stateDefinition : definition.getPossibleStates()) {
			if (stateDefinition.getName().indexOf(',') >= 0) {
				String[] names = stateDefinition.getName().split(",");

				CaseStateDefinition[] similarStates = new CaseStateDefinition[names.length];

				similarStates[0] = stateDefinition;

				for (int i = 1; i < similarStates.length; ++i) {
					similarStates[i] = stateDefinition.deepClone();
					statesToAdd.add(similarStates[i]);
				}

				for (int i = 0; i < similarStates.length; ++i) {
					similarStates[i].setName(names[i]);
				}
			}
		}
		definition.getPossibleStates().addAll(statesToAdd);
	}

	private void adjustStateDefinition(CaseStateDefinition state) {
		if (state.getWidgets() != null) {
			for (CaseStateWidget csw : state.getWidgets()) {
				adjustWidget(csw, null, state);
			}
		}
		if (state.getRoles() != null) {
			for (CaseStateRole role : state.getRoles()) {
				role.setStateDefinition(state);
			}
		}
		if (state.getProcesses() != null) {
			for (CaseStateProcess process : state.getProcesses()) {
				process.setStateDefinition(state);
			}
		}
    }

    private void adjustWidget(CaseStateWidget widget, CaseStateWidget parent, CaseStateDefinition caseStateDefinition) {
        if (parent != null) {
            widget.setParent(parent);
            widget.setCaseStateDefinition(null);
        }
		else {
			widget.setCaseStateDefinition(caseStateDefinition);
		}
		if (widget.getAttributes() != null) {
			for (CaseStateWidgetAttribute attr : widget.getAttributes()) {
				attr.setCaseStateWidget(widget);
			}
		}
		if (widget.getPermissions() != null) {
			for (CaseStateWidgetPermission perm : widget.getPermissions()) {
				perm.setCaseStateWidget(widget);
			}
		}
		if (widget.getChildren() != null) {
			for (CaseStateWidget child : widget.getChildren()) {
				adjustWidget(child, widget, caseStateDefinition);
			}
		}
    }

    public CaseDefinition deployOrUpdateCaseDefinition(CaseDefinition caseDefinition, Session session) {
        CaseDefinitionDAO dao = new CaseDefinitionDAOImpl(session);
        return dao.createOrUpdateDefinition(caseDefinition);
    }
}
