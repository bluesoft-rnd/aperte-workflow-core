package pl.net.bluesoft.rnd.processtool.ui.basewidgets.datahandler;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.ProcessVaaadinHistoryWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class HistoryDataHandler implements IWidgetDataHandler
{
    private static Logger logger = Logger.getLogger(HistoryDataHandler.class.getName());

    private IWidgetDataHandler nextDataHandler;

    public HistoryDataHandler()
    {

    }

    public HistoryDataHandler(IWidgetDataHandler nextDataHandler)
    {
        this.nextDataHandler = nextDataHandler;
    }

    @Override
    public Collection<HandlingResult> handleWidgetData(BpmTask task, Map<String, String> data)
    {
        return new LinkedList<HandlingResult>();
    }

//    //TODO refactor & reuse common code with ProcessInstanceAdminManagerPane
//    private ProcessVaaadinHistoryWidget.ProcessLogInfo getProcessLogInfo(ProcessInstanceLog pl) {
//        ProcessVaaadinHistoryWidget.ProcessLogInfo plInfo = new ProcessVaaadinHistoryWidget.ProcessLogInfo();
//        String userDescription = getUserDescription(pl.getUserLogin());
//        if (pl.getUserSubstituteLogin() != null) {
//            String substituteDescription = getUserDescription(pl.getUserSubstituteLogin());
//            plInfo.userDescription = substituteDescription + "(" + getMessage("awf.basewidgets.process-history.substituting") + " " + userDescription  + ")";
//        }
//        else {
//            plInfo.userDescription = userDescription;
//        }
//        plInfo.entryDescription = nvl(pl.getAdditionalInfo(), pl.getLogValue());
//        plInfo.actionDescription = i18NSource.getMessage(pl.getEventI18NKey());
//        if (hasText(plInfo.getEntryDescription())) {
//            plInfo.actionDescription = plInfo.actionDescription + " - " + getMessage(plInfo.entryDescription);
//        }
//        plInfo.performDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pl.getEntryDate().getTime());
//        plInfo.stateDescription = pl.getState() != null ? nvl(pl.getState().getDescription(), pl.getState().getName()) : "";
//        return plInfo;
//    }


}
