package pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * A persistent representation of a cyclic report generation order. The entries are held in the <code>public</code> schema
 * in a table named <code>ar_cyclic_report_order</code>.
 */
@Entity
@Table(name = "ar_cyclic_report_order")
public class CyclicReportOrder {

    public CyclicReportOrder() {
    }

    public CyclicReportOrder(CyclicReportOrder order) {
        this.cronSpec = order.getCronSpec();
        this.description = order.getDescription();
        this.enabled = order.getEnabled();
        this.id = order.getId();
        this.outputFormat = order.getOutputFormat();
        this.parametersXml = order.getParametersXml();
        this.processedOrder = order.getProcessedOrder();
        this.recipientEmail = order.getRecipientEmail();
        this.report = order.getReport();
        this.reportOrder = order.getReportOrder();
        this.componentId = order.getComponentId();
    }

    /**
     * An identifier used by GUI to mark the object.
     */
    @Transient
    private Integer componentId;

    public Integer getComponentId() {
        return componentId;
    }

    public void setComponentId(Integer componentId) {
        this.componentId = componentId;
    }

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, length = 10)
    private Long id;

    /**
     * Cron expression. Specifies the frequency of generation of the report.
     */
    @Column(name = "cron_spec")
    private String cronSpec;

    /**
     * Indicates whether a cyclic report is enabled or disabled.
     */
    @Column
    private Boolean enabled;

    /**
     * Report input parameters formatted as XML.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "parameters_xml")
    private String parametersXml;

    /**
     * Output format.
     */
    @Column(name = "output_format")
    private String outputFormat;

    /**
     * Displayed description.
     */
    @Column(name = "description")
    private String description;

    /**
     * Template of a report that has been ordered.
     */
    @ManyToOne
    private ReportTemplate report;
    /**
     * Last successful report.
     */
    @ManyToOne
    @JoinColumn(name = "report_order_id")
    private ReportOrder reportOrder;

    /**
     * Report due to be executed.
     * Deleted after execution (successful or not).
     */
    @ManyToOne
    @JoinColumn(name = "processed_order_id")
    private ReportOrder processedOrder;

    /**
     * Email address of an user to receive report results or to be
     * notified of execution failure.
     */
    @Column(name = "recipient_email")
    private String recipientEmail;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronSpec() {
        return cronSpec;
    }

    public Boolean getEnabled() {
        return enabled;
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

    public ReportOrder getProcessedOrder() {
        return processedOrder;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public ReportTemplate getReport() {
        return report;
    }

    public ReportOrder getReportOrder() {
        return reportOrder;
    }

    public void setCronSpec(String cronSpec) {
        this.cronSpec = cronSpec;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public void setProcessedOrder(ReportOrder processedOrder) {
        this.processedOrder = processedOrder;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setReport(ReportTemplate report) {
        this.report = report;
    }

    public void setReportOrder(ReportOrder reportOrder) {
        this.reportOrder = reportOrder;
    }
}
