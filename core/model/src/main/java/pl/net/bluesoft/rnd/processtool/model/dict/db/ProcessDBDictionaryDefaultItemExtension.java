package pl.net.bluesoft.rnd.processtool.model.dict.db;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;

import javax.persistence.*;

/**
 * User: POlszewski
 * Date: 2014-09-30
 */
@Entity
@Table(name = "pt_dictionary_def_item_ext")
public class ProcessDBDictionaryDefaultItemExtension extends AbstractPersistentEntity implements ProcessDictionaryItemExtension {
	public static final String _ITEM_VALUE = "itemValue";
	public static final String _NAME = "name";
	public static final String _VALUE = "value";
	public static final String _DESCRIPTION = "description";
	public static final String _VALUE_TYPE = "valueType";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_DICT_DEF_ITEM_EXT")
			}
	)
	@Column(name = "id")
	protected Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(value = { org.hibernate.annotations.CascadeType.ALL})
	private ProcessDBDictionary dictionary;

	private String name;
	@Column(name="value_")
	private String value;
	@Column(name="description_")
	private String description;
	private String valueType;

	public ProcessDBDictionaryDefaultItemExtension() {
	}

	private ProcessDBDictionaryDefaultItemExtension(ProcessDBDictionaryDefaultItemExtension ext) {
		id = ext.id;
		name = ext.name;
		value = ext.value;
		valueType = ext.value;
		description = ext.description;
		dictionary = ext.dictionary;
	}

	public ProcessDBDictionaryDefaultItemExtension exactCopy() {
		return new ProcessDBDictionaryDefaultItemExtension(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public ProcessDBDictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(ProcessDBDictionary dictionary) {
		this.dictionary = dictionary;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
