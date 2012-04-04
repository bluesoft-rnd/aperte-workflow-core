package pl.net.bluesoft.rnd.processtool.dict.mapping;

/**
 * User: POlszewski
 * Date: 2012-01-04
 * Time: 13:20:22
 */
public interface DictEntryFilter<EntryType> {
	boolean filter(EntryType entry);
}
