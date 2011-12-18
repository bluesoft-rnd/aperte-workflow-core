package pl.net.bluesoft.rnd.poutils.cquery;

public final class EqualityComparers {
	public static final EqualityComparer<String> CASE_INSENSITIVE = new EqualityComparer<String>() {		
		@Override
		public int hashCode(String t) {
			return t != null ? t.toLowerCase().hashCode() : 0;
		}
		
		@Override
		public boolean equals(String t1, String t2) {
			return t1 == t2 || t1 != null && t1.toLowerCase().equals(t2.toLowerCase());
		}
	};
}
