package pl.net.bluesoft.rnd.processtool.hibernate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwysocki_bls
 * Date: 9/2/11
 * Time: 10:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResultsPageWrapper<T> {
	private List<T> results;
	private Integer total;

    public ResultsPageWrapper(List<T> results) {
        this.results = results;
        this.total = results.size();
    }

	public ResultsPageWrapper(List<T> results, Integer total) {
		this.results = results;
		this.total = total;
	}

	public ResultsPageWrapper() {
		this.results = new ArrayList<T>(0);
		this.total = 0;
	}

	public List<T> getResults() {
		return results;
	}

	public Integer getTotal() {
		return total;
	}
}
