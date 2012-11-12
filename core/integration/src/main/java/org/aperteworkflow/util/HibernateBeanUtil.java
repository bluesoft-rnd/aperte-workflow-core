package org.aperteworkflow.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentSet;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.util.lang.Lang;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class HibernateBeanUtil {
    private static class MyPersistentSetConverter extends CollectionConverter {
        public MyPersistentSetConverter(Mapper mapper) {
            super(mapper);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            PersistentSet ps = (PersistentSet) source;
            super.marshal(new HashSet(ps), writer, context);
        }

        @Override
        public boolean canConvert(Class type) {
            if (type == null) return false;
            return type.isAssignableFrom(PersistentSet.class);
        }
    }
    private final static XStream xs = new XStream();
    static {
        xs.registerConverter(new MyPersistentSetConverter(xs.getMapper()), XStream.PRIORITY_VERY_HIGH);
    }

    public static <T> T fetchHibernateData(T o) {
        try {
			if (o == null) return null;
			new LazyLoadExpander().expand(o);
			String s = xs.toXML(o);
			s = s.replace("class=\"org.hibernate.collection.PersistentSet\"", "class=\"java.util.HashSet\"");
			s = s.replaceAll("class=\"(.*?)_\\$\\$_javassist[^\"]*\"", "class=\"$1\"");
			T clone = (T)xs.fromXML(s);
			return clone;
		}
		catch (Exception e) {
			Logger.getLogger(HibernateBeanUtil.class.toString()).log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
    }

	private static class LazyLoadExpander {
		private final Set<Object> expanded = new HashSet<Object>();

		public <T> void expand(T t) {
			if (t == null || expanded.contains(t)) {
				return;
			}
			expanded.add(t);
			if (t instanceof PersistentEntity) {
				Hibernate.initialize(t);

				for (Field field : Lang.getFieldsFromClassAndAncestors(t.getClass())) {
					try {
						Object value = Lang.get(t, field.getName());
						if (value instanceof PersistentEntity) {
							expand(value);
						}
						if (value instanceof Collection) {
							for (Object obj : (Collection)value) {
								expand(obj);
							}
						}
					}
					catch (Exception e) {
					}
				}
			}
		}
	}
}
