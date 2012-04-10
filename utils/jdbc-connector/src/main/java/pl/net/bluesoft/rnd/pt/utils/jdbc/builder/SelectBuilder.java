package pl.net.bluesoft.rnd.pt.utils.jdbc.builder;

import pl.net.bluesoft.util.criteria.*;
import pl.net.bluesoft.util.criteria.lang.Formats;
import pl.net.bluesoft.rnd.pt.utils.jdbc.exception.TableNameNotFoundException;
import pl.net.bluesoft.rnd.pt.utils.jdbc.helper.Classes;
import pl.net.bluesoft.util.lang.Strings;

import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.util.criteria.lang.Formats.join;

public class SelectBuilder extends AbstractQueryBuilder {
    private QueryMetadata queryMetadata;
    private String columns;
    private Class clazz;
    private String tableName;
    private OrderByCriterion orderBy;

    private List<String> properties = new ArrayList<String>();
    private List<Criterion> criteria = new ArrayList<Criterion>();

    public class SelectQueryMetadata extends QueryMetadata {
        @Override
        public String getColumnName(String fieldName) {
            return clazz != null ? Formats.camelToUnderscore(fieldName) : fieldName;
        }

        @Override
        public String formatValue(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof CharSequence || value instanceof Character) {
                return "'" + value.toString() + "'";
            }
            return value.toString();
        }
    }

    public SelectBuilder() {
        this.queryMetadata = new SelectQueryMetadata();
    }

    public SelectBuilder(QueryMetadata queryMetadata) {
        this.queryMetadata = queryMetadata;
    }

    public SelectBuilder all() {
        this.columns = ALL;
        return this;
    }

    public SelectBuilder forTable(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public SelectBuilder forClass(Class clazz) {
        this.clazz = clazz;
        Table table = Classes.getClassAnnotation(clazz, Table.class);
        if (table == null) {
            throw new TableNameNotFoundException("@Table annotation not found");
        }
        this.tableName = table.name();
        return this;
    }

    public SelectBuilder field(String fieldName) {
        properties.add(queryMetadata.getColumnName(fieldName));
        return this;
    }

    public SelectBuilder criterion(Criterion criterion) {
        criteria.add(criterion);
        return this;
    }

    public SelectBuilder orderBy(Order... orders) {
        this.orderBy = new OrderByCriterion(orders);
        return this;
    }

    @Override
    protected String getCommand() {
        if (columns == null) {
            columns = properties.isEmpty() ? ALL : join(", ", properties.toArray(new String[properties.size()]));
        }
        return join(" ", SELECT, columns, FROM);
    }

    @Override
    protected String getTable() {
        return tableName;
    }

    @Override
    protected String getWhat() {
        return Strings.hasText(getCriteria()) ? WHERE : "";
    }

    @Override
    protected String getCriteria() {
        return criteria.isEmpty() ? "" : new AndCriterion(criteria.toArray(new Criterion[criteria.size()])).toSql(queryMetadata);
    }

    @Override
    protected String getOrder() {
        return orderBy != null ? orderBy.toSql(queryMetadata) : "";
    }
}
