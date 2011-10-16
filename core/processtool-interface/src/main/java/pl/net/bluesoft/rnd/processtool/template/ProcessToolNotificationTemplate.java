package pl.net.bluesoft.rnd.processtool.template;

public interface ProcessToolNotificationTemplate {
    String getSender();
    String getSubjectTemplate();
    String getBodyTemplate();
    String getTemplateName();
}
