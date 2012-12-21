package org.aperteworkflow.plugin.ext.log;

import com.vaadin.ui.TextArea;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredPropertyConfigurator;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AliasName(name = "LogStep")
public class LogStep implements ProcessToolProcessStep {

    /**
     * Default logger
     */
    private static final Logger DEFAULT_LOGGER = Logger.getLogger(LogStep.class.getName());

    /**
     * Pattern used to extract the process attribute names from the log message
     */
    private static final Pattern PROCESS_ATTRIBUTE_PATTERN = Pattern.compile("\\$\\{([a-zA-z0-9._-]+)\\}");

    @AperteDoc(
            humanNameKey = "log-step.attribute.message",
            descriptionKey = "log-step.attribute.message.description"
    )
    @AutoWiredProperty(required = true)
    @AutoWiredPropertyConfigurator(fieldClass = TextArea.class)
    private String message;

    @AperteDoc(
            humanNameKey = "log-step.attribute.loggerName",
            descriptionKey = "log-step.attribute.loggerName.description"
    )
    @AutoWiredProperty
    private String loggerName;

    @AperteDoc(
            humanNameKey = "log-step.attribute.loggerLevel",
            descriptionKey = "log-step.attribute.loggerLevel.description"
    )
    @AutoWiredProperty
    private String loggerLevel;

    @Override
    public String invoke(BpmStep step, Map params) throws Exception {
        Logger usedLogger = DEFAULT_LOGGER;
        if (loggerName != null) {
            usedLogger = Logger.getLogger(loggerName);
        }

        Level usedLevel = Level.INFO;
        if (loggerLevel != null) {
            try {
                Level.parse(loggerLevel);
            } catch (IllegalArgumentException e) {
                DEFAULT_LOGGER.log(Level.SEVERE, "Logger level " + loggerLevel + " is invalid, using INFO", e);
            }
        }

        String parsedMessage = parseLogMessage(message, step.getProcessInstance());
        usedLogger.log(usedLevel, parsedMessage);
        return parsedMessage;
    }

    /**
     * Parse log message evaluating possible references to process attributes
     *
     * @param message Content of the message
     * @param processInstance Content of the process
     * @return Parsed message
     */
    private String parseLogMessage(String message, ProcessInstance processInstance) {
        StringBuilder builder = new StringBuilder();

        Matcher matcher = PROCESS_ATTRIBUTE_PATTERN.matcher(message);
        int pos = 0;
        while (matcher.find()) {
            String processAttributeKey = matcher.group(1);

            ProcessInstanceAttribute attribute = processInstance.findAttributeByKey(processAttributeKey);
            if (attribute != null) {
                int start = matcher.start(0);
                if (pos < start) {
                    builder.append(message.substring(pos, start));
                }

                builder.append(attribute.toString());
                pos = matcher.end(0);
            }
        }

        if (pos < message.length()) {
            builder.append(message.substring(pos));
        }

        return builder.toString();
    }

    @SuppressWarnings("unused")
    public String getMessage() {
        return message;
    }

    @SuppressWarnings("unused")
    public void setMessage(String message) {
        this.message = message;
    }

    @SuppressWarnings("unused")
    public String getLoggerName() {
        return loggerName;
    }

    @SuppressWarnings("unused")
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @SuppressWarnings("unused")
    public String getLoggerLevel() {
        return loggerLevel;
    }

    @SuppressWarnings("unused")
    public void setLoggerLevel(String loggerLevel) {
        this.loggerLevel = loggerLevel;
    }

}
