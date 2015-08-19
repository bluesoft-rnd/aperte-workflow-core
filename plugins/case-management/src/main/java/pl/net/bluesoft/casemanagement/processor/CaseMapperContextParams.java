package pl.net.bluesoft.casemanagement.processor;

import org.aperteworkflow.files.model.IFilesRepositoryItem;
import pl.net.bluesoft.casemanagement.model.CaseStage;
import pl.net.bluesoft.rnd.processtool.plugins.MapperContext;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2014-11-03
 */
public class CaseMapperContextParams {
	private static final String STAGE = "stage";
	private static final String ATTACHMENTS = "attachments";

	public static CaseStage getStage(MapperContext mapperContext) {
		return (CaseStage)mapperContext.getParam(STAGE);
	}

	public static void setStage(MapperContext mapperContext, CaseStage stage) {
		mapperContext.setParam(STAGE, stage);
	}

	public static List<IFilesRepositoryItem> getAttachments(MapperContext mapperContext) {
		return (List<IFilesRepositoryItem>)mapperContext.getParam(ATTACHMENTS);
	}

	public static void setAttachments(MapperContext mapperContext, List<IFilesRepositoryItem> attachments) {
		mapperContext.setParam(ATTACHMENTS, attachments);
	}
}
