package org.aperteworkflow.util.taskitem;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderParams;
import pl.net.bluesoft.util.lang.cquery.func.F;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;
import static org.aperteworkflow.util.vaadin.VaadinUtility.labelWithIcon;
import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * User: POlszewski
 * Date: 2011-12-14
 * Time: 09:07:24
 */
public class ProcessInfoBuilder {
    public static final Alignment DEFAULT_ALIGNMENT = Alignment.MIDDLE_CENTER;

    private final TaskItemProviderParams params;

    private static class ProcessInfoItem {
        public Component component;
        public Alignment alignment;
        public int width, height; // mierzone w kolumnach/wierszach

        public ProcessInfoItem(Component component, Alignment alignment, int width, int height) {
            this.component = component;
            this.alignment = alignment;
            this.width = width;
            this.height = height;
        }
    }

    private final List<List<ProcessInfoItem>> rows = new ArrayList<List<ProcessInfoItem>>();

    public ProcessInfoBuilder(TaskItemProviderParams params) {
        this.params = params;
    }

    public TaskItemProviderParams getParams() {
        return params;
    }

    public ProcessToolContext getCtx() {
        return params.getCtx();
    }

    public ProcessToolBpmSession getBpmSession() {
        return params.getBpmSession();
    }

    public I18NSource getI18NSource() {
        return params.getI18NSource();
    }

    public String getMessage(String key) {
        return getI18NSource().getMessage(key);
    }

    public ProcessInstance getProcessInstance() {
        return params.getProcessInstance();
    }

    public BpmTask getTask() {
        return params.getTask();
    }

    // dodawanie elementow

    public Component addLabelWithIcon(String image, String caption, String style, String description) {
        return addLabelWithIcon(image, caption, style, description, DEFAULT_ALIGNMENT);
    }

    public Component addLabelWithIcon(String image, String caption, String style, String description, Alignment alignment) {
        return addComponent(labelWithIcon(params.getImage(image), caption, style, description), alignment);
    }

    public Label addLabel(String text, Alignment alignment, int width, int height) {
        return addComponent(new Label(text), alignment, width, height);
    }

    public Label addLabel(String text, Alignment alignment) {
        return addComponent(new Label(text), alignment);
    }

    public Label addLabel(String text, int width, int height) {
        return addComponent(new Label(text), width, height);
    }

    public Label addLabel(String text) {
        return addComponent(new Label(text));
    }


    public <T extends Component> T addComponent(T component, Alignment alignment, int width, int height) {
        getCurrentRow().add(new ProcessInfoItem(component, alignment, width, height));
        return component;
    }

    public <T extends Component> T addComponent(T component, Alignment alignment) {
        return addComponent(component, alignment, 1, 1);
    }

    public <T extends Component> T addComponent(T component, int width, int height) {
        return addComponent(component, DEFAULT_ALIGNMENT, width, height);
    }

    public <T extends Component> T addComponent(T component) {
        return addComponent(component, DEFAULT_ALIGNMENT);
    }

    /**
     * generuje przejscie do nast kolumny
     */

    public void addSeparator() {
        rows.add(null);
    }

    /**
     *  generuje przejscie do nast wiersza
     */

    public void addNewRow() {
        rows.add(new ArrayList<ProcessInfoItem>());
    }

    /**
     * tworzy layout na podstawie zgromadzonego opisu
     */

    public GridLayout buildLayout() {
        // w = suma szerokosci wszystkich komorek w wierszu
        // h = ilosc wierszy
        int w = nvl(from(rows).max(new F<List<ProcessInfoItem>, Integer>() {
                    @Override
                    public Integer invoke(List<ProcessInfoItem> cells) {
                        return nvl(from(cells).sum(new F<ProcessInfoItem, Integer>() {
                            @Override
                            public Integer invoke(ProcessInfoItem cell) {
                                return cell.width;
                            }
                        }), 0);
                    }
                }), 0);
        int h = rows.size();

        if (w == 0 || h == 0) {
            return null;
        }

        GridLayout layout = new GridLayout(w, h);
        layout.setSpacing(true);

        int rowNo = 0;
        for (List<ProcessInfoItem> row : rows) {
            int colNo = 0;
            for (ProcessInfoItem item : row) {
                if (item != null) {
                    layout.addComponent(item.component, colNo, rowNo, colNo + item.width - 1, rowNo + item.height - 1);
                    layout.setComponentAlignment(item.component, item.alignment);
                    colNo += item.width;
                }
                else {
                    ++colNo;
                }
            }
            ++rowNo;
        }
        return layout;
    }

    private List<ProcessInfoItem> getCurrentRow() {
        if (rows.isEmpty()) {
            addNewRow();
        }
        return rows.get(rows.size() - 1);
    }
}
