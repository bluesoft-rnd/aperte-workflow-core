package pl.net.bluesoft.rnd.processtool.model.report.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:38
 */
@XStreamAlias("report")
public class ReportDefinition {
	@XStreamAsAttribute
	private String query;
	@XStreamImplicit
	private List<ReportParam> params = new ArrayList<ReportParam>();
	@XStreamImplicit
	private List<ReportResultColumn> columns = new ArrayList<ReportResultColumn>();

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<ReportParam> getParams() {
		return params;
	}

	public void setParams(List<ReportParam> params) {
		this.params = params;
	}

	public ReportDefinition addParam(ReportParam param) {
		params.add(param);
		return this;
	}

	public List<ReportResultColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<ReportResultColumn> columns) {
		this.columns = columns;
	}

	public ReportDefinition addColumn(ReportResultColumn column) {
		columns.add(column);
		return this;
	}
}
