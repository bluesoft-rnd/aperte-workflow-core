package org.aperteworkflow.webapi.main.processes;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Łukasz Karwot (BlueSoft)
 * Date: 18.04.13
 * Time: 12:39
 */
public class DataPagingBean<T> {

	Collection<T> aaData;

    int	iTotalRecords;

    int	iTotalDisplayRecords;

    String	sEcho;

    public DataPagingBean(Collection<T> dataList, int pageLimit, String echo)
    {
        this.aaData = dataList;
        this.iTotalDisplayRecords = pageLimit;
        this.iTotalRecords = dataList.size();
        this.sEcho = echo;

    }

    public Collection<T> getAaData() {
        return aaData;
    }

    public void setAaData(Collection<T> aaData) {
        this.aaData = aaData;
    }

    public int getiTotalRecords() {
        return iTotalRecords;
    }

    public void setiTotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }

    public String getsEcho() {
        return sEcho;
    }

    public void setsEcho(String sEcho) {
        this.sEcho = sEcho;
    }
}
