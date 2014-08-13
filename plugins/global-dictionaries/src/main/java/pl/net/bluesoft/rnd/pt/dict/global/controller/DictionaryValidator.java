package pl.net.bluesoft.rnd.pt.dict.global.controller;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.pt.dict.global.exception.InvalidValueException;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.DateUtil;

import java.util.Date;

import static pl.net.bluesoft.util.lang.DateUtil.truncHours;
import static pl.net.bluesoft.util.lang.Strings.hasText;


/**
 * {@link ProcessDBDictionaryItem} item validator
 * <p/>
 * Validate for:
 * - empty keys
 * - duplicate keys in item dictionary
 * - value end date is before start date
 * - values dates' are overlapping
 * - item has empty value
 *
 * @author mpawlak@bluesoft.net.pl
 */
public class DictionaryValidator {
    private static final long serialVersionUID = 5861407560054058663L;

    private I18NSource messageSource;

    public DictionaryValidator(I18NSource messageSource) {
        this.messageSource = messageSource;
    }

    public void validate(ProcessDBDictionaryItem value) throws InvalidValueException {
        ProcessDBDictionaryItem itemToValidate = value;

        validateItemKey(itemToValidate);
        validateItemKeyDuplication(itemToValidate);

        for (ProcessDBDictionaryItemValue itemValue : itemToValidate.getValues()) {
            validateItemValueContent(itemValue);
            validateItemValueEndDateAfterStartDate(itemValue);

            for (ProcessDBDictionaryItemExtension extension : itemValue.getExtensions()) {
                validateItemValueExtensionForEmptyValues(extension);
            }

            validateItemValueExtensionKeyDuplication(itemValue);
        }

        validateItemForToManyFullRangeExistance(itemToValidate);

    }

    /**
     * Check if the key field is empty
     */
    private void validateItemKey(ProcessDBDictionaryItem itemToValidate) throws InvalidValueException {
        String key = itemToValidate.getKey();

        if (key == null || key.isEmpty())
            throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.itemValues.empty.key"));
    }

    /**
     * Check if the item key is duplicated
     */
    private void validateItemKeyDuplication(ProcessDBDictionaryItem itemToValidate) throws InvalidValueException {
        ProcessDBDictionary dictionary = itemToValidate.getDictionary();
        String key = itemToValidate.getKey();

        ProcessDBDictionaryItem lookUpItem = (ProcessDBDictionaryItem) dictionary.lookup(key);
        if (lookUpItem != null && !lookUpItem.equals(itemToValidate))
            throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.itemValues.duplicated.key", key));
    }

    /**
     * Check if the item key is duplicated
     */
    private void validateItemForToManyFullRangeExistance(ProcessDBDictionaryItem itemToValidate) throws InvalidValueException {
        for (ProcessDBDictionaryItemValue val : itemToValidate.getValues()) {
            /* Represent bottom full range as date with min value, and upper full range as maximum date */
            Date itemToValidateStartDate = val.getValidFrom() == null ? new Date(Long.MIN_VALUE) : truncHours(val.getValidFrom());
            Date itemToValidateEndDate = val.getValidTo() == null ? new Date(Long.MAX_VALUE) : truncHours(val.getValidTo());

            for (ProcessDBDictionaryItemValue otherVal : itemToValidate.getValues()) {
                /* Do not compare the same values */
                if (otherVal == val)
                    continue;

                Date currentValueStartDate = otherVal.getValidFrom() == null ? new Date(Long.MIN_VALUE) : truncHours(otherVal.getValidFrom());
                Date currentValueEndDate = otherVal.getValidTo() == null ? new Date(Long.MAX_VALUE) : truncHours(otherVal.getValidTo());

            	/*
                 * Let ConditionA Mean DateRange A Completely After DateRange B (True if StartA > EndB)
            	 * Let ConditionB Mean DateRange A Completely Before DateRange B (True if EndA < StartB)
            	 * 
            	 * Then Overlap exists if Neither A Nor B is true ( If one range is neither completely after 
            	 * the other, nor completely before the other, then they must overlap)
            	 * 
            	 * Now deMorgan's law says that: Not (A Or B) <=> Not A And Not B
            	 * 
            	 * Which means (StartA <= EndB) and (EndA >= StartB)
            	 */

                boolean areDatesOverlapping = DateUtil.beforeInclusive(itemToValidateStartDate, currentValueEndDate) &&
                        DateUtil.afterInclusive(itemToValidateEndDate, currentValueStartDate);

                if (areDatesOverlapping) {
                    throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.itemValues.overlapping.dates", val.getDefaultValue(), otherVal.getDefaultValue()));
                }
            }
        }
    }

    /**
     * Check if the all item value dates are correct: end date is after start date
     */
    private void validateItemValueEndDateAfterStartDate(ProcessDBDictionaryItemValue itemVaueToValidate) throws InvalidValueException {
        Date startDate = itemVaueToValidate.getValidFrom() != null ? truncHours(itemVaueToValidate.getValidFrom()) : null;
        Date endDate = itemVaueToValidate.getValidTo() != null ? truncHours(itemVaueToValidate.getValidTo()) : null;

        if (endDate != null && startDate != null && endDate.before(startDate)) {
            throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.itemValues.dates"));
        }
    }

    /**
     * Check if item value is not empty
     */
    private void validateItemValueContent(ProcessDBDictionaryItemValue itemValueToValidate) throws InvalidValueException {
        if (!hasText(itemValueToValidate.getDefaultValue())) {
            throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.itemValues.empty.value"));
        }
    }

    /**
     * Check item value extension for empty values
     */
    private void validateItemValueExtensionForEmptyValues(ProcessDBDictionaryItemExtension itemExtension) throws InvalidValueException {
        if (!hasText(itemExtension.getName())) {
            throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.valueExtensions.empty.key"));
        }
    }

    /**
     * Check if the item value extension key is duplicated
     */
    private void validateItemValueExtensionKeyDuplication(ProcessDBDictionaryItemValue itemVaueToValidate) throws InvalidValueException {
        for (ProcessDBDictionaryItemExtension ext : itemVaueToValidate.getExtensions()) {
            for (ProcessDBDictionaryItemExtension otherExt : itemVaueToValidate.getExtensions()) {
                if (ext != otherExt && ext.getName().equals(otherExt.getName())) {
                    throw new InvalidValueException(messageSource.getMessage("dictionary.editor.validator.valueExtensions.duplicate.key", ext.getName()));
                }
            }
        }
    }

}
