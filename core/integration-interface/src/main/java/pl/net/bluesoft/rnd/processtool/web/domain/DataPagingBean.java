package pl.net.bluesoft.rnd.processtool.web.domain;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Łukasz Karwot (BlueSoft)
 * Date: 18.04.13
 * Time: 12:39
 */
public class DataPagingBean<T> extends GenericResultBean {

	Collection<T> listData;

    int	recordsTotal;

    int	recordsFiltered;

    int	draw;

    public DataPagingBean(Collection<T> dataList, int pageLimit, int draw)
    {
        this.listData = dataList;
        this.recordsFiltered = pageLimit;
        this.recordsTotal = dataList.size();
        this.draw = draw;

    }

    public Collection<T> getListData() {
        return listData;
    }

    public void setListData(Collection<T> listData) {
        this.listData = listData;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }
}
