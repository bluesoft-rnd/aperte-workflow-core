package org.aperteworkflow.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.hibernate.collection.PersistentSet;

import java.util.HashSet;

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
        if (o == null) return null;
        String s = xs.toXML(o);
        s = s.replace("class=\"org.hibernate.collection.PersistentSet\"", "class=\"java.util.HashSet\"");
        return (T) xs.fromXML(s);
    }
}
