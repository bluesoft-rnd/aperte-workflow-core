package org.aperteworkflow.util.vaadin.ui.table;

import com.vaadin.data.Container;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.List;

public class LocalizedPagedTable extends Table {
    public interface PageChangeListener {
        public void pageChanged(PagedTableChangeEvent event);
    }

    public class PagedTableChangeEvent {
        final LocalizedPagedTable table;
        public PagedTableChangeEvent(LocalizedPagedTable table) {
            this.table = table;
        }
        public LocalizedPagedTable getTable() {
            return table;
        }
        public int getCurrentPage() {
            return table.getCurrentPage();
        }
        public int getTotalAmountOfPages() {
            return table.getTotalAmountOfPages();
        }
    }

    private List<PageChangeListener> listeners = null;

    private PagedTableContainer container;

    public LocalizedPagedTable() {
        this(null);
    }

    public LocalizedPagedTable(String caption) {
        super(caption);
        setPageLength(10);
        addStyleName("pagedtable");
    }

    public HorizontalLayout createPageSizeControls(I18NSource messageSource) {
        return createPageSizeControls(messageSource.getMessage("pagedtable.itemsperpage"));
    }
    
    public HorizontalLayout createPageSizeControls(String itemsPerPageCaption) {
        Label itemsPerPageLabel = new Label(itemsPerPageCaption + ":");
        itemsPerPageLabel.addStyleName("pagedtable-label pagedtable-itemsperpagecaption");

        final ComboBox itemsPerPageSelect = new ComboBox();
        itemsPerPageSelect.addStyleName("pagedtable-combobox pagedtable-itemsperpagecombobox");
        itemsPerPageSelect.addItem("5");
        itemsPerPageSelect.addItem("10");
        itemsPerPageSelect.addItem("25");
        itemsPerPageSelect.addItem("50");
        itemsPerPageSelect.addItem("100");
        itemsPerPageSelect.setImmediate(true);
        itemsPerPageSelect.setNullSelectionAllowed(false);
        itemsPerPageSelect.setWidth("50px");
        itemsPerPageSelect.addListener(new ValueChangeListener() {
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                setPageLength(Integer.valueOf(String.valueOf(event.getProperty().getValue())));
            }
        });
        itemsPerPageSelect.select(String.valueOf(getPageLength()));

        HorizontalLayout pageSize = new HorizontalLayout();
        pageSize.addComponent(itemsPerPageLabel);
        pageSize.addComponent(itemsPerPageSelect);
        pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
        pageSize.setComponentAlignment(itemsPerPageSelect, Alignment.MIDDLE_LEFT);
        pageSize.setSpacing(true);

        addListener(new PageChangeListener() {
            public void pageChanged(PagedTableChangeEvent event) {
                itemsPerPageSelect.setValue(String.valueOf(getPageLength()));
            }
        });

