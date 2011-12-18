package pl.net.bluesoft.rnd.poutils;

public final class Lang {
	public static <T1, T2> boolean equals(T1 t1, T2 t2) {
		return t1 == t2 || t1 != null && t1.equals(t2);
	}

    public static <T> T coalesce(T...values) {
        for (T t : values) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public static String blankIfNull(String value) {
        return value != null ? value : "";
    }
}
