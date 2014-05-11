package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Util for steps
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class StepUtil {
    public static String extractVariable(String variableCode, ProcessToolContext ctx, ProcessInstance pi) {
        if (variableCode == null)
            return null;

        variableCode = variableCode.trim();

        /* If key is variable #{variableName}, extract it */
        if (variableCode.matches("#\\{.*\\}")) {
            String variableName = variableCode.replaceAll("#\\{(.*)\\}", "$1");
            variableName = pi.getSimpleAttributeValue(variableName);

            return variableName;
        }
        /* Otherwise variableCode == key */
        else {
            return pi.getSimpleAttributeValue(variableCode);
        }
    }

    /**
     * Evaluate the given query string to a list of simple attributes.
     *
     * @param query
     * @return
     */
    public static Map<String, String> evaluateQuery(String query)
    {
        if(query == null)
            return null;

        Map<String, String> attributes = new HashMap<String, String>();
        String[] parts = query.split("[,;]");
        for (String part : parts) {
            String[] assignment = part.split("[:=]");
            if (assignment.length != 2)
                continue;

            if (assignment[1].startsWith("\"") && assignment[1].endsWith("\""))
                assignment[1] = assignment[1].substring(1, assignment[1].length() - 1);

            String key = assignment[0];
            String value = assignment[1];
            attributes.put(key, value);
        }
        return attributes;
    }

    public static Collection<String> evaluateList(String query)
    {
        if(query == null)
            return null;

        Collection<String> attributes = new LinkedList<String>();
        String[] parts = query.split("[,;]");
        for (String part : parts) {
            attributes.add(part);
        }
        return attributes;
    }
}