        return pageSize;
    }

    public HorizontalLayout createPageManagementControls(I18NSource messageSource) {
        return createPageManagementControls(messageSource.getMessage("pagedtable.page"));
    }

    public HorizontalLayout createPageManagementControls(String pageCaption) {
        Label pageLabel = new Label(pageCaption + ":&nbsp;", Label.CONTENT_XHTML);
        final TextField currentPageTextField = new TextField();
        currentPageTextField.setValue(String.valueOf(getCurrentPage()));
        currentPageTextField.addValidator(new IntegerValidator(null));
        Label separatorLabel = new Label("&nbsp;/&nbsp;", Label.CONTENT_XHTML);
        final Label totalPagesLabel = new Label(String.valueOf(getTotalAmountOfPages()), Label.CONTENT_XHTML);
        currentPageTextField.setStyleName(Reindeer.TEXTFIELD_SMALL);
        currentPageTextField.setImmediate(true);
        currentPageTextField.addListener(new ValueChangeListener() {
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                if (currentPageTextField.isValid() && currentPageTextField.getValue() != null) {
                    int page = Integer.valueOf(String.valueOf(currentPageTextField.getValue()));
                    setCurrentPage(page);
                }
            }
        });
        pageLabel.setWidth(null);
        currentPageTextField.setWidth("20px");
        separatorLabel.setWidth(null);
        totalPagesLabel.setWidth(null);

        HorizontalLayout pageManagement = new HorizontalLayout();
        final Button first = new Button("<<", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                setCurrentPage(0);
            }
        });
        final Button previous = new Button("<", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                previousPage();
            }
        });
        final Button next = new Button(">", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                nextPage();
            }
        });
        final Button last = new Button(">>", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                setCurrentPage(getTotalAmountOfPages());
            }
        });
        first.setStyleName(Reindeer.BUTTON_LINK);
        previous.setStyleName(Reindeer.BUTTON_LINK);
        next.setStyleName(Reindeer.BUTTON_LINK);
        last.setStyleName(Reindeer.BUTTON_LINK);

        pageLabel.addStyleName("pagedtable-label pagedtable-pagecaption");
        currentPageTextField.addStyleName("pagedtable-label pagedtable-pagefield");
        separatorLabel.addStyleName("pagedtable-label pagedtable-separator");
        totalPagesLabel.addStyleName("pagedtable-label pagedtable-total");
        first.addStyleName("pagedtable-button pagedtable-first");
        previous.addStyleName("pagedtable-button pagedtable-previous");
        next.addStyleName("pagedtable-button pagedtable-next");
        last.addStyleName("pagedtable-button pagedtable-last");

        pageManagement.addComponent(first);
        pageManagement.addComponent(previous);
        pageManagement.addComponent(pageLabel);
        pageManagement.addComponent(currentPageTextField);
        pageManagement.addComponent(separatorLabel);
        pageManagement.addComponent(totalPagesLabel);
        pageManagement.addComponent(next);
        pageManagement.addComponent(last);
        pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(currentPageTextField, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(separatorLabel, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
        pageManagement.setWidth(null);
        pageManagement.setSpacing(true);

        addListener(new PageChangeListener() {
            @Override
            public void pageChanged(PagedTableChangeEvent event) {
                first.setEnabled(container.getStartIndex() > 0);
                previous.setEnabled(container.getStartIndex() > 0);
                next.setEnabled(container.getStartIndex() < container
                        .getRealSize() - getPageLength());
                last.setEnabled(container.getStartIndex() < container
                        .getRealSize() - getPageLength());
                currentPageTextField.setValue(String.valueOf(getCurrentPage()));
                totalPagesLabel.setValue(getTotalAmountOfPages());
            }
        });

        return pageManagement;
    }
    
    public HorizontalLayout createControls(I18NSource messageSource) {
        return createControls(messageSource.getMessage("pagedtable.itemsperpage"), messageSource.getMessage("pagedtable.page"));
    }

    public HorizontalLayout createControls(String itemsPerPageCaption, String pageCaption) {
        HorizontalLayout pageSize = createPageSizeControls(itemsPerPageCaption);
        HorizontalLayout pageManagement = createPageManagementControls(pageCaption);

        HorizontalLayout controlBar = new HorizontalLayout();
        controlBar.addComponent(pageSize);
        controlBar.addComponent(pageManagement);
        controlBar.setComponentAlignment(pageManagement, Alignment.MIDDLE_CENTER);
        controlBar.setWidth("100%");
        controlBar.setExpandRatio(pageSize, 1);

        return controlBar;
    }

    @Override
    public Container.Indexed getContainerDataSource() {
        return container;
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        if (!(newDataSource instanceof Container.Indexed)) {
            throw new IllegalArgumentException("PagedTable can only use containers that implement Container.Indexed");
        }
        PagedTableContainer pagedTableContainer = new PagedTableContainer((Container.Indexed) newDataSource);
        pagedTableContainer.setPageLength(getPageLength());
        if (newDataSource instanceof Container.ItemSetChangeNotifier) {
            Container.ItemSetChangeNotifier notifier = (ItemSetChangeNotifier) newDataSource;
            notifier.addListener(this);
        }
        super.setContainerDataSource(pagedTableContainer);
        this.container = pagedTableContainer;
        firePagedChangedEvent();
    }

    private void setPageFirstIndex(int firstIndex) {
        if (container != null) {
            if (firstIndex <= 0) {
                firstIndex = 0;
            }
            if (firstIndex > container.getRealSize() - 1) {
                int size = container.getRealSize() - 1;
                int pages = 0;
                if (getPageLength() != 0) {
                    pages = (int) Math.floor((double)size / getPageLength());
                }
                firstIndex = pages * getPageLength();
            }
            container.setStartIndex(firstIndex);
            containerItemSetChange(new Container.ItemSetChangeEvent() {
                public Container getContainer() {
                    return container;
                }
            });
            if (alwaysRecalculateColumnWidths) {
                for (Object columnId : container.getContainerPropertyIds()) {
                    setColumnWidth(columnId, -1);
                }
            }
            firePagedChangedEvent();
        }
    }

    public void firePagedChangedEvent() {
        if (listeners != null) {
            PagedTableChangeEvent event = new PagedTableChangeEvent(this);
            for (PageChangeListener listener : listeners) {
                listener.pageChanged(event);
            }
        }
    }

    @Override
    public void setPageLength(int pageLength) {
        if (pageLength >= 0 && getPageLength() != pageLength) {
            container.setPageLength(pageLength);
            super.setPageLength(pageLength);
            firePagedChangedEvent();
        }
    }

    public void nextPage() {
        setPageFirstIndex(container.getStartIndex() + getPageLength());
    }

    public void previousPage() {
        setPageFirstIndex(container.getStartIndex() - getPageLength());
    }

    public int getCurrentPage() {
        double pageLength = getPageLength();
        int page = (int) Math.floor((double) container.getStartIndex() / pageLength) + 1;
        if (page < 1) {
            page = 1;
        }
        return page;
    }

    public void setCurrentPage(int page) {
        int newIndex = (page - 1) * getPageLength();
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex >= 0 && newIndex != container.getStartIndex()) {
            setPageFirstIndex(newIndex);
        }
    }

    public int getTotalAmountOfPages() {
        int size = container.getContainer().size();
        double pageLength = getPageLength();
        int pageCount = (int) Math.ceil(size / pageLength);
        if (pageCount < 1) {
            pageCount = 1;
        }
        return pageCount;
    }

    public void addListener(PageChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<PageChangeListener>();
        }
        listeners.add(listener);
    }

    public void removeListener(PageChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<PageChangeListener>();
        }
        listeners.remove(listener);
    }

    public void setAlwaysRecalculateColumnWidths(boolean alwaysRecalculateColumnWidths) {
        this.alwaysRecalculateColumnWidths = alwaysRecalculateColumnWidths;
    }

}
