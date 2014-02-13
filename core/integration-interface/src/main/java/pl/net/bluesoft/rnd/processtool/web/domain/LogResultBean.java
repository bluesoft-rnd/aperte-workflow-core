package pl.net.bluesoft.rnd.processtool.web.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class LogResultBean
{
    private String messageKey;
    private String headerKey;
    private List<String> parameters = new ArrayList<String>();

    public LogResultBean(String headerKey, String messageKey, String ... params)
    {
        this.messageKey = messageKey;
        this.headerKey = headerKey;

        for(String paramter: params)
            parameters.add(paramter);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void addParamter(int index, String paramter) {
        this.parameters.add(index, paramter);
    }


    public String getHeaderKey() {
        return headerKey;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogResultBean)) return false;

        LogResultBean that = (LogResultBean) o;

        if (headerKey != null ? !headerKey.equals(that.headerKey) : that.headerKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return headerKey != null ? headerKey.hashCode() : 0;
    }
}
