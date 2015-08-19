package pl.net.bluesoft.rnd.processtool.web.domain;

import java.util.Collection;

/**
 * Created by lukasz on 5/30/14.
 */
public class Select2PagingBean<T> extends GenericResultBean {

    Collection<T> data;
    long totalRecords;


    public Select2PagingBean(Collection<T> data, long totalRecords) {
        this.data = data;
        this.totalRecords = totalRecords;
    }

    public Collection<T> getData() {
        return data;
    }

    public void setData(Collection<T> data) {
        this.data = data;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
}
