package pl.net.bluesoft.casemanagement.util;

import org.codehaus.jackson.map.ObjectMapper;
import pl.net.bluesoft.casemanagement.model.CaseStateProcess;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Dominik Dębowczyk on 2015-08-17.
 */
public class CaseProcessUtil {
    private static final Logger logger = Logger.getLogger(CaseProcessUtil.class.getName());

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Set<CaseStateProcess> processes,  I18NSource messageSource) {
        List<CaseStateProcess> sortedPorcesses = getSortedProcessesByPriority(processes);
        List<CaseStateProcessDTO> dtos = getCaseStateProcessDTOs(sortedPorcesses, messageSource);
        try {
            return MAPPER.writeValueAsString(dtos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<CaseStateProcess> getSortedProcessesByPriority(Set<CaseStateProcess> processes) {
        List<CaseStateProcess> processesList = new ArrayList<CaseStateProcess>(processes);
        Collections.sort(processesList, new Comparator<CaseStateProcess>() {
            @Override
            public int compare(CaseStateProcess o1, CaseStateProcess o2) {
                int o1Priority = Integer.getInteger(o1.getProcessPriority());
                int o2Priority = Integer.getInteger(o2.getProcessPriority());
                return Integer.compare(o1Priority, o2Priority);
            }
        });
        return processesList;
    }

    private static class CaseStateProcessDTO {
        private String bpmDefinitionKey;
        private String processLabel;
        private String processActionType;
        private String processPriority;
        private String processIcon;

        public CaseStateProcessDTO(CaseStateProcess process,  I18NSource messageSource) {
            setBpmDefinitionKey(process.getBpmDefinitionKey());
            setProcessLabel(messageSource.getMessage(process.getProcessLabel()));
            setProcessActionType(process.getProcessActionType());
            setProcessPriority(process.getProcessPriority());
            setProcessIcon(process.getProcessIcon());
        }

        public String getBpmDefinitionKey() {
            return bpmDefinitionKey;
        }

        public void setBpmDefinitionKey(String bpmDefinitionKey) {
            this.bpmDefinitionKey = bpmDefinitionKey;
        }

        public String getProcessLabel() {
            return processLabel;
        }

        public void setProcessLabel(String processLabel) {
            this.processLabel = processLabel;
        }

        public String getProcessActionType() {
            return processActionType;
        }

        public void setProcessActionType(String processActionType) {
            this.processActionType = processActionType;
        }

        public String getProcessPriority() {
            return processPriority;
        }

        public void setProcessPriority(String processPriority) {
            this.processPriority = processPriority;
        }

        public String getProcessIcon() {
            return processIcon;
        }

        public void setProcessIcon(String processIcon) {
            this.processIcon = processIcon;
        }
    }

    private static List<CaseStateProcessDTO> getCaseStateProcessDTOs(List<CaseStateProcess> processes, I18NSource messageSource) {
        List<CaseStateProcessDTO> result = new ArrayList<CaseStateProcessDTO>();
        for (CaseStateProcess process : processes) {
            CaseStateProcessDTO dto = new CaseStateProcessDTO(process, messageSource);
            result.add(dto);
        }
        return result;
    }
}
