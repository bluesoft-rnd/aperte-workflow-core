package pl.net.bluesoft.rnd.poutils.cquery;

import pl.net.bluesoft.rnd.poutils.Lang;
import pl.net.bluesoft.rnd.poutils.cquery.func.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static pl.net.bluesoft.rnd.poutils.cquery.CQuery.from;

public abstract class CQueryCollection<T> implements Collection<T> {

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public abstract Iterator<T> iterator();
	
	public <R> CQueryCollection<R> select(final F<? super T, R> selector) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new Iterator<R>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					
					@Override
					public boolean hasNext() {
						return iterator.hasNext();		
					}
				
					@Override
					public R next() {
						return selector.invoke(iterator.next());
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();		
					}	
				};
			}
		};
	}
	
	public <R> CQueryCollection<R> select(final FI<? super T, R> selector) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {				
				return new Iterator<R>() {	
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private int index = 0;
					
					@Override
					public boolean hasNext() {
						return iterator.hasNext();		
					}
				
					@Override
					public R next() {
						return selector.invoke(iterator.next(), index++);
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();		
					}
				};
			}
		};
	}
	
	public <R> CQueryCollection<R> selectMany(final F<? super T, ? extends Iterable<R>> selector) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {				
				return new Iterator<R>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private Iterator<R> innerIterator;
					
					@Override
					public boolean hasNext() {
						if (innerIterator != null && innerIterator.hasNext()) {
							return true;
						}
						while (iterator.hasNext()) {
							Iterable<R> col = selector.invoke(iterator.next());
							if (col != null) {
								innerIterator = col.iterator();
								if (innerIterator.hasNext()) {
									return true;
								}
							}
						}
						return false;
					}
				
					@Override
					public R next() {
						return innerIterator.next();
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();		
					}
				};
			}
		};
	}

    public <R> CQueryCollection<R> selectMany(final FI<? super T, ? extends Iterable<R>> selector) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new Iterator<R>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private Iterator<R> innerIterator;
                    private int pos = 0;

					@Override
					public boolean hasNext() {
						if (innerIterator != null && innerIterator.hasNext()) {
							return true;
						}
						while (iterator.hasNext()) {
							Iterable<R> col = selector.invoke(iterator.next(), pos++);
							if (col != null) {
								innerIterator = col.iterator();
								if (innerIterator.hasNext()) {
									return true;
								}
							}
						}
						return false;
					}

					@Override
					public R next() {
						return innerIterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

    public <R, TCollection> CQueryCollection<R> selectMany(
            final F<? super T, ? extends Iterable<TCollection>> collectionSelector, final F2<? super T, ? super TCollection, R> resultSelector) {
        return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new Iterator<R>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private Iterator<TCollection> innerIterator;
                    private T t;

					@Override
					public boolean hasNext() {
						if (innerIterator != null && innerIterator.hasNext()) {
							return true;
						}
						while (iterator.hasNext()) {
							Iterable<TCollection> col = collectionSelector.invoke(t = iterator.next());
							if (col != null) {
								innerIterator = col.iterator();
								if (innerIterator.hasNext()) {
									return true;
								}
							}
						}
						return false;
					}

					@Override
					public R next() {
						return resultSelector.invoke(t, innerIterator.next());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
    }

     public <R, TCollection> CQueryCollection<R> selectMany(
            final FI<? super T, ? extends Iterable<TCollection>> collectionSelector, final F2<? super T, ? super TCollection, R> resultSelector) {
        return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new Iterator<R>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private Iterator<TCollection> innerIterator;
                    private T t;
                    private int pos = 0;
                    
					@Override
					public boolean hasNext() {
						if (innerIterator != null && innerIterator.hasNext()) {
							return true;
						}
						while (iterator.hasNext()) {
							Iterable<TCollection> col = collectionSelector.invoke(t = iterator.next(), pos++);
							if (col != null) {
								innerIterator = col.iterator();
								if (innerIterator.hasNext()) {
									return true;
								}
							}
						}
						return false;
					}

					@Override
					public R next() {
						return resultSelector.invoke(t, innerIterator.next());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
    }
	
	public CQueryCollection<T> where(final P<? super T> pred) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private T nextValue;
					private boolean hasNext = false;
					
					@Override
					public boolean hasNext() {
						if (hasNext) {
							return true;
						}		
						while (iterator.hasNext()) {
							T t = iterator.next();
							if (pred.invoke(t)) {
								nextValue = t;
								hasNext = true;
								return true;
							}
						}
						return false;
					}
				
					@Override
					public T next() {
						if (!hasNext) {
							throw new IllegalStateException();
						}
						hasNext = false;
						return nextValue;
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();	
					}
				};
			}						
		};
	}
	
	public CQueryCollection<T> where(final PI<? super T> pred) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {	
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private int index = 0;
					private T nextValue;
					private boolean hasNext = false;
										
					@Override
					public boolean hasNext() {
						if (hasNext) {
							return true;
						}		
						while (iterator.hasNext()) {
							T t = iterator.next();
							if (pred.invoke(t, index++)) {
								nextValue = t;
								hasNext = true;
								return true;
							}
						}
						return false;
					}
				
					@Override
					public T next() {
						if (!hasNext) {
							throw new IllegalStateException();
						}
						hasNext = false;
						return nextValue;
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();	
					}
				};
			} 
		};
	}
	
	public CQueryCollection<T> notNull() {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private T nextValue;
					
					@Override
					public boolean hasNext() {
						if (nextValue != null) {
							return true;
						}		
						while (iterator.hasNext()) {
							T t = iterator.next();
							if (t != null) {
								nextValue = t;
								return true;
							}
						}
						return false;
					}
				
					@Override
					public T next() {
						if (nextValue == null) {
							throw new IllegalStateException();
						}
						T r = nextValue;
						nextValue = null;
						return r;
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();	
					}
				};
			}						
		};
	}		

	private static abstract class DistinctIterator<T> implements Iterator<T> {
		private final Iterator<T> iterator;
		private final Set<T> alreadyReturned;
		private T nextValue;
		private boolean hasNext = false;
		
		public DistinctIterator(Iterator<T> iterator) {
			this.iterator = iterator;
			this.alreadyReturned = createIndexSet();
		}
		
		@Override
		public boolean hasNext() {
			if (hasNext) {
				return true;
			}
			while (iterator.hasNext()) {
				T t = iterator.next();
				if (!alreadyReturned.contains(t)) {
					alreadyReturned.add(t);
					nextValue = t;
					hasNext = true;
					return true;
				}
			}
			return false;
		}

		@Override
		public T next() {
			if (!hasNext) {
				throw new IllegalStateException();
			}
			hasNext = false;
			return nextValue;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		protected abstract Set<T> createIndexSet();
	}

	public CQueryCollection<T> distinct() {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new DistinctIterator<T>(CQueryCollection.this.iterator()) {
					@Override
					protected Set<T> createIndexSet() {
						return new HashSet<T>();
					}					
				};
			}
		};
	}
	
	public CQueryCollection<T> distinct(final EqualityComparer<? super T> comparer) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new DistinctIterator<T>(CQueryCollection.this.iterator()) {
					@Override
					protected Set<T> createIndexSet() {
						return new CustomEqualitySet<T>(comparer);
					}					
				};
			}
		};
	}
	
	public CQueryCollection<T> skip(final int n) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private int skip = 0;

					@Override
					public boolean hasNext() {
						while (skip < n && iterator.hasNext()) {
							iterator.next();
							++skip;
						}
						return iterator.hasNext();
					}

					@Override
					public T next() {
						if (skip < n) {
							throw new IllegalStateException();
						}
						return iterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();					
					}				
				};
			}
		};
	}
	
	public CQueryCollection<T> skipWhile(final P<? super T> pred) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private boolean skip = true;
					private boolean firstNonskipped;
					private T toReturn;
					
					@Override
					public boolean hasNext() {
						while (skip) {
							if (!iterator.hasNext()) {
								return false;
							}
							T t = iterator.next();
							if (!pred.invoke(t)) {
								skip = false;
								firstNonskipped = true;
								toReturn = t;
								return true;
							}
						}						
						return iterator.hasNext();
					}

					@Override
					public T next() {
						if (skip) {
							throw new IllegalStateException();
						}
						if (firstNonskipped) {
							firstNonskipped = false;
							return toReturn;
						}
						return iterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();					
					}				
				};
			}
		};
	}
	
	public CQueryCollection<T> skipWhile(final PI<? super T> pred) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private boolean skip = true;
					private boolean firstNonskipped;
					private T toReturn;
					private int index = 0;
					
					@Override
					public boolean hasNext() {
						while (skip) {
							if (!iterator.hasNext()) {
								return false;
							}
							T t = iterator.next();
							++index;
							if (!pred.invoke(t, index)) {
								skip = false;
								firstNonskipped = true;
								toReturn = t;
								return true;
							}
						}						
						return iterator.hasNext();
					}

					@Override
					public T next() {
						if (skip) {
							throw new IllegalStateException();
						}
						if (firstNonskipped) {
							firstNonskipped = false;
							return toReturn;
						}
						return iterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();					
					}				
				};
			}
		};
	}
	
	public CQueryCollection<T> take(final int n) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private int i = 0;
					
					@Override
					public boolean hasNext() {						
						return i < n && iterator.hasNext();
					}

					@Override
					public T next() {
						if (i < n) {
							return iterator.next();
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
	
	public CQueryCollection<T> takeWhile(final P<? super T> pred) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private T toReturn;
					private boolean take = true;
					private boolean stop = false;
					@Override
					public boolean hasNext() {
						if (stop) {
							return true;
						}
						if (!take) {
							return true;
						}
						take = false;						
						if (iterator.hasNext() && pred.invoke(toReturn = iterator.next())) {
							return true;
						}
						else {
							stop = true;
							return false;
						}							
					}

					@Override
					public T next() {
						if (!take) {
							take = true;
							return toReturn;
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
	
	public CQueryCollection<T> takeWhile(final PI<? super T> pred) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private T toReturn;
					private boolean take = true;
					private boolean stop = false;
					private int index = 0;
					
					@Override
					public boolean hasNext() {
						if (stop) {
							return true;
						}
						if (!take) {
							return true;
						}
						take = false;						
						if (iterator.hasNext() && pred.invoke(toReturn = iterator.next(), index++)) {
							return true;
						}
						else {
							stop = true;
							return false;
						}							
					}

					@Override
					public T next() {
						if (!take) {
							take = true;
							return toReturn;
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

	public <R extends Comparable<R>> OrderByCollection<T> orderBy(F<? super T, R> selector) {
        return new OrderByCollection<T>(this, new AscendingComparator<T, R>(selector));
	}

	public <R extends Comparable<R>> OrderByCollection<T> orderByDescending(F<? super T, R> selector) {
		return new OrderByCollection<T>(this, new DescendingComparator<T, R>(selector));
	}
    
    public <R> OrderByCollection<T> orderBy(F<? super T, R> selector, Comparator<? super R> comparator) {
        return new OrderByCollection<T>(this, new CustomAscentingComparator<T, R>(selector, comparator));
    }

    public <R> OrderByCollection<T> orderByDescending(F<? super T, R> selector, Comparator<? super R> comparator) {
        return new OrderByCollection<T>(this, new CustomDescendingComparator<T, R>(selector, comparator));
    }

	public CQueryCollection<T> reverse() {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private List<T> reversed;
					private int i = 0;
					
					@Override
					public boolean hasNext() {
						if (reversed != null) {
							return i < reversed.size();
						}
						else {
							return iterator.hasNext();
						}						
					}

					@Override
					public T next() {
						if (reversed == null) {
							reversed = new ArrayList<T>();							
							while (iterator.hasNext()) {
								reversed.add(iterator.next());
							}
							Collections.reverse(reversed);
						}
						return reversed.get(i++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();						
					}					
				};
			}
		};
	}
	
	public CQueryCollection<T> concat(final Iterable<? extends T> iterable2) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {				
				return new Iterator<T>() {
					private Iterator<? extends T> iterator = CQueryCollection.this.iterator();
					private boolean usingIt2 = false;
					
					@Override
					public boolean hasNext() {
						if (iterator.hasNext()) {
							return true;
						}
						if (!usingIt2) {
							iterator = iterable2.iterator();
							usingIt2 = true;
							return iterator.hasNext();
						}
						return false;
					}

					@Override
					public T next() {
						return iterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

    public <Q extends T> CQueryCollection<T> concat(Q[] array) {
        return concat(from(array));
    }

    // greately limited in use
    public CQueryCollection<CQueryCollection<T>> split(final int collectionLength) {
    	if (collectionLength < 1) {
    		throw new IllegalArgumentException("collectionLength must be positive");
    	}
        return new CQueryCollection<CQueryCollection<T>>() {
            @Override
            public Iterator/*<CQueryCollection<T>>*/ iterator() {
            	return new Iterator<CQueryCollection<T>>() {
                    Iterator<T> iterator = CQueryCollection.this.iterator();
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public CQueryCollection<T> next() {
                        return new CQueryCollection<T>() {
                            @Override
                            public Iterator<T> iterator() {
                                return new Iterator<T>() {
                                    int pos = 0;
                                    @Override
                                    public boolean hasNext() {
                                        return pos < collectionLength && iterator.hasNext();
                                    }

                                    @Override
                                    public T next() {
                                        if (pos >= collectionLength) {
                                            throw new IllegalStateException();
                                        }
                                        ++pos;
                                        return iterator.next();
                                    }

                                    @Override
                                    public void remove() {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
	
	public String toString() {
		return toString(",", "[", "]");
	}
	public String toString(String separator) {
		StringBuilder sb = new StringBuilder();
		toStringHelper(sb, separator);		
		return sb.toString();
	}
	
	public String toString(String separator, String prefix, String suffix) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		toStringHelper(sb, separator);
		if (suffix != null) {
			sb.append(suffix);
		}
		return sb.toString();
	}
	
	private void toStringHelper(StringBuilder sb, String separator) {
		boolean first = true;
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			if (!first) {
				sb.append(separator);				
			}
			else {
				first = false;
			}
			T t = it.next();
			sb.append(t);
		}
	}
	
	public String toString(String separator, String prefix, String suffix, String format) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		boolean first = true;
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			if (!first) {
				sb.append(separator);				
			}
			else {
				first = false;
			}
			T t = it.next();
			sb.append(String.format(format, t));
		}
		if (suffix != null) {
			sb.append(suffix);
		}
		return sb.toString();
	}

    @Override
	public Object[] toArray() {
		return toList().toArray();
//      Object[] t = new Object[size()];
//		int i = 0;
//		Iterator<T> it = iterator();
//		while (it.hasNext()) {
//			t[i++] = it.next();
//		}
//		return t;
	}

    //@SuppressWarnings("unchecked")
    @Override
	public <R> R[] toArray(R[] t) {
        return toList().toArray(t);
//		int size = size();           
//		if (t.length < size) {
//	        t = (T[])Array.newInstance(t.getClass().getComponentType(), size);
//	    }
//		else if (t.length > size) {
//	        t[size] = null;
//	    }
//		int i = 0;
//		Iterator<T> it = iterator();
//		while (it.hasNext()) {
//			t[i++] = it.next();
//		}
//		return t;
	}
	
	public <R> CQueryCollection<R> cast(final Class<R> clazz) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new Iterator<R>() {
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public R next() {
						T t = iterator.next();
						return clazz.cast(t);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};				
			}			
		};
	}
	
	public <R> CQueryCollection<R> ofType(final Class<R> clazz) {
		return new CQueryCollection<R>() {
			@Override 
			public Iterator<R> iterator() {				
				return new Iterator<R>() {	
					private Iterator<T> iterator = CQueryCollection.this.iterator();
					private R nextValue;
										
					@SuppressWarnings("unchecked")
					@Override
					public boolean hasNext() {
						if (nextValue != null) {
							return true;
						}		
						while (iterator.hasNext()) {
							T t = iterator.next();
							if (clazz.isInstance(t)) {
								nextValue = (R)t;
								return true;
							}
						}
						return false;
					}
				
					@Override
					public R next() {
						if (nextValue == null) {
							throw new IllegalStateException();
						}
						R r = nextValue;
						nextValue = null;
						return r;
					}
				
					@Override
					public void remove() {
						throw new UnsupportedOperationException();	
					}
				};
			}			
		};
	}
	
	public List<T> toList() {
		//return new ArrayList<T>(this);

        List<T> list = new ArrayList<T>();
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			list.add(t);
		}
		return list;
	}		
	
	public Set<T> toSet() {
		// return new HashSet<T>(this);
        Set<T> set = new HashSet<T>();
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			set.add(t);
		}
		return set;
	}
	
	public <K> Map<K, T> toMap(F<? super T, K> keySelector) {
		Map<K, T> map = new HashMap<K, T>();
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			map.put(keySelector.invoke(t), t);
		}
		return map;
	}
	
	public <K, V> Map<K, V> toMap(F<? super T, K> keySelector, F<? super T, V> valueSelector) {
		Map<K, V> map = new HashMap<K, V>();
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			map.put(keySelector.invoke(t), valueSelector.invoke(t));
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public CQueryCollection<T> defaultIfEmpty(T default_) {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			return this;
		}
		return from(Arrays.asList(default_));
	}
	
	public T first() {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			return it.next();
		}
		throw new IllegalArgumentException("Selecting first element of empty collection");		
	}
	
	public T firstOrDefault(T default_) {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			return it.next();
		}
		return default_;
	}
	
	public T first(P<? super T> pred) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				return t;
			}
		}
		throw new IllegalArgumentException("Selecting first element of empty collection");		
	}
	
	public T firstOrDefault(P<? super T> pred, T default_) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				return t;
			}
		}
		return default_;
	}
	
	public T last() {
		Iterator<T> it = iterator();
		if (!it.hasNext()) {
			throw new IllegalArgumentException("Selecting last element of empty collection");
		}		
		T last = null;
		while (it.hasNext()) {
			last = it.next();
		}
		return last;
	}
	
	public T lastOrDefault(T default_) {
		Iterator<T> it = iterator();
		if (!it.hasNext()) {
			return default_;
		}		
		T last = null;
		while (it.hasNext()) {
			last = it.next();
		}
		return last;
	}
	
	public T last(P<? super T> pred) {
		Iterator<T> it = iterator();
		T last = null;
		boolean found = false;
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				last = t;
				found = true;
			}
		}
		if (found) {
			return last;
		}
		throw new IllegalArgumentException("Selecting last element of empty collection");
	}
	
	public T lastOrDefault(P<? super T> pred, T default_) {
		Iterator<T> it = iterator();
		T last = null;
		boolean found = false;
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				last = t;
				found = true;
			}
		}
		if (found) {
			return last;
		}
		return default_;
	}
	
	public T single() {
		Iterator<T> it = iterator();
		T t = it.next();
		if (it.hasNext()) {
			throw new IllegalStateException();
		}
		return t;
	}
	
	public T single(P<? super T> pred) {
		Iterator<T> it = iterator();
		T result = null;
		boolean hasResult = false;		
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				if (!hasResult) {
					result = t;
					hasResult = true;
				}
				else {
					throw new IllegalStateException();
				}					
			}
		}
		
		if (hasResult) {
			return result;
		}
		else {
			throw new IllegalStateException();
		}
	}
	
	public T singleOrDefault(T default_) {
		Iterator<T> it = iterator();
		if (!it.hasNext()) {
			return default_;
		}
		T t = it.next();
		if (it.hasNext()) {
			throw new IllegalStateException();
		}
		return t;
	}
	
	public T singleOrDefault(P<? super T> pred, T default_) {
		Iterator<T> it = iterator();
		T result = null;
		boolean hasResult = false;		
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				if (!hasResult) {
					result = t;
					hasResult = true;
				}
				else {
					throw new IllegalStateException();
				}					
			}
		}
		
		if (hasResult) {
			return result;
		}
		else {
			return default_;
		}
	}
	
	public T elementAt(int index) {
		int i = 0;
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (i++ == index) {
				return t;
			}
		}
		throw new IllegalArgumentException("There is no element at index: " + index);
	}
	
	public T elementAtOrDefault(int index, T default_) {
		int i = 0;
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (i++ == index) {
				return t;
			}
		}
		return default_;
	}

    @Override
	public int size() {
		return count();
	}
	
	public int count() {
		Iterator<T> it = iterator();
		int size = 0;
		while (it.hasNext()) {
			it.next();
			++size;
		}
		return size;
	}
	
	public int count(P<? super T> pred) {
		Iterator<T> it = iterator();
		int size = 0;
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				++size;
			}
		}
		return size;
	}

    @Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}
	
	public boolean any() {
		return iterator().hasNext();
	}
	
	public boolean any(P<? super T> pred) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (pred.invoke(t)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean all(P<? super T> pred) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (!pred.invoke(t)) {
				return false;
			}
		}
		return true;
	}

    @Override
	public boolean contains(Object value) {
		Iterator<T> it = iterator();
		if (value == null) {
			while (it.hasNext()) {
				T t = it.next();
				if (t == null) {
					return true;
				}
			}	
		}
		else {
			while (it.hasNext()) {
				Object t = it.next();
				if (value.equals(t)) {
					return true;
				}
			}
		}
		return false;
	}

    public boolean contains(T value, EqualityComparer<? super T> comparer) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (comparer.equals(value, t)) {
				return true;
			}
		}
		return false;
	}

    public boolean containsAny(T... values) {
        Set<T> index = toSet();
        for (T t : values) {
            if (index.contains(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAny(T[] values, EqualityComparer<T> comparer) {
        Set<T> index = new CustomEqualitySet<T>(comparer);
		for (T t : this) {
			index.add(t);
		}
        for (T t : values) {
            if (index.contains(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAny(Iterable<T> values) {
        Set<T> index = toSet();
        for (T t : values) {
            if (index.contains(t)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsAny(Iterable<T> values, EqualityComparer<T> comparer) {
        Set<T> index = new CustomEqualitySet<T>(comparer);
		for (T t : this) {
			index.add(t);
		}
        for (T t : values) {
            if (index.contains(t)) {
                return true;
            }
        }
        return false;
    }
    public boolean containsAll(T... values) {
        Set<T> index = toSet();
        for (T t : values) {
            if (!index.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAll(T[] values, EqualityComparer<T> comparer) {
        Set<T> index = new CustomEqualitySet<T>(comparer);
		for (T t : this) {
			index.add(t);
		}
        for (T t : values) {
            if (!index.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAll(Iterable<T> values) {
        Set<T> index = toSet();
        for (T t : values) {
            if (!index.contains(t)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Set<?> index = toSet();
        for (Object t : c) {
            if (!index.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAll(Iterable<T> values, EqualityComparer<T> comparer) {
        Set<T> index = new CustomEqualitySet<T>(comparer);
		for (T t : this) {
			index.add(t);
		}
        for (T t : values) {
            if (!index.contains(t)) {
                return false;
            }
        }
        return true;
    }

    // wy element jest seedem

	public T aggregate(F2<? super T, ? super T, ? extends T> func) {
        Iterator<T> it = iterator();
        if (!it.hasNext()) {
            return null;
        }
        T acc = it.next();
        while (it.hasNext()) {
            acc = func.invoke(acc, it.next());
        }
        return acc;
    }

	public <R> R aggregate(R seed, F2<? super R, ? super T, ? extends R> func) {
        Iterator<T> it = iterator();
        R acc = seed;
        while (it.hasNext()) {
            acc = func.invoke(acc, it.next());
        }
        return acc;
    }

	public <R, S> S aggregate(R seed, F2<? super R, ? super T, ? extends R> func, F<? super R, S> resultSelector) {
        Iterator<T> it = iterator();
        R acc = seed;
        while (it.hasNext()) {
            acc = func.invoke(acc, it.next());
        }
        return resultSelector.invoke(acc);
    }

	@SuppressWarnings("unchecked")
	public T min() {
		T min = null;
		
		for (T t : this) {			
			if (t != null) {
				if (min != null) {
					if (((Comparable<T>)t).compareTo(min) < 0) {
						min = t;
					}
				}
				else {
					min = t;				
				}
			}
				
		}
		return min;
	}
	
	public T min(Comparator<? super T> comparator) {
		T min = null;
		
		for (T t : this) {			
			if (t != null) {
				if (min != null) {
					if (comparator.compare(t, min) < 0) {
						min = t;
					}
				}
				else {
					min = t;				
				}
			}
				
		}
		return min;
	}
	
	public <R extends Comparable<R>> R min(F<? super T, R> selector){
		R min = null;
		
		for (T t : this) {
			R v = selector.invoke(t);
			if (v != null) {
				if (min != null) {
					if (v.compareTo(min) < 0) {
						min = v;
					}
				}
				else {
					min = v;				
				}
			}
		}
		return min;
	}
	
	@SuppressWarnings("unchecked")
	public T max() {
		T max = null;
		
		for (T t : this) {			
			if (t != null) {
				if (max != null) {
					if (((Comparable<T>)t).compareTo(max) > 0) {
						max = t;
					}
				}
				else {
					max = t;				
				}
			}
				
		}
		return max;
	}
	
	public T max(Comparator<? super T> comparator) {
		T max = null;
		
		for (T t : this) {			
			if (t != null) {
				if (max != null) {
					if (comparator.compare(t, max) > 0) {
						max = t;
					}
				}
				else {
					max = t;				
				}
			}
				
		}
		return max;
	}
	
	public <R extends Comparable<R>> R max(F<? super T, R> selector){
		R max = null;
		
		for (T t : this) {
			R v = selector.invoke(t);
			if (v != null) {
				if (max != null) {
					if (v.compareTo(max) > 0) {
						max = v;
					}
				}
				else {
					max = v;				
				}
			}
		}
		return max;
	}

    @SuppressWarnings("unchecked")
	public T sum() {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            T r = it.next();
            if (r != null) {
                if (r instanceof Integer) {
                    int sum = (Integer)r;
                    while (it.hasNext()) {
                        r = it.next();
                        if (r != null) {
                            sum += (Integer)r;
                        }
                    }
                    return (T)new Integer(sum);
                }
                if (r instanceof Long) {
                    long sum = (Long)r;
                    while (it.hasNext()) {
                        r = it.next();
                        if (r != null) {
                            sum += (Long)r;
                        }
                    }
                    return (T)new Long(sum);
                }
                if (r instanceof Double) {
                    double sum = (Double)r;
                    while (it.hasNext()) {
                        r = it.next();
                        if (r != null) {
                            sum += (Double)r;
                        }
                    }
                    return (T)new Double(sum);
                }
                if (r instanceof Float) {
                    float sum = (Float)r;
                    while (it.hasNext()) {
                        r = it.next();
                        if (r != null) {
                            sum += (Float)r;
                        }
                    }
                    return (T)new Float(sum);
                }
                if (r instanceof BigDecimal) {
                    BigDecimal sum = (BigDecimal)r;
                    while (it.hasNext()) {
                        r = it.next();
                        if (r != null) {
                            sum = sum.add((BigDecimal)r);
                        }
                    }
                    return (T)sum;
                }
                if (r instanceof BigInteger) {
                    BigInteger sum = (BigInteger)r;
                    while (it.hasNext()) {
                        r = it.next();
                        if (r != null) {
                            sum = sum.add((BigInteger)r);
                        }
                    }
                    return (T)sum;
                }
                throw new UnsupportedOperationException(r.getClass().getName() + " can not be sumed up");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public <R extends Number> R sum(F<? super T, R> selector) {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            R r = selector.invoke(it.next());
            if (r != null) {
                if (r instanceof Integer) {
                    int sum = (Integer)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        if (r != null) {
                            sum += (Integer)r;
                        }
                    }
                    return (R)new Integer(sum);
                }
                if (r instanceof Long) {
                    long sum = (Long)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        if (r != null) {
                            sum += (Long)r;
                        }
                    }
                    return (R)new Long(sum);
                }
                if (r instanceof Double) {
                    double sum = (Double)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        if (r != null) {
                            sum += (Double)r;
                        }
                    }
                    return (R)new Double(sum);
                }
                if (r instanceof Float) {
                    float sum = (Float)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        if (r != null) {
                            sum += (Float)r;
                        }
                    }
                    return (R)new Float(sum);
                }
                if (r instanceof BigDecimal) {
                    BigDecimal sum = (BigDecimal)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        if (r != null) {
                            sum = sum.add((BigDecimal)r);
                        }
                    }
                    return (R)sum;
                }
                if (r instanceof BigInteger) {
                    BigInteger sum = (BigInteger)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        if (r != null) {
                            sum = sum.add((BigInteger)r);
                        }
                    }
                    return (R)sum;
                }
                throw new UnsupportedOperationException(r.getClass().getName() + " can not be sumed up");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public T average() {
        Iterator<T> it = iterator();
        int cnt = 0;
        while (it.hasNext()) {
            T r = it.next();
            ++cnt;
            if (r != null) {
                if (r instanceof Integer) {
                    int sum = (Integer)r;
                    while (it.hasNext()) {
                        r = it.next();
                        ++cnt;
                        if (r != null) {
                            sum += (Integer)r;
                        }
                    }
                    return (T)new Integer(sum/cnt);
                }
                if (r instanceof Long) {
                    long sum = (Long)r;
                    while (it.hasNext()) {
                        r = it.next();
                        ++cnt;
                        if (r != null) {
                            sum += (Long)r;
                        }
                    }
                    return (T)new Long(sum/cnt);
                }
                if (r instanceof Double) {
                    double sum = (Double)r;
                    while (it.hasNext()) {
                        r = it.next();
                        ++cnt;
                        if (r != null) {
                            sum += (Double)r;
                        }
                    }
                    return (T)new Double(sum/cnt);
                }
                if (r instanceof Float) {
                    float sum = (Float)r;
                    while (it.hasNext()) {
                        r = it.next();
                        ++cnt;
                        if (r != null) {
                            sum += (Float)r;
                        }
                    }
                    return (T)new Float(sum/cnt);
                }
                if (r instanceof BigDecimal) {
                    BigDecimal sum = (BigDecimal)r;
                    while (it.hasNext()) {
                        r = it.next();
                        ++cnt;
                        if (r != null) {
                            sum = sum.add((BigDecimal)r);
                        }
                    }
                    return (T)sum.divide(new BigDecimal(cnt));
                }
                if (r instanceof BigInteger) {
                    BigInteger sum = (BigInteger)r;
                    while (it.hasNext()) {
                        r = it.next();
                        ++cnt;
                        if (r != null) {
                            sum = sum.add((BigInteger)r);
                        }
                    }
                    return (T)sum.divide(new BigInteger(String.valueOf(cnt)));
                }
                throw new UnsupportedOperationException(r.getClass().getName() + " can not be sumed up");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public <R extends Number> R average(F<? super T, R> selector) {
        Iterator<T> it = iterator();
        int cnt = 0;
        while (it.hasNext()) {
            R r = selector.invoke(it.next());
            ++cnt;
            if (r != null) {
                if (r instanceof Integer) {
                    int sum = (Integer)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        ++cnt;
                        if (r != null) {
                            sum += (Integer)r;
                        }
                    }
                    return (R)new Integer(sum/cnt);
                }
                if (r instanceof Long) {
                    long sum = (Long)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        ++cnt;
                        if (r != null) {
                            sum += (Long)r;
                        }
                    }
                    return (R)new Long(sum/cnt);
                }
                if (r instanceof Double) {
                    double sum = (Double)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        ++cnt;
                        if (r != null) {
                            sum += (Double)r;
                        }
                    }
                    return (R)new Double(sum/cnt);
                }
                if (r instanceof Float) {
                    float sum = (Float)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        ++cnt;
                        if (r != null) {
                            sum += (Float)r;
                        }
                    }
                    return (R)new Float(sum/cnt);
                }
                if (r instanceof BigDecimal) {
                    BigDecimal sum = (BigDecimal)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        ++cnt;
                        if (r != null) {
                            sum = sum.add((BigDecimal)r);
                        }
                    }
                    return (R)sum.divide(new BigDecimal(cnt));
                }
                if (r instanceof BigInteger) {
                    BigInteger sum = (BigInteger)r;
                    while (it.hasNext()) {
                        r = selector.invoke(it.next());
                        ++cnt;
                        if (r != null) {
                            sum = sum.add((BigInteger)r);
                        }
                    }
                    return (R)sum.divide(new BigInteger(String.valueOf(cnt)));
                }
                throw new UnsupportedOperationException(r.getClass().getName() + " can not be sumed up");
            }
        }
        return null;
    }
    
	private static abstract class ExceptIterator<T> implements Iterator<T> {
		private final Iterator<T> iterator;
		private final Iterable<? extends T> second;
		private Set<T> secondIndex;
		private Set<T> returned;		
		private T toReturn;
		boolean hasResult = false;
		
		public ExceptIterator(Iterable<T> first, Iterable<? extends T> second) {
			this.iterator = first.iterator();
			this.second = second;
		}
		
		@Override
		public boolean hasNext() {
			if (hasResult) {
				return true;
			}
			if (secondIndex == null) {
				secondIndex = createIndexSet();
				returned = createIndexSet();
				for (T t : second) {
					secondIndex.add(t);
				}
			}						
			while (iterator.hasNext()) {
				toReturn = iterator.next();
				if (!secondIndex.contains(toReturn) && !returned.contains(toReturn)) {
					hasResult = true;
					return true;
				}
			}
			hasResult = false;
			return false;
		}

		@Override
		public T next() {
			if (hasResult) {
				hasResult = false;
				returned.add(toReturn);
				return toReturn;							
			}
			throw new IllegalStateException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();						
		}
		
		protected abstract Set<T> createIndexSet();
	}
	
	public CQueryCollection<T> except(final Iterable<? extends T> second) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new ExceptIterator<T>(CQueryCollection.this, second) {
					@Override
					protected Set<T> createIndexSet() {
						return new HashSet<T>();
					}
					
				};
			}
		};
	}
	
	public CQueryCollection<T> except(final Iterable<? extends T> second, final EqualityComparer<? super T> comparer) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new ExceptIterator<T>(CQueryCollection.this, second) {
					@Override
					protected Set<T> createIndexSet() {
						return new CustomEqualitySet<T>(comparer);						
					}
					
				};
			}
		};
	}

    public <Q extends T> CQueryCollection<T> except(Q[] second) {
        return except(from(second));
    }

    public <Q extends T> CQueryCollection<T> except(Q[] second, final EqualityComparer<? super T> comparer) {
        return except(from(second), comparer);
    }

	private static abstract class IntersectIterator<T> implements Iterator<T> {
		private Set<T> secondIndex;
		private Set<T> returned;
		private final Iterator<T> iterator;
		private final Iterable<? extends T> second;
		private T toReturn;
		private boolean hasResult = false;
		
		public IntersectIterator(Iterable<T> first, Iterable<? extends T> second) {
			this.iterator = first.iterator();
			this.second = second;
		}
		
		@Override
		public boolean hasNext() {
			if (hasResult) {
				return true;
			}
			if (secondIndex == null) {
				secondIndex = createIndexSet();
				returned = createIndexSet();
				for (T t : second) {
					secondIndex.add(t);
				}
			}						
			while (iterator.hasNext()) {
				toReturn = iterator.next();
				if (secondIndex.contains(toReturn) && !returned.contains(toReturn)) {
					hasResult = true;
					return true;
				}
			}
			hasResult = false;
			return false;
		}

		@Override
		public T next() {
			if (hasResult) {
				hasResult = false;
				returned.add(toReturn);
				return toReturn;							
			}
			throw new IllegalStateException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();						
		}
		
		protected abstract Set<T> createIndexSet();
	}

	
	public CQueryCollection<T> intersect(final Iterable<? extends T> second) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new IntersectIterator<T>(CQueryCollection.this, second) {
					@Override
					protected Set<T> createIndexSet() {
						return new HashSet<T>();
					}
				};
			}
		};
	}
	
	public CQueryCollection<T> intersect(final Iterable<? extends T> second, final EqualityComparer<? super T> comparer) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new IntersectIterator<T>(CQueryCollection.this, second) {
					@Override
					protected Set<T> createIndexSet() {
						return new CustomEqualitySet<T>(comparer);
					}
				};
			}
		};
	}

    public <Q extends T> CQueryCollection<T> intersect(Q[] second) {
        return intersect(from(second));
    }

    public <Q extends T> CQueryCollection<T> intersect(Q[] second, final EqualityComparer<? super T> comparer) {
        return intersect(from(second), comparer);
    }

	private static abstract class UnionIterator<T> implements Iterator<T> {			
		private Iterator<? extends T> iterator;
		private final Iterable<? extends T> second;
		private Set<T> returned;
		private T toReturn;
		private boolean hasResult = false;
		private boolean usingIt2 = false;
		
		public UnionIterator(Iterable<T> first, Iterable<? extends T> second) {
			this.iterator = first.iterator();
			this.second = second;
			this.returned = createIndexSet();
		}
		
		@Override
		public boolean hasNext() {
			if (hasResult) {
				return true;
			}
			while (iterator.hasNext()) {
				toReturn = iterator.next();
				if (!returned.contains(toReturn)) {
					hasResult = true;
					return true;
				}
			}
			if (!usingIt2) {
				iterator = second.iterator();
				while (iterator.hasNext()) {
					toReturn = iterator.next();
					if (!returned.contains(toReturn)) {
						hasResult = true;
						return true;
					}
				}
			}
			hasResult = false;
			return false;
		}

		@Override
		public T next() {
			if (hasResult) {
				hasResult = false;
				returned.add(toReturn);
				return toReturn;							
			}
			throw new IllegalStateException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();						
		}
		
		protected abstract Set<T> createIndexSet();
	}

	
	public CQueryCollection<T> union(final Iterable<? extends T> second) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new UnionIterator<T>(CQueryCollection.this, second) {
					@Override
					protected Set<T> createIndexSet() {
						return new HashSet<T>();
					}					
				};
			}
		};
	}

	public CQueryCollection<T> union(final Iterable<? extends T> second, final EqualityComparer<? super T> comparer) {
		return new CQueryCollection<T>() {
			@Override
			public Iterator<T> iterator() {
				return new UnionIterator<T>(CQueryCollection.this, second) {
					@Override
					protected Set<T> createIndexSet() {
						return new CustomEqualitySet<T>(comparer);
					}					
				};
			}
		};
	}

    public <Q extends T> CQueryCollection<T> union(Q[] second) {
        return union(from(second));
    }

    public <Q extends T> CQueryCollection<T> union(Q[] second, final EqualityComparer<? super T> comparer) {
        return union(from(second), comparer);
    }

	private static abstract class JoinIterator<Outer, Inner, K, R> implements Iterator<R> {
		private final Iterator<? extends Outer> iterator;
		private final Iterable<? extends Inner> innerIterable;
		private Map<K, List<Inner>> innerIndex;
		private Outer outer;
		private List<Inner> innerList;
		int innerListIndex;
		
		private final F<? super Outer, K> outerKeySelector;
		private final F<? super Inner, K> innerKeySelector;
		private final F2<? super Outer,? super Inner,R> resultSelector;
		
		public JoinIterator(Iterable<? extends Outer> outer, Iterable<? extends Inner> inner, F<? super Outer, K> outerKeySelector, F<? super Inner, K> innerKeySelector, F2<? super Outer,? super Inner,R> resultSelector) {
			this.outerKeySelector = outerKeySelector;
			this.innerKeySelector = innerKeySelector;
			this.resultSelector = resultSelector;
			this.iterator = outer.iterator();
			this.innerIterable = inner;
		}
		
		@Override
		public boolean hasNext() {
			if (innerList != null && innerListIndex < innerList.size()) {
				return true;
			}
			if (innerIndex == null && iterator.hasNext()) {
				createIndex();
			}
			while (iterator.hasNext()) {
				outer = iterator.next();							
				K k = outerKeySelector.invoke(outer);
				innerList = innerIndex.get(k);
				if (innerList != null) {
					innerListIndex = 0;
					return true;
				}
			}
			return false;
		}

		@Override
		public R next() {
			return resultSelector.invoke(outer, innerList.get(innerListIndex++));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();						
		}
		
		private void createIndex() {
			innerIndex = createInnerIndexMap();
			for (Inner i : innerIterable) {
				K k = innerKeySelector.invoke(i);
				List<Inner> list = innerIndex.get(k);
				if (list != null) {
					list.add(i);
				}
				else {
					list = new ArrayList<Inner>();
					list.add(i);
					innerIndex.put(k, list);
				}
			}
		}
		
		protected abstract Map<K, List<Inner>> createInnerIndexMap();
	}

	public <Inner, K, R> CQueryCollection<R> join(final Iterable<Inner> inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super Inner,R> resultSelector) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new JoinIterator<T, Inner, K, R>(CQueryCollection.this, inner, outerKeySelector, innerKeySelector, resultSelector) {
					@Override
					protected Map<K, List<Inner>> createInnerIndexMap() {						
						return new HashMap<K, List<Inner>>();
					}
				};
			}
		};
	}
	
	public <Inner, K, R> CQueryCollection<R> join(final Iterable<Inner> inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super Inner,R> resultSelector, final EqualityComparer<? super K> keyComparer) {
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return new JoinIterator<T, Inner, K, R>(CQueryCollection.this, inner, outerKeySelector, innerKeySelector, resultSelector) {
					@Override
					protected Map<K, List<Inner>> createInnerIndexMap() {						
						return new CustomEqualityMap<K, List<Inner>>(keyComparer);
					}
				};
			}
		};
	}

    public <Inner, K, R> CQueryCollection<R> join(Inner[] inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super Inner,R> resultSelector) {
        return join(from(inner), outerKeySelector, innerKeySelector, resultSelector);
    }

    public <Inner, K, R> CQueryCollection<R> join(Inner[] inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super Inner,R> resultSelector, final EqualityComparer<? super K> keyComparer) {
        return join(from(inner), outerKeySelector, innerKeySelector, resultSelector, keyComparer);
    }
	
	public <K> GroupByCollection<K, T> groupBy(F<? super T, K> keySelector) {
		Map<K, List<T>> groups = new HashMap<K, List<T>>();
		groupByHelper(groups, keySelector);
		return new GroupByCollection<K, T>(groups);
	}
	
	public <K> GroupByCollection<K, T> groupBy(F<? super T, K> keySelector, EqualityComparer<K> keyComparer) {
		Map<K, List<T>> groups = new CustomEqualityMap<K, List<T>>(keyComparer);
		groupByHelper(groups, keySelector);		
		return new GroupByCollection<K, T>(groups);
	}
	
	public <K, E> GroupByCollection<K, E> groupBy(F<? super T, K> keySelector, F<? super T, E> elementSelector) {
		Map<K, List<E>> groups = new HashMap<K, List<E>>();
		groupByHelper(groups, keySelector, elementSelector);		
		return new GroupByCollection<K, E>(groups);
	}
	
	public <K, E> GroupByCollection<K, E> groupBy(F<? super T, K> keySelector, F<? super T, E> elementSelector, EqualityComparer<? super K> keyComparer) {
		Map<K, List<E>> groups = new CustomEqualityMap<K, List<E>>(keyComparer);
		groupByHelper(groups, keySelector, elementSelector);		
		return new GroupByCollection<K, E>(groups);
	}
	
	
