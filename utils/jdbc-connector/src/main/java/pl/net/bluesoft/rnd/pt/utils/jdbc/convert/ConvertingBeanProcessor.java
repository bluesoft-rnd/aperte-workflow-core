package pl.net.bluesoft.rnd.pt.utils.jdbc.convert;

import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.wrappers.StringTrimmedResultSet;
import pl.net.bluesoft.util.criteria.lang.Formats;
import pl.net.bluesoft.util.lang.Strings;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static pl.net.bluesoft.util.lang.Formats.nvl;

public class ConvertingBeanProcessor extends BeanProcessor {
    public static final SimpleColumnNameConverter SIMPLE_COLUMN_CONVERTER = new SimpleColumnNameConverter();
    public static final CharacterResultValueConverter CHARACTER_RESULT_VALUE_CONVERTER = new CharacterResultValueConverter();

    protected ColumnNameConverter columnConverter;
    protected ResultSetValueConverter resultSetValueConverter;

    public ConvertingBeanProcessor() {
        this(null, null);
    }

    public ConvertingBeanProcessor(ColumnNameConverter columnConverter) {
        this(columnConverter, null);
    }

    public ConvertingBeanProcessor(ColumnNameConverter columnConverter, ResultSetValueConverter resultSetValueConverter) {
        super();
        this.columnConverter = nvl(columnConverter, SIMPLE_COLUMN_CONVERTER);
        this.resultSetValueConverter = nvl(resultSetValueConverter, CHARACTER_RESULT_VALUE_CONVERTER);
    }

    @Override
    public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
        return super.toBean(StringTrimmedResultSet.wrap(rs), type);
    }

    @Override
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
        return super.toBeanList(StringTrimmedResultSet.wrap(rs), type);
    }

    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {
        int cols = rsmd.getColumnCount();
        int columnToProperty[] = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);
        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (!Strings.hasText(columnName)) {
                columnName = rsmd.getColumnName(col);
            }
            if (columnConverter != null) {
                columnName = columnConverter.convert(columnName);
            }
            for (int i = 0; i < props.length; i++) {
                if (columnName.equalsIgnoreCase(props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }
        return columnToProperty;
    }

    @Override
    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        if (resultSetValueConverter != null && resultSetValueConverter.supports(propType)) {
            return resultSetValueConverter.convert(rs.getObject(index), propType);
        }
        return super.processColumn(rs, index, propType);
    }

    public static class CharacterResultValueConverter extends ResultSetValueConverter<Character> {
        @Override
        public Character convert(Object value, Class<?> propertyType) {
            return value != null ? value.toString().charAt(0) : null;
        }
    }

    public static class SimpleColumnNameConverter implements ColumnNameConverter {
        @Override
        public String convert(String columnName) {
            return Formats.underscoreToCamel(columnName);
        }
    }
}
