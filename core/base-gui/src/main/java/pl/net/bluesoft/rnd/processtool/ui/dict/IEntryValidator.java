package pl.net.bluesoft.rnd.processtool.ui.dict;


/**
 * Validation source interface
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IEntryValidator<ENTRY> 
{
    /**
     * Validate given dictionary item
     * 
     * @param item item to validate
     * @return false if item is invalid
     */
    boolean isEntryValid(ENTRY itemToValidate);
}
