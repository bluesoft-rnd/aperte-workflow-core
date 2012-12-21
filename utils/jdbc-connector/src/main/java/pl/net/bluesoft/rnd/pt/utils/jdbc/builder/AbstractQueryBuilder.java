package pl.net.bluesoft.rnd.pt.utils.jdbc.builder;

import pl.net.bluesoft.util.criteria.lang.Keywords;

import static pl.net.bluesoft.util.criteria.lang.Formats.join;

public abstract class AbstractQueryBuilder implements Keywords {
    protected abstract String getCommand();

    protected abstract String getTable();

    protected abstract String getWhat();

    protected abstract String getCriteria();

    protected abstract String getOrder();

    public String build() {
        return join(" ", getCommand(), getTable(), getWhat(), getCriteria(), getOrder());
    }
}
