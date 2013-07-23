package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * User: POlszewski
 * Date: 2013-07-18
 * Time: 10:56
 */
@XStreamAlias("i18n")
public class DictionaryI18N {
	@XStreamAsAttribute
	private String lang;
	@XStreamAsAttribute
	private String value;

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
