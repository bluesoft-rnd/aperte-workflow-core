/**
 *
 */
package pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Represents a persistent report generation order. The data should be kept in <code>ar_report_order</code> table in <code>public</code> schema.
 */
@Entity
@Table(name = "ar_report_order")
public class ReportOrder {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "id", nullable = false, length = 10)
    private Long id;

    /**
     * Login of user who ordered a report.
     */
    @Column
    private String username;

    /**
     * Date of order.
     */
    @Column(name = "create_date", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar createDate = Calendar.getInstance();

    /**
     * Date processing started.
     */
    @Column(name = "start_date")
    private Calendar startDate;

    /**
     * Date processing finished.
     */
    @Column(name = "finish_date")
    private Calendar finishDate;

    /**
     * Input parameters formatted as XML.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "parameters_xml")
    @Basic(fetch = FetchType.LAZY)
    private String parametersXml;

    /**
     * Report result formatted as BASE64.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "report_result")
    @Basic(fetch = FetchType.LAZY)
    private String reportResult;

    /**
     * Output format, e.g. CSV, HTML, PDF, XLS.
     */
    @Column(name = "output_format")
    private String outputFormat;

    /**
     * Details of an error should processing failed.
     */
    @Column(name = "error_details")
    private String errorDetails;

    /**
     * Email address of an user to receive report results or to be
     * notified of execution failure.
     */
    @Column(name = "recipient_email")
    private String recipientEmail;

    /**
     * JMS queue the order should reply to.
     */
    @Column(name = "reply_to_q")
    private String replyToQ;

    /**
     * Report template used.
     */
    @ManyToOne
    @JoinColumn(name = "report_id")
    private ReportTemplate report;

    /**
     * Report status as defined in enum org.apertereports.model.ReportOrder.Status
     * <dl>
     * <dt>0</dt>
     * <dd>new</dd>
     * <dt>1</dt>
     * <dd>processing</dd>
     * <dt>2</dt>
     * <dd>succeeded</dd>
     * <dt>3</dt>
     * <dd>failed</dd>
     * </dl>
     */
    @Enumerated
    @Column(name = "report_status")
    private Status reportStatus = Status.NEW;

    public Calendar getCreateDate() {
        return createDate;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public Calendar getFinishDate() {
        return finishDate;
    }

    public Long getId() {
        return id;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getParametersXml() {
        return parametersXml;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getReplyToQ() {
        return replyToQ;
    }

    public ReportTemplate getReport() {
        return report;
    }

    public String getReportResult() {
        return reportResult;
    }

    public Status getReportStatus() {
        return reportStatus;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public String getUsername() {
        return username;
    }

    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public void setFinishDate(Calendar finishDate) {
        this.finishDate = finishDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void setParametersXml(String parametersXml) {
        this.parametersXml = parametersXml;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setReplyToQ(String replyToQ) {
        this.replyToQ = replyToQ;
    }

    public void setReport(ReportTemplate report) {
        this.report = report;
    }

    public void setReportResult(String reportResult) {
        this.reportResult = reportResult;
    }

    public void setReportStatus(Status reportStatus) {
        this.reportStatus = reportStatus;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public enum Status {
        NEW, PROCESSING, SUCCEEDED, FAILED
    }

    public ReportOrder shallowCopy() {
        ReportOrder copy = new ReportOrder();
        copy.setOutputFormat(getOutputFormat());
        copy.setParametersXml(getParametersXml());
        copy.setRecipientEmail(getRecipientEmail());
        copy.setReplyToQ(getReplyToQ());
        copy.setReport(getReport());
        copy.setUsername(getUsername());
        return copy;
    }
}
