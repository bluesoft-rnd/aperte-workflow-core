package pl.net.bluesoft.rnd.processtool.ui.dict.validator;

import static pl.net.bluesoft.util.lang.DateUtil.truncHours;

import java.util.Date;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.util.lang.DateUtil;

import com.vaadin.data.Validator;

/**
 * {@link ProcessDBDictionaryItem} item validator
 * 
 * Validate for:
 * - empty keys
 * - duplicate keys in item dictionary
 * - value end date is before start date
 * - values dates' are overlapping
 * - item has empty value
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DictionaryItemValidator implements Validator
{
	private static final long serialVersionUID = 5861407560054058663L;
	
	private GenericVaadinPortlet2BpmApplication application;
    
	public DictionaryItemValidator(GenericVaadinPortlet2BpmApplication application) {
		this.application = application;
	}

	@Override
	public void validate(Object value) throws InvalidValueException 
	{
		ProcessDBDictionaryItem itemToValidate = (ProcessDBDictionaryItem)value;

		validateItemKey(itemToValidate);
		validateItemKeyDuplication(itemToValidate);
		
		for(ProcessDBDictionaryItemValue itemValue: itemToValidate.getValues())
		{
			validateItemValueContent(itemValue);
			validateItemValueEndDateAfterStartDate(itemValue);
			
			for(ProcessDBDictionaryItemExtension extension: itemValue.getExtensions())
			{
				validateItemValueExtensionForEmptyValues(extension);
			}
			
			validateItemValueExtensionKeyDuplication(itemValue);
		}
		
		validateItemForToManyFullRangeExistance(itemToValidate);

	}
	
	/** Check if the key field is empty */
	private void validateItemKey(ProcessDBDictionaryItem itemToValidate) throws InvalidValueException
	{
		String key = itemToValidate.getKey();
		
		if(key == null || key.isEmpty())
			throw new InvalidValueException(application.getMessage("validation.empty.key"));
	}
	
	/** Check if the item key is duplicated */
	private void validateItemKeyDuplication(ProcessDBDictionaryItem itemToValidate) throws InvalidValueException
	{
		ProcessDBDictionary dictionary = itemToValidate.getDictionary();
		String key = itemToValidate.getKey();
		
		ProcessDBDictionaryItem lookUpItem = (ProcessDBDictionaryItem)dictionary.lookup(key);
		if(lookUpItem != null && lookUpItem != itemToValidate && itemToValidate.getKey().equals(lookUpItem.getKey()))
			throw new InvalidValueException(application.getMessage("validation.duplicated.key", key, key));
	}
	
	/** Check if the item key is duplicated */
	private void validateItemForToManyFullRangeExistance(ProcessDBDictionaryItem itemToValidate) throws InvalidValueException
	{
        for (ProcessDBDictionaryItemValue val : itemToValidate.getValues()) 
        {
        	/* Represent bottom full range as date with min value, and upper full range as maximum date */
        	Date itemToValidateStartDate = val.getValidStartDate() == null ? new Date(Long.MIN_VALUE) : truncHours(val.getValidStartDate());
        	Date itemToValidateEndDate = val.getValidEndDate() == null ? new Date(Long.MAX_VALUE) : truncHours(val.getValidEndDate());
        	
        	for (ProcessDBDictionaryItemValue otherVal : itemToValidate.getValues()) 
        	{
        		/* Do not compare the same values */
        		if(otherVal == val)
        			continue;
        		
            	Date currentValueStartDate = otherVal.getValidStartDate() == null ? new Date(Long.MIN_VALUE) : truncHours(otherVal.getValidStartDate());
            	Date currentValueEndDate = otherVal.getValidEndDate() == null ? new Date(Long.MAX_VALUE) : truncHours(otherVal.getValidEndDate());
            	
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
            	
            	if(areDatesOverlapping)
            		throw new InvalidValueException(application.getMessage("validate.item.val.overlapping.dates", "", val.getValue(), otherVal.getValue()));
        	}
 
        }
	}
	
	/** Check if the all item value dates are correct: end date is after start date */
	private void validateItemValueEndDateAfterStartDate(ProcessDBDictionaryItemValue itemVaueToValidate) throws InvalidValueException
	{
        Date startDate = itemVaueToValidate.getValidStartDate() != null ? truncHours(itemVaueToValidate.getValidStartDate()) : null;
        Date endDate = itemVaueToValidate.getValidEndDate() != null ? truncHours(itemVaueToValidate.getValidEndDate()) : null;

        if (endDate != null && startDate != null && endDate.before(startDate)) {
            throw new InvalidValueException(application.getMessage("validate.item.val.dates"));
        }
	}
	
	/** Check if item value is not empty */
	private void validateItemValueContent(ProcessDBDictionaryItemValue itemVaueToValidate)
	{
		boolean isItemValueEmpty = itemVaueToValidate.getValue() == null || itemVaueToValidate.getValue().isEmpty();
		
		if(isItemValueEmpty)
			throw new InvalidValueException(application.getMessage("validate.item.val.empty"));
	}
	
	/** Check item value extension for empty values */
	private void validateItemValueExtensionForEmptyValues(ProcessDBDictionaryItemExtension itemExtension)
	{
		boolean isExtensionNameEmpty = itemExtension.getName() == null || itemExtension.getName().isEmpty();

		if(isExtensionNameEmpty)
			throw new InvalidValueException(application.getMessage("validate.item.ext.name.empty"));
	}
	
	/** Check if the item value extension key is duplicated */
	private void validateItemValueExtensionKeyDuplication(ProcessDBDictionaryItemValue itemVaueToValidate) throws InvalidValueException
	{
        for (ProcessDBDictionaryItemExtension ext : itemVaueToValidate.getExtensions()) 
            for (ProcessDBDictionaryItemExtension otherExt : itemVaueToValidate.getExtensions()) 
                if (ext != otherExt && ext.getName().equals(otherExt.getName())) 
                    throw new InvalidValueException(application.getMessage("validate.item.ext.name.duplicate", "", ext.getName()));
	}

	@Override
	public boolean isValid(Object value) 
	{
		try
		{	
			validate(value);
		}
		catch(InvalidValueException ex)
		{
			return false;
		}
		
		return true;
	}

}
