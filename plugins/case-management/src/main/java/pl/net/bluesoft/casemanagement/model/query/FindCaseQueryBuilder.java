package pl.net.bluesoft.casemanagement.model.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.casemanagement.model.query.FindCaseQueryParams.*;

/**
 * Created by Dominik Dêbowczyk on 2015-08-18.
 */
public class FindCaseQueryBuilder {
    private static final String VIEW_ROLE_NAME = "SHOW_COMPLAINT_";
    private static final String EDIT_ROLE_NAME = "EDIT_COMPLAINT_";

    public static String SelectCase = "selectCase";
    public static String SelectCaseCount = "selectCaseCount";

    private Map<String, Object> params = new HashMap<String, Object>();

    public Map<String, Object> build() {
        return params;
    }

    public FindCaseQueryBuilder withCaseNumber(String value) {
        params.put(CaseNumber, value);
        return this;
    }

    public FindCaseQueryBuilder withCaseShortNumber(String value) {
        params.put(CaseShortNumber, value);
        return this;
    }

    public FindCaseQueryBuilder withCreateDate(String value, SimpleDateFormat format) {
        Date date = null;
        try {
            if (value != null) {
                date = format.parse(value);
                params.put(CreateDate, date);
            }
        } catch (ParseException e) {
            //ignore
        }
        return this;
    }

    public FindCaseQueryBuilder withStages(String value) {
        params.put(Stages, value);
        return this;
    }

    public FindCaseQueryBuilder withCreateDateTo(String value, SimpleDateFormat format) {
        Date date = null;
        try {
            if (value != null) {
                date = format.parse(value);
                params.put(CreateDateTo, date);
            }
        } catch (ParseException e) {
            //ignore
        }
        return this;
    }

    public FindCaseQueryBuilder withTextSearch(String value) {
        params.put(TextSearch, value);
        return this;
    }

    private Boolean getBooleanValue(String value) {
        Boolean b;
        try {
            b = Boolean.parseBoolean(value);
        } catch (Exception e) {
            b = Boolean.FALSE;
        }
        return b;
    }

    public FindCaseQueryBuilder withCreateDateRange(String value) {
        params.put(CreateDateRange, getBooleanValue(value));
        return this;
    }
}
