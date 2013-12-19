package pl.net.bluesoft.rnd.processtool.process.externalkey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.DateUtil.getYear;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 *
 *
 * @author polszewski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class ExternalKeyGenerator {
    public static final String PLACEHOLDER_COMPANY = "company";

    public static final String PLACEHOLDER_YEAR = "year";
    public static final String PLACEHOLDER_MONTH = "month";
    public static final String PLACEHOLDER_DAY = "day";

    public static final String PLACEHOLDER_SEQUENCE_NO = "seqNo";

    public enum Mode {
        PREVIEW,
        FINAL,
    }

    private final String processNumberPattern;
    private final String sequenceScope;

    public ExternalKeyGenerator(String processNumberPattern, String sequenceScope) {
        this.processNumberPattern = processNumberPattern;
        this.sequenceScope = sequenceScope;
    }

    public String getNumber(IExternalKeyAbbreviationProvider abbreviationProvider, Mode mode) {
        return PatternUtil.substitute(processNumberPattern, getArguments(abbreviationProvider, mode));
    }

    private Map<String, Object> getArguments(IExternalKeyAbbreviationProvider abbreviationProvider, Mode mode) {
        Map<String, Object> arguments = new HashMap<String, Object>();

        arguments.put(PLACEHOLDER_COMPANY, abbreviationProvider.getAbbreviation());
        arguments.put(PLACEHOLDER_YEAR, getYear(new Date()));

        if (mode == Mode.FINAL) {
            long  sequenceNo = getThreadProcessToolContext()
                        .getNextValue(getSequenceKey(arguments));

            arguments.put(PLACEHOLDER_SEQUENCE_NO, sequenceNo);
        }
        else {
            arguments.put(PLACEHOLDER_SEQUENCE_NO, null);
        }
        return arguments;
    }

    private String getSequenceKey(Map<String, Object> arguments) {
        StringBuilder sb = new StringBuilder("sequence_no");

        if (hasText(sequenceScope)) {
            String[] sequenceParts = sequenceScope.split(",");

            for (String part : sequenceParts) {
                sb.append("/").append(arguments.get(part));
            }
        }
        return sb.toString();
    }
}
