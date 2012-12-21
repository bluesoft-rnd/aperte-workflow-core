package pl.net.bluesoft.rnd.pt.ext.filescapture;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.pt.ext.filescapture.model.FilesCheckerConfiguration;
import pl.net.bluesoft.rnd.pt.ext.filescapture.model.FilesCheckerRuleConfiguration;
import pl.net.bluesoft.rnd.pt.utils.cmis.CmisAtomSessionFacade;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * Created by Agata Taraszkiewicz
 */
public class FilesChecker {

    private static final Logger logger = Logger.getLogger(FilesChecker.class.getName());

    private ProcessToolContext context;

    public FilesChecker(ProcessToolContext context) {
        this.context = context;
    }

    public void run() {
        List<FilesCheckerConfiguration> configs = context.getHibernateSession().createCriteria(FilesCheckerConfiguration.class).list();
        for (FilesCheckerConfiguration cfg : configs) {
            try {
                execute(cfg);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void execute(FilesCheckerConfiguration cfg) throws Exception {
        ProcessToolBpmSession toolBpmSession = context.getProcessToolSessionFactory().createSession(
                new UserData(cfg.getAutomaticUser(), cfg.getAutomaticUser(), cfg.getAutomaticUser()),
                new HashSet());


        File file = new File(cfg.getFilesProperties());

        if (file.isDirectory()) {
            String[] dirList = file.list();
            if (dirList != null) {
                processDirs(dirList, cfg, toolBpmSession);
            }
        }
    }

    private void processDirs(String[] dirList, FilesCheckerConfiguration cfg, ProcessToolBpmSession toolBpmSession) throws IOException {
        for (int i = 0; i < dirList.length; ++i) {
            File dir = new File(cfg.getFilesProperties()+ "/" + dirList[i]);
            if (!dir.isDirectory()) {
                dir.delete();
            } else {
                File finish = new File(dir.getAbsolutePath() + "/.finish");
                if (finish.exists()) {
                    processFiles(dir, cfg, toolBpmSession);
                }
            }
        }
    }

    private void processFiles(File dir, FilesCheckerConfiguration cfg, ProcessToolBpmSession toolBpmSession) throws IOException {

        for (FilesCheckerRuleConfiguration rule : cfg.getRules()) {
            ProcessInstance existingPi = null;
            if (hasText(rule.getProcessIdSubjectLookupRegexp())) {
                Matcher m = java.util.regex.Pattern.compile(rule.getProcessIdSubjectLookupRegexp()).matcher(dir.getName());
                if (m.matches()) {
                    String processId = m.group(1);
                    existingPi = nvl(
                            context.getProcessInstanceDAO().getProcessInstanceByExternalId(processId),
                            context.getProcessInstanceDAO().getProcessInstanceByInternalId(processId));
                    if (existingPi != null) {
                        logger.fine("Found existing process for " + processId + ", ID: " + existingPi.getInternalId());
                    }
                }
            }
            if (existingPi != null && hasText(rule.getRunningProcessActionName())) {
                Collection<BpmTask> taskList = toolBpmSession.findProcessTasks(existingPi, context);
                for (BpmTask t : taskList) {
                    if (!hasText(rule.getProcessTaskName()) || rule.getProcessTaskName().equalsIgnoreCase(t.getTaskName())) {
                        Set<ProcessStateAction> actions = context.getProcessDefinitionDAO().getProcessStateConfiguration(t).getActions();
                        for (ProcessStateAction a : actions) {
                            if (rule.getRunningProcessActionName().equals(a.getBpmName())) {
                                toolBpmSession.performAction(a, t, context);
                                logger.info("Performed action " + rule.getId() + " on matched process id: " + existingPi.getInternalId());
                                break;
                            }
                        }
                    }
                }

            }
            if (existingPi != null && hasText(rule.getRepositoryAtomUrl())) {
                CmisAtomSessionFacade sessionFacade = new CmisAtomSessionFacade(rule.getRepositoryUser(),
                        rule.getRepositoryPassword(),
                        rule.getRepositoryAtomUrl(),
                        rule.getRepositoryId());
                String folderId = null;

                for (ProcessInstanceAttribute at : existingPi.getProcessAttributes()) {
                    if (at instanceof ProcessInstanceSimpleAttribute) {
                        ProcessInstanceSimpleAttribute pisa = (ProcessInstanceSimpleAttribute) at;
                        if (pisa.getKey().equals(rule.getFolderAttributeName())) {
                            folderId = pisa.getValue();
                            break;
                        }
                    }
                }
                org.apache.chemistry.opencmis.client.api.Folder mainFolder;
                if (folderId == null) {
                    mainFolder = sessionFacade.createFolderIfNecessary(nvl(rule.getNewFolderPrefix(), "") +
                            existingPi.getInternalId(), rule.getRootFolderPath());
                    if (StringUtil.hasText(rule.getSubFolder()))
                        mainFolder = sessionFacade.createFolderIfNecessary(rule.getSubFolder(), mainFolder.getPath());
//                    folderId = mainFolder.getId();
                } else {
                    mainFolder = sessionFacade.getFolderById(folderId);
                }
                for (int i = 0; i < dir.list().length; ++i) {
                    String fileName = dir.list()[i];
                    if (!fileName.equals(".finish")) {
                        File file = new File(dir.getAbsolutePath() + "/" + fileName);
                        String mimeType = new MimetypesFileTypeMap().getContentType(file);
                        InputStream is = new FileInputStream(file);
                        try {
							long length = file.length();
							if (length > Integer.MAX_VALUE) {
								throw new IOException("Could not completely read file " + file.getName());
							}
							byte[] bytes = new byte[(int) length];
							int offset = 0;
							int numRead = 0;
							while (offset < bytes.length
									&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
								offset += numRead;
							}
							if (offset < bytes.length) {
								throw new IOException("Could not completely read file " + file.getName());
							}
							sessionFacade.uploadDocument(file.getName(), mainFolder, bytes, mimeType, null);
						}
						finally {
                        	is.close();
							file.delete();
						}
                    }
                }
            }
        }
    }
}
