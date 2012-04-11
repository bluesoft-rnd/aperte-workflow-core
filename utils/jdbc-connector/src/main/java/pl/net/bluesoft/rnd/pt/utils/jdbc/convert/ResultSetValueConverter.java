package pl.net.bluesoft.rnd.pt.utils.jdbc.convert;

import java.lang.reflect.ParameterizedType;

public abstract class ResultSetValueConverter<T> {
    protected Class<T> type;

    protected ResultSetValueConverter() {
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected ResultSetValueConverter(Class<T> clazz) {
        this.type = clazz;
    }

    public boolean supports(Class<?> clazz) {
        return type.isAssignableFrom(clazz);
    }

    public abstract T convert(Object value, Class<?> propertyType);

}
