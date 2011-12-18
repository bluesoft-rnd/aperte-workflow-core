package pl.net.bluesoft.rnd.poutils.cquery;

import pl.net.bluesoft.rnd.poutils.cquery.func.P;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Collection wrapper that mimics .NET LINQ functionality
 */
public final class CQuery {
	public static <T> CQueryCollection<T> from(final Set<T> collection) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return collection.iterator();
			}
			
			@Override
			public boolean any() {				
				return collection.isEmpty();
			}
			
			@Override
			public boolean isEmpty() {
				return collection.isEmpty();
			}
			
			@Override
			public int count() {				
				return collection.size();
			}
			
			@Override
			public int size() {
				return collection.size();
			}
			
			@Override
			public CQueryCollection<T> distinct() {
				return this;
			}

            @Override
            public boolean add(T t) {
                return collection.add(t);
            }

            @Override
            public boolean remove(Object o) {
                return collection.remove(o);
            }

            @Override
            public boolean addAll(Collection<? extends T> c) {
                return collection.addAll(c);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return collection.removeAll(c);
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return collection.retainAll(c);
            }

            @Override
            public void clear() {
                collection.clear();
            }
		};
	}
	
	public static <T> CQueryCollection<T> from(final Collection<T> collection) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return collection.iterator();
			}
			
			@Override
			public boolean any() {				
				return collection.isEmpty();
			}
			
			@Override
			public boolean isEmpty() {
				return collection.isEmpty();
			}
			
			@Override
			public int count() {				
				return collection.size();
			}
			
			@Override
			public int size() {
				return collection.size();
			}

            @Override
            public boolean add(T t) {
                return collection.add(t);
            }

            @Override
            public boolean remove(Object o) {
                return collection.remove(o);
            }

            @Override
            public boolean addAll(Collection<? extends T> c) {
                return collection.addAll(c);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return collection.removeAll(c);
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return collection.retainAll(c);
            }

            @Override
            public void clear() {
                collection.clear();
            }
		};
	}
	
	public static <T> CQueryCollection<T> from(final Iterable<T> iterable) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterable.iterator();
			}
		};
	}
	
	public static <T> CQueryCollection<T> from(final T...array) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					private int pos = 0;
					
					@Override
					public boolean hasNext() {
						return pos < array.length;
					}
					
					@Override
					public T next() {		
						return array[pos++];
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();		
					}
				};
			}
			
			@Override
			public boolean isEmpty() {
				return array.length == 0;
			}
			
			@Override
			public boolean any() {
				return array.length > 0;
			}
			
			@Override
			public int count() {
				return array.length;
			}
						
			@Override
			public int size() {
				return array.length;
			}

            @Override
            public T elementAt(int index) {
                return array[index];
            }

            @Override
            public T elementAtOrDefault(int index, T default_) {
                return index < array.length ? array[index] : default_;
            }
        };
	}
	
	public static CQueryCollection<Boolean> from(final boolean...array) {
		return new CQueryCollection<Boolean>() {
			@Override
			public Iterator<Boolean> iterator() {
				return new Iterator<Boolean>() {
					private int pos = 0;
					
					@Override
					public boolean hasNext() {
						return pos < array.length;
					}
					
					@Override
					public Boolean next() {		
						return array[pos++];
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();		
					}
				};
			}
			
			@Override
			public boolean isEmpty() {
				return array.length == 0;
			}
			
			@Override
			public boolean any() {
				return array.length > 0;
			}
			
			@Override
			public int count() {
				return array.length;
			}
						
			@Override
			public int size() {
				return array.length;
			}

            @Override
            public Boolean elementAt(int index) {
                return array[index];
            }

            @Override
            public Boolean elementAtOrDefault(int index, Boolean default_) {
                return index < array.length ? array[index] : default_;
            }
		};
	}
	
	public static CQueryCollection<Integer> from(final int...array) {
		return new CQueryCollection<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int pos = 0;
					
					@Override
					public boolean hasNext() {
						return pos < array.length;
					}
					
					@Override
					public Integer next() {		
						return array[pos++];
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();		
					}
				};
			}
			
			@Override
			public boolean isEmpty() {
				return array.length == 0;
			}
			
			@Override
			public boolean any() {
				return array.length > 0;
			}
			
			@Override
			public int count() {
				return array.length;
			}
						
			@Override
			public int size() {
				return array.length;
			}

            @Override
            public Integer elementAt(int index) {
                return array[index];
            }

            @Override
            public Integer elementAtOrDefault(int index, Integer default_) {
                return index < array.length ? array[index] : default_;
            }
		};
	}	
	
	public static <T> CQueryCollection<T> repeat(final T value, final int n) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					int pos = 0;
					@Override
					public boolean hasNext() {
						return pos < n;
					}

					@Override
					public T next() {
						if (pos < n) {
							++pos;
							return value;
						}
						throw new IllegalStateException();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}					
				};
			}
		};		
	}
	
	public static CQueryCollection<Integer> range(final int start, final int end) {
		return range(start, end, 1);
	}
	
	public static CQueryCollection<Integer> range(final int start, final int end, final int step) {
		return new CQueryCollection<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int pos = start;
					@Override
					public boolean hasNext() {
						return pos <= end;
					}

					@Override
					public Integer next() {
						if (pos <= end) {
							int result = pos;
							pos += step;
							return result;
						}
						throw new IllegalStateException();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}					
				};
			}
		};		
	}
	
	@SuppressWarnings("rawtypes")
	private static CQueryCollection empty = new CQueryCollection() {
		Iterator iterator = new Iterator() {
			@Override
			public boolean hasNext() {				
				return false;
			}

			@Override
			public Object next() {
				throw new IllegalStateException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};
		
		@Override
		public Iterator iterator() {
			return iterator;
		}
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public boolean any() {
			return false;
		}
		
		@Override
		public int count() {
			return 0;
		}
		
		@Override
		public int count(P pred) {
			return 0;
		}
		
		@Override
		public int size() {
			return 0;
		}
	};
	
	@SuppressWarnings("unchecked")
	public static <T> CQueryCollection<T> empty(Class<T> clazz) {
		return empty;
	}
}
