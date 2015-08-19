package pl.net.bluesoft.casemanagement.processor;

import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;
import org.aperteworkflow.webapi.main.processes.processor.AbstractSaveProcessor;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseLog;
import pl.net.bluesoft.rnd.processtool.model.IAttribute;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.IAttributesMapper;
import pl.net.bluesoft.rnd.processtool.plugins.IMapper;
import pl.net.bluesoft.rnd.processtool.plugins.MapperContext;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-05-07.
 */
public class CaseProcessor extends AbstractSaveProcessor {
    private final Case caseInstance;
    private final IAttributesProvider provider;
    private final Logger logger = Logger.getLogger(CaseProcessor.class.getName());
	private final UserData user;

	@Override
    protected IAttributesProvider getProvider() {
        return caseInstance;
    }

    @Override
    protected IAttributesConsumer getConsumer() {
        return caseInstance;
    }

    public CaseProcessor(Case caseInstance, IAttributesProvider provider, I18NSource messageSource, Collection<HtmlWidget> widgets, UserData user) {
        super(messageSource, widgets);
        this.caseInstance = caseInstance;
        this.provider = provider;
	    this.user = user;
    }

    /**
     * Copy given attributes to the case
     *
     * @param attributes
     */
    public void copyAttributes(final List<? extends IAttribute> attributes) throws Exception {
        for (IAttribute attr : attributes) {
            List<IAttributesMapper> mappers = ProcessToolRegistry.Util.getRegistry().getDataRegistry().getAttributesMappersFor(attr.getClass());
            copyAttribute(attr, mappers);
        }
    }

    private void copyAttribute(IAttribute attr, List<IAttributesMapper> mappers) throws Exception {
        for (IAttributesMapper mapper : mappers) {
            mapper.map(attr, caseInstance, provider);
        }
    }

    public void copyAllAttributes(final List<? extends IAttribute> attributes) throws Exception {
		copyAllAttributes(attributes, new MapperContext());
	}

    public void copyAllAttributes(final List<? extends IAttribute> attributes, MapperContext mapperContext) throws Exception {
        copyAttributes(attributes);
        copyOtherData(mapperContext);
    }

    private void copyOtherData(MapperContext mapperContext) {
        List<IMapper> mappers = ProcessToolRegistry.Util.getRegistry().getDataRegistry().getMappersFor(provider.getClass(), provider.getDefinitionName());
        for (IMapper mapper : mappers) {
            mapper.map(caseInstance, provider, mapperContext);
        }
    }

	@Override
	public void auditLog(Collection<HandlingResult> results) {
		String json = null;
		try {
			json = mapper.writeValueAsString(results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		CaseLog log = new CaseLog();
		log.setEntryDate(new Date());
		log.setEventI18NKey("case.log.case-change");
		log.setLogType(CaseLog.LOG_TYPE_CASE_CHANGE);
		log.setLogValue(json);
		log.setUserLogin(user != null ? user.getLogin() : "");
		caseInstance.getCaseLog().add(log);
	}
}
