package pl.net.bluesoft.rnd.processtool.model.dict.db;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import javax.persistence.*;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-07-17
 * Time: 12:09
 */
@Entity
@Table(name = "pt_dictionary_i18n")
public class ProcessDBDictionaryI18N extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_I18N")
			}
	)
	@Column(name = "id")
	private Long id;

	@Column(name = "languageCode", length = 10)
	private String languageCode;

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "text_")
	private String text;

	public ProcessDBDictionaryI18N() {}

	public ProcessDBDictionaryI18N(String languageCode, String text) {
		this.languageCode = languageCode;
		this.text = text;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public static void setLocalizedText(List<ProcessDBDictionaryI18N> i18Ns, String languageCode, String text) {
		ProcessDBDictionaryI18N i18N = findByLanguageCode(i18Ns, languageCode);

		if (i18N != null) {
			i18N.text = text;
		}
		else {
			i18Ns.add(new ProcessDBDictionaryI18N(languageCode, text));
		}
	}

	public static String getLocalizedText(List<ProcessDBDictionaryI18N> i18Ns, String languageCode, String defaultText) {
		if(defaultText == null)
			defaultText = "";

		if (languageCode == null) {
			return defaultText;
		}

		String[] parts = languageCode.split("_");

		ProcessDBDictionaryI18N i18N = null;

		if (parts.length == 2) {
			i18N = findByLanguageCode(i18Ns, languageCode);
		}
		if (i18N == null) {
			i18N = findByLanguageCode(i18Ns, parts[0]);
		}
		if (i18N == null){
			i18N = findByLanguageCode(i18Ns, "default");
		}
		if (defaultText == null){
			defaultText = "";
		}
		
		return i18N != null ? i18N.text : defaultText;
	}

	private static ProcessDBDictionaryI18N findByLanguageCode(List<ProcessDBDictionaryI18N> i18Ns, String languageCode) {
		for (ProcessDBDictionaryI18N i18N : i18Ns) {
			if (i18N.languageCode.equals(languageCode)) {
				return i18N;
			}
		}
		return null;
	}
}
