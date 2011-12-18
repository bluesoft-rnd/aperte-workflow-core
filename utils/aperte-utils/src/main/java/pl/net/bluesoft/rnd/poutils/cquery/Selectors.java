package pl.net.bluesoft.rnd.poutils.cquery;

import pl.net.bluesoft.rnd.poutils.cquery.func.F;

public final class Selectors<T> {
	@SuppressWarnings("unchecked")
	public static <T> F<T, T> identity(Class<T> clazz) {
		return (F<T,T>)identity;
	}
	
	public static <T> F<T, String> format(final String format) {
		return new F<T, String>() {
			@Override
			public String invoke(T x) {
				return String.format(format, x);
			}
		};
	}
	
	@SuppressWarnings("rawtypes")
	private static final F<?,?> identity = new F() {
		@Override
		public Object invoke(Object x) {
			return x;
		}		
	};	
}
