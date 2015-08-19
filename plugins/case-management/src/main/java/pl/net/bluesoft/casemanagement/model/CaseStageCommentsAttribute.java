package pl.net.bluesoft.casemanagement.model;

import pl.net.bluesoft.util.lang.cquery.func.F;

import javax.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Created by Dominik Dębowczyk on 2015-08-13.
 */
@Entity
@Table(name="pt_case_stage_comments_attr", schema = CASES_SCHEMA)
public class CaseStageCommentsAttribute extends CaseStageAttribute{
    public static final String TABLE = CASES_SCHEMA + "." + CaseStageCommentsAttribute.class.getAnnotation(Table.class).name();
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "case_stage_comment_id")
    private Set<CaseComment> comments = new HashSet<CaseComment>();

    public Set<CaseComment> getComments() {
        return comments;
    }

    public void setComments(Set<CaseComment> comments) {
        this.comments = comments;
    }

    public List<CaseComment> getCommentsOrderedByDate(boolean ascending) {
        if (ascending) {
            return from(getComments()).orderBy(BY_CREATE_DATE).toList();
        } else {
            return from(getComments()).orderByDescending(BY_CREATE_DATE).toList();
        }
    }

    private static final F<CaseComment, Long> BY_CREATE_DATE = new F<CaseComment, Long>() {
        @Override
        public Long invoke(CaseComment x) {
            return x.getCreateDate().getTime();
        }
    };
}
