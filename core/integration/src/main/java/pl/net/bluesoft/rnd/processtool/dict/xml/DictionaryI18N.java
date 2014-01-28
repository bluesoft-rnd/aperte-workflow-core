package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * User: POlszewski
 * Date: 2013-07-18
 * Time: 10:56
 */
    @XStreamAlias("i18n")
    @XStreamConverter(value=ToAttributedValueConverter.class, strings={"text"})
public class DictionaryI18N {

	private String lang;

	private String value;

    private String text;

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getValue() {
        if(value ==null || value.length()<=0)
                return text;
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
