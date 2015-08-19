package pl.net.bluesoft.casemanagement.controller.bean;

/**
 * Created by Michal Radko on 2014-08-06.
 * mradko@bluesoft.net.pl
 */
public class CasesCountBean {
    private long allCases;
    private long notClosedCases;

    public CasesCountBean() {
        this.allCases = 0;
        this.notClosedCases = 0;
    }

    public long getAllCases() {
        return allCases;
    }

    public void setAllCases(long allCases) {
        this.allCases = allCases;
    }

    public long getNotClosedCases() {
        return notClosedCases;
    }

    public void setNotClosedCases(long notClosedCases) {
        this.notClosedCases = notClosedCases;
    }
}