//	public <K, R> CQueryCollection<R> groupBy(F<T, K> keySelector, F<CQueryCollection<T>, R> resultSelector) {
//		Map<K, List<T>> groups = new HashMap<K, List<T>>();
//		groupByHelper(groups, keySelector);
//		return transform(groups, resultSelector);
//	}
	
//	public <K, R> Map<K, List<T>> groupBy(F<T, K> keySelector, F<List<T>, R> resultSelector, EqualityComparer<K> keyComparer) {
// WRR java shit
//	}
	
	public <K, E, R> CQueryCollection<R> groupBy(F<? super T, K> keySelector, F<? super T, E> elementSelector, F<? super CQueryCollection<E>, R> resultSelector) {
		Map<K, List<E>> groups = new HashMap<K, List<E>>();
		groupByHelper(groups, keySelector, elementSelector);
		return transform(groups, resultSelector);
	}
	
	public <K, E, R> CQueryCollection<R> groupBy(F<? super T, K> keySelector, F<? super T, E> elementSelector, F<? super CQueryCollection<E>, R> resultSelector, EqualityComparer<? super K> keyComparer) {
		Map<K, List<E>> groups =  new CustomEqualityMap<K, List<E>>(keyComparer);
		groupByHelper(groups, keySelector, elementSelector);
		return transform(groups, resultSelector);
	}
	
	
	private <K> void groupByHelper(Map<K, List<T>> groups, F<? super T, K> keySelector) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			K k = keySelector.invoke(t);
			List<T> list = groups.get(k);
			if (list != null) {
				list.add(t);
			}
			else {
				list = new ArrayList<T>();
				list.add(t);
				groups.put(k, list);
			}
		}
	}		
	
	private <K, E> void groupByHelper(Map<K, List<E>> groups, F<? super T, K> keySelector, F<? super T, E> elementSelector) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T t = it.next();
			K k = keySelector.invoke(t);
			E e = elementSelector.invoke(t);
			List<E> list = groups.get(k);
			if (list != null) {
				list.add(e);
			}
			else {
				list = new ArrayList<E>();
				list.add(e);
				groups.put(k, list);
			}
		}
	}
	
	private <K, E, R> CQueryCollection<R> transform(Map<K, List<E>> grouping, F<? super CQueryCollection<E>, R> resultSelector) {
		final List<R> result = new ArrayList<R>();
		for (Map.Entry<K, List<E>> group : grouping.entrySet()) {
			result.add(resultSelector.invoke(from(group.getValue())));
		}
		return new CQueryCollection<R>() {
			@Override
			public Iterator<R> iterator() {				
				return result.iterator();
			}			
			
			@Override
			public List<R> toList() {				
				return result;
			}
		};
	}
		
	@SuppressWarnings("rawtypes")
	public <Inner, K, R> CQueryCollection<R> groupJoin(final Iterable<Inner> inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super CQueryCollection<Inner>,R> resultSelector) {
		return groupJoinHelper(new HashMap<K, List[]>(), inner, outerKeySelector, innerKeySelector, resultSelector);
	}
	
	@SuppressWarnings("rawtypes")
	public <Inner, K, R> CQueryCollection<R> groupJoin(final Iterable<Inner> inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super CQueryCollection<Inner>,R> resultSelector, EqualityComparer<? super K> keyComparer) {
		return groupJoinHelper(new CustomEqualityMap<K, List[]>(keyComparer), inner, outerKeySelector, innerKeySelector, resultSelector);
	}

    public <Inner, K, R> CQueryCollection<R> groupJoin(Inner[] inner, final F<T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T, CQueryCollection<? super Inner>,R> resultSelector) {
        return groupJoin(from(inner), outerKeySelector, innerKeySelector, resultSelector);
    }

    public <Inner, K, R> CQueryCollection<R> groupJoin(Inner[] inner, final F<T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T, CQueryCollection<? super Inner>,R> resultSelector, EqualityComparer<? super K> keyComparer) {
        return groupJoin(from(inner), outerKeySelector, innerKeySelector, resultSelector, keyComparer);
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <Inner, K, R> CQueryCollection<R> groupJoinHelper(Map<K, List[]> groups, final Iterable<Inner> inner, final F<? super T, K> outerKeySelector, final F<? super Inner, K> innerKeySelector, final F2<? super T,? super CQueryCollection<Inner>,R> resultSelector) {
		for (T t : this) {
			K k = outerKeySelector.invoke(t);
			List[] lists = groups.get(k);
			if (lists != null) {
				lists[0].add(t);
			}
			else {
				lists = new List[]{ new ArrayList<T>(), new ArrayList<Inner>() };
				lists[0].add(t);
				groups.put(k, lists);
			}
		}
		for (Inner in : inner) {
			List[] lists = groups.get(innerKeySelector.invoke(in));
			if (lists != null) {
				if (lists[1] == null) {
					lists[1] = new ArrayList<Inner>();
				}
				lists[1].add(in);
			}
		}
		List<R> result = new ArrayList<R>();
		for (List[] lists : groups.values()) {
			for (T outerElem : (List<T>)lists[0]) {
				result.add(resultSelector.invoke(outerElem, from((List<Inner>)lists[1])));
			}			
		}
		return from(result);
	}

	public boolean sequenceEqual(Iterable<? extends T> second) {
		Iterator<T> it1 = iterator();
		Iterator<? extends T> it2 = second.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			T t1 = it1.next(), t2 = it2.next();
			if (!Lang.equals(t1, t2)) {
				return false;
			}
		}
		return !it1.hasNext() && !it2.hasNext();
	}
	
	public boolean sequenceEqual(Iterable<? extends T> second, EqualityComparer<? super T> comparer) {
		Iterator<T> it1 = iterator();
		Iterator<? extends T> it2 = second.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			T t1 = it1.next(), t2 = it2.next();
			if (!comparer.equals(t1, t2)) {
				return false;
			}
		}
		return !it1.hasNext() && !it2.hasNext();
	}
    
    public <Q extends T> boolean sequenceEqual(Q[] second) {
        return sequenceEqual(from(second));
    }

    public <Q extends T> boolean sequenceEqual(Q[] second, EqualityComparer<? super T> comparer) {
        return sequenceEqual(from(second), comparer);
    }
}
