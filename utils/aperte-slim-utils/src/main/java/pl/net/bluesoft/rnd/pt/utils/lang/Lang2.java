package pl.net.bluesoft.rnd.pt.utils.lang;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * User: POlszewski
 * Date: 2012-10-05
 * Time: 10:29
 */
public class Lang2 {
	public static byte[] noCopy(byte[] t) {
		return t;
	}

	public static <T> T[] noCopy(T[] t) {
		return t;
	}

	public static String[] toStringArray(Collection<String> collection) {
		if (collection != null) {
			return collection.toArray(new String[collection.size()]);
		}
		return null;
	}

	public static <T> T[] toObjectArray(Collection<T> collection, Class<T> clazz) {
		if (collection != null) {
			return collection.toArray((T[])Array.newInstance(clazz, collection.size()));
		}
		return null;
	}

	public static <T> T assumeType(Object obj, Class<T> clazz) {
		if (obj == null) {
			return null;
		}
		if (clazz.isAssignableFrom(obj.getClass())) {
			return (T)obj;
		}
		throw new ClassCastException("Unable to cast " + obj.getClass() + " to " + clazz);
	}
}
