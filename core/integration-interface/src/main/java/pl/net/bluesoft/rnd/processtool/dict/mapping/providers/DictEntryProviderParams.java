package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.processtool.dict.mapping.DictMapper;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Date;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 21:04:00
 */
public class DictEntryProviderParams {
	private ProcessInstance processInstance;
	private I18NSource i18NSource;
	private Date date;
	private DictMapper dictMapper;

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	public void setI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public DictMapper getDictMapper() {
		return dictMapper;
	}

	public void setDictMapper(DictMapper dictMapper) {
		this.dictMapper = dictMapper;
	}
}
