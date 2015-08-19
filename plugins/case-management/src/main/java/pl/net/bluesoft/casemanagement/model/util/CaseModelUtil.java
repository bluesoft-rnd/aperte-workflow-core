package pl.net.bluesoft.casemanagement.model.util;

import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.model.FilesRepositoryAttributes;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;
import pl.net.bluesoft.casemanagement.dao.FilesRepositoryCaseAttributeFactoryImpl;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2014-10-24
 */
public class CaseModelUtil {
	public static Long getCaseId(IAttributesProvider provider) {
		if (provider instanceof Case) {
			return provider.getId();
		}
		String attr = provider.getSimpleAttributeValue(CaseAttributes.CASE_ID.value());
		return hasText(attr) ? Long.valueOf(attr) : null;
	}

	public static void setCaseId(ProcessInstance processInstance, Long caseId) {
		processInstance.setSimpleAttribute(CaseAttributes.CASE_ID.value(), caseId != null ? String.valueOf(caseId) : null);
	}

	public static IFilesRepositoryAttribute getFiles(IAttributesProvider provider) {
		return (IFilesRepositoryAttribute) provider.getAttribute(FilesRepositoryAttributes.FILES.value());
	}

	public static IFilesRepositoryAttribute getFiles(IAttributesConsumer consumer, FilesRepositoryAttributeFactory factory) {
		IFilesRepositoryAttribute attribute = getFiles(consumer);
		if (attribute == null) {
			attribute = factory.create();
			attribute.setKey(FilesRepositoryAttributes.FILES.value());
			consumer.setAttribute(FilesRepositoryAttributes.FILES.value(), attribute);
		}
		return attribute;
	}

	public static IFilesRepositoryAttribute getFiles(Case consumer) {
		return getFiles(consumer, FilesRepositoryCaseAttributeFactoryImpl.INSTANCE);
	}

	public static CaseCommentsAttribute getCaseComments(IAttributesProvider provider) {
		return (CaseCommentsAttribute) provider.getAttribute(CaseAttributes.COMMENTS.value());
	}

	public static CaseStageCommentsAttribute getCaseStageComments(IAttributesProvider provider) {
		return (CaseStageCommentsAttribute) provider.getAttribute(CaseStageAttributes.COMMENTS.value());
	}
}
