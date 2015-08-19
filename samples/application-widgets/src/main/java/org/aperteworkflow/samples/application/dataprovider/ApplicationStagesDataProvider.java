package org.aperteworkflow.samples.application.dataprovider;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.casemanagement.util.CaseAttachmentUtil;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;

import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by Dominik Dębowczyk on 2015-08-12.
 */
public class ApplicationStagesDataProvider implements IWidgetDataProvider {
    private static final Logger LOGGER = Logger.getLogger(ApplicationStagesDataProvider.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ICaseManagementFacade caseManagementFacade;

    @Autowired
    private IFilesRepositoryFacade filesRepositoryFacade;

    @Override
    public Map<String, Object> getData(IAttributesProvider provider, Map<String, Object> baseViewData) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<StageDTO> stageDTOs = getStageDTOs(getCase(provider));
        data.put("stageDTOs", stageDTOs);
        return data;
    }

    private Case getCase(IAttributesProvider provider) {
        if(provider instanceof  Case)
            return (Case) provider;
        String attr = provider.getSimpleAttributeValue(CaseAttributes.CASE_ID.value());
        Long caseId = hasText(attr) ? Long.valueOf(attr) : null;
        return caseId != null ? caseManagementFacade.getCaseById(caseId) : null;

    }

    public abstract static class StageDTO {
        public abstract String getName();

        public abstract Date getStartDate();

        public abstract Date getEndDate();

        public abstract String getFiles();

        public abstract CaseStage getStage();

        public abstract Collection<CaseComment> getComments();

    }

    public static class CaseStageWrapper extends StageDTO {

        private final CaseStage stage;
        private Collection<? extends IFilesRepositoryItem> files;
        private Collection<CaseComment> comments;

        public CaseStageWrapper(CaseStage stage) {
            this.stage = stage;
            FilesRepositoryCaseStageAttribute filesAttribute = (FilesRepositoryCaseStageAttribute) this.stage.getAttribute(CaseStageAttributes.STAGE_FILES.value());
            if (filesAttribute != null)
                this.files = filesAttribute.getFilesRepositoryItems();
            CaseStageCommentsAttribute commentsAttribute = (CaseStageCommentsAttribute) this.stage.getAttribute(CaseStageAttributes.COMMENTS.value());
            if (commentsAttribute != null)
                this.comments = commentsAttribute.getComments();
        }


        @Override
        public String getName() {
            return this.stage.getName();
        }

        @Override
        public Date getStartDate() {
            return this.stage.getStartDate();
        }

        @Override
        public Date getEndDate() {
            return this.stage.getEndDate();
        }

        @Override
        public String getFiles() {
            return CaseAttachmentUtil.toJson((Collection<IFilesRepositoryItem>) this.files);
        }

        @Override
        public CaseStage getStage() {
            return this.stage;
        }

        @Override
        public Collection<CaseComment> getComments() {
            return this.comments;
        }


    }

    private List<StageDTO> getStageDTOs(Case caseInstance) {
        List<StageDTO> stages = new ArrayList<StageDTO>();
        for (CaseStage stage : caseInstance.getStages()) {
            CaseStageWrapper stageWrapper = new CaseStageWrapper(stage);
            stages.add(stageWrapper);
        }
        return stages;
    }

}
