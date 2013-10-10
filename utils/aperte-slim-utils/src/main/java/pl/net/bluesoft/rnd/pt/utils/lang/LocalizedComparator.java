package pl.net.bluesoft.rnd.pt.utils.lang;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 12:01
 */
public abstract class LocalizedComparator<T> implements Comparator<T> {
	private final Collator collator;

	protected LocalizedComparator(Locale locale) {
		collator = Collator.getInstance(locale != null ? locale : new Locale("pl", "PL"));
		collator.setStrength(Collator.SECONDARY);
	}

	@Override
	public int compare(T t1, T t2) {
		return collator.compare(getValue(t1), getValue(t2));
	}

	protected abstract String getValue(T t);
}