package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Util for steps
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class StepUtil
{
    public static String extractVariable(String variableCode, ProcessToolContext ctx, ProcessInstance pi )
    {
        if (variableCode == null)
            return null;

        variableCode = variableCode.trim();

        /* If key is variable #{variableName}, extract it */
        if(variableCode.matches("#\\{.*\\}"))
        {
            String variableName = variableCode.replaceAll("#\\{(.*)\\}", "$1");
            variableName = pi.getSimpleAttributeValue(variableName);

            return variableName;
        }
        /* Otherwise variableCode == key */
        else
        {
            return pi.getSimpleAttributeValue(variableCode);
        }
    }
}
