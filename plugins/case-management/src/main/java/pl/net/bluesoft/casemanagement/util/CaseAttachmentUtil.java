package pl.net.bluesoft.casemanagement.util;

import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2014-09-23
 */
public class CaseAttachmentUtil {
	private static final Logger logger = Logger.getLogger(CaseAttachmentUtil.class.getName());

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static Collection<IFilesRepositoryItem> fromJson(String stageFilesJson) {
		Collection<IFilesRepositoryItem> stageFiles = null;

		if (stageFilesJson != null) {
			try {
				stageFiles = MAPPER.readValue(stageFilesJson, new TypeReference<List<FilesRepositoryItem>>() {});// todo unmarshal data
			}
			catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return stageFiles;
	}

	public static String toJson(Collection<IFilesRepositoryItem> stageFiles) {
		try {
			return MAPPER.writeValueAsString(stageFiles);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
