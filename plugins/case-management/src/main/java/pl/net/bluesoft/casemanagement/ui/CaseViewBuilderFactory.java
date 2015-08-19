package pl.net.bluesoft.casemanagement.ui;

import pl.net.bluesoft.casemanagement.model.Case;

/**
 * User: POlszewski
 * Date: 2014-11-01
 */
public interface CaseViewBuilderFactory {
	CaseViewBuilder create(Case caseInstance);
}
