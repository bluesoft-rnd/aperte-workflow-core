package pl.net.bluesoft.rnd.processtool.auditlog.model;

import pl.net.bluesoft.rnd.util.CollectionComparer;

import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 27.05.14
 * Time: 16:16
 */
public class AuditLog {
	private String groupKey;
	private boolean singleRow;
	private List<AuditedProperty> pre = new ArrayList<AuditedProperty>();
	private List<AuditedProperty> post = new ArrayList<AuditedProperty>();

	public AuditLog(String groupKey) {
		this.groupKey = groupKey;
	}

	public AuditLog() {
		this("");
	}

	public void addPre(AuditedProperty prop){
		pre.add(prop);
	}

	public void addPost(AuditedProperty prop){
		post.add(prop);
	}

	public boolean isDifferent() {
		return !PROP_COMPARER.compare(pre, post);
	}

	private static final CollectionComparer<AuditedProperty> PROP_COMPARER = new CollectionComparer<AuditedProperty>() {
		@Override
		protected String getKey(AuditedProperty item) {
			return item.getName();
		}

		@Override
		protected boolean compareItems(AuditedProperty item1, AuditedProperty item2) {
			return nvl(item1.getValue()).equals(nvl(item2.getValue()));
		}
	};

	public String getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}

	public boolean isSingleRow() {
		return singleRow;
	}

	public void setSingleRow(boolean singleRow) {
		this.singleRow = singleRow;
	}

	public List<AuditedProperty> getPre() {
		return pre;
	}

	public void setPre(List<AuditedProperty> pre) {
		this.pre = pre;
	}

	public List<AuditedProperty> getPost() {
		return post;
	}

	public void setPost(List<AuditedProperty> post) {
		this.post = post;
	}
}
