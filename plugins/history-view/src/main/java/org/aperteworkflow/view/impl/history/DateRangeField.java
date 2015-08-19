package org.aperteworkflow.view.impl.history;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;
import org.aperteworkflow.util.vaadin.ui.OrderedLayoutFactory;
import org.aperteworkflow.util.vaadin.ui.date.OptionalDateField;
import org.vaadin.addon.customfield.CustomField;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroup;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroupItemComponent;
import org.vaadin.risto.stepper.IntStepper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.eventbus.listenables.Listenable;
import pl.net.bluesoft.util.eventbus.listenables.ListenableSupport;
import pl.net.bluesoft.util.lang.DateUtil;
import pl.net.bluesoft.util.lang.Transformer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;


/**
 * @author: amichalak@bluesoft.net.pl
 */

public class DateRangeField extends CustomField implements Listenable<DateRangeField.DateRangeListener> {
    private ListenableSupport<DateRangeListener> listenable = ListenableSupport.strongListenable();

    private I18NSource messageSource;

    private OptionalDateField fromDate;
    private OptionalDateField toDate;
    private PopupDateField latestDate;
    private IntStepper stepper;

    private ConfigurableOptionGroupField<Mode> optionGroup;

    public enum Mode {
        RANGE, LATEST
    }

    private Mode[] allowedModes = new Mode[] {Mode.LATEST, Mode.RANGE};
    private Mode currentMode = Mode.LATEST;

    private boolean allowValueUpdate = false;

    public DateRangeField(I18NSource messageSource) {
        this.messageSource = messageSource;
        init();
    }

    @Override
    public Class<?> getType() {
        return DateRange.class;
    }

    private void init() {
        if (allowedModes.length == 0) {
            throw new IllegalArgumentException("Allowed modes cannot be empty!");
        }

        VerticalLayout root = new VerticalLayout();
        root.setWidth("100%");
        root.setSpacing(true);

        if (allowedModes.length > 1) {
            optionGroup = new ConfigurableOptionGroupField<Mode>(Arrays.asList(allowedModes))
                    .setMultiSelect(false)
                    .setItemComponentGenerator(new ConfigurableOptionGroupField.ItemComponentGenerator<Mode>() {
                        @Override
                        public Component generate(FlexibleOptionGroup optionGroup, Mode itemId, FlexibleOptionGroupItemComponent itemComponent,
                                                  Transformer<Mode, String> itemCaptionResolver) {
                            AligningHorizontalLayout ahl = new AligningHorizontalLayout(Alignment.MIDDLE_LEFT);
                            ahl.setSpacing(true);
                            ahl.setMargin(false, true, false, false);
                            ahl.setData(itemCaptionResolver);
                            ahl.addComponent(itemComponent);
                            ahl.addComponent(createControlsForMode(itemId));
                            ahl.setData(itemComponent);
                            return ahl;
                        }
                    })
                    .init();
            optionGroup.setValue(currentMode);
            optionGroup.addOptionChangedListener(new ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    currentMode = (Mode) event.getProperty().getValue();
                    updateValues();
                }
            });
            root.addComponent(optionGroup);
        }
        else {
            currentMode = allowedModes[0];
            Component controls = createControlsForMode(currentMode);
            root.addComponent(controls);
        }
        allowValueUpdate = true;
        setCompositionRoot(root);
    }

    public void setOptionGroupLayoutFactory(OrderedLayoutFactory layoutFactory) {
        optionGroup.setLayoutFactory(layoutFactory).init();
    }

    public void setAllowedModes(Mode[] allowedModes) {
        this.allowedModes = new Mode[allowedModes.length];
        System.arraycopy(allowedModes, 0, this.allowedModes, 0, allowedModes.length);
        init();
    }

    public void setDateFormat(String dateFormat) {
        this.fromDate.setDateFormat(dateFormat);
        this.toDate.setDateFormat(dateFormat);
        this.latestDate.setDateFormat(dateFormat);
    }

    public void setDateStepperMaxValue(int maxValue) {
        stepper.setMaxValue(Math.max(maxValue, 0));
    }

    public void setDateStepperBaseValue(int value) {
        stepper.setValue(value);
    }

    private HorizontalLayout createLatestDateControls() {
        latestDate = new PopupDateField();
        latestDate.setDateFormat(VaadinUtility.SIMPLE_DATE_FORMAT_STRING);
        latestDate.setEnabled(false);
        latestDate.setWidth("100px");
        latestDate.setCaption(messageSource.getMessage("date.field.from"));

        stepper = new IntStepper();
        stepper.setMaxValue(15);
        stepper.setMinValue(0);
        stepper.setStepAmount(1);
        stepper.setWidth("50px");
        stepper.setImmediate(true);
        stepper.setValue(5);
        stepper.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateLatestVisualDate();
                updateValues();
            }
        });
        updateLatestVisualDate();

        return VaadinUtility.horizontalLayout(Alignment.MIDDLE_CENTER, new Label(messageSource.getMessage("date.field.latest")), stepper,
                new Label(messageSource.getMessage("date.field.days")) {{
                    setWidth("20px");
                }}, latestDate);
    }

    private void updateLatestVisualDate() {
        int value = stepper.getValue() != null ? (Integer) stepper.getValue() : 0;
        latestDate.setValue(DateUtil.addDays(new Date(), -value));
    }

    private HorizontalLayout createDateRangeControls() {
        fromDate = new OptionalDateField(messageSource);
        fromDate.setValue(DateUtil.addDays(new Date(), -5));
        fromDate.addDateChangedListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateValues();
            }
        });

        toDate = new OptionalDateField(messageSource);
        toDate.setValue(new Date());
        toDate.addDateChangedListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateValues();
            }
        });

        fromDate.setCaption(messageSource.getMessage("date.field.from"));
        toDate.setCaption(messageSource.getMessage("date.field.to"));

        return VaadinUtility.horizontalLayout(Alignment.MIDDLE_CENTER, fromDate, toDate);
    }

    private Component createControlsForMode(Mode itemId) {
        switch (itemId) {
            case LATEST:
                return createLatestDateControls();
            case RANGE:
                return createDateRangeControls();
        }
        throw new IllegalArgumentException("Cannot determine date range mode: " + itemId);
    }

    private void updateValues() {
        if (allowValueUpdate) {
            DateRange value = (DateRange) getValue();
            if (value != null) {
                value.setStartDate(getStartDate());
                value.setEndDate(getEndDate());
                value.setMode(currentMode);
            }
            else {
                setValue(new DateRange(getStartDate(), getEndDate(), currentMode));
            }
            fireDateRangeChangedEvent();
        }
    }

    @Override
    protected void setInternalValue(Object newValue) {
        if (!(newValue instanceof DateRange)) {
            throw new IllegalArgumentException("Unable to handle non-date-range values");
        }
        super.setInternalValue(newValue);
        updateValuesFromOriginal((DateRange) newValue);
    }

    private void updateValuesFromOriginal(DateRange originalValue) {
        if (originalValue.getMode() == null) {
            originalValue.setMode(Mode.RANGE);
        }
        allowValueUpdate = false;
        currentMode = originalValue.getMode();
        switch (currentMode) {
            case LATEST:
                latestDate.setValue(originalValue.getStartDate());
                Long diff = DateUtil.diffDays(originalValue.getStartDate(), new Date());
                stepper.setValue(diff.intValue());
                break;
            case RANGE:
                fromDate.setValue(originalValue.getStartDate());
                toDate.setValue(originalValue.getEndDate());
                break;
        }
        optionGroup.setValue(currentMode);
        allowValueUpdate = true;
        fireDateRangeChangedEvent();
    }

    public Date getStartDate() {
        switch (currentMode) {
            case RANGE:
                return fromDate.dateValue();
            case LATEST:
                return (Date) latestDate.getValue();
            default:
                return null;
        }
    }

    public Date getEndDate() {
        switch (currentMode) {
            case RANGE:
                return toDate.dateValue();
            case LATEST:
            default:
                return new Date();
        }
    }

    private void fireDateRangeChangedEvent() {
        listenable.fireEvent(new DateRangeChangedEvent(this));
    }

    @Override
    public void addListener(DateRangeListener listener) {
        listenable.addListener(listener);
    }

    @Override
    public void removeListener(DateRangeListener listener) {
        listenable.removeListener(listener);
    }

    public static class DateRange implements Serializable {
        private Date startDate;
        private Date endDate;
        private Mode mode;

        public DateRange() {
        }

        public DateRange(DateRange copy) {
            this(copy.getStartDate(), copy.getEndDate(), copy.getMode());
        }

        public DateRange(Date startDate, Date endDate, Mode mode) {
            this.startDate = DateUtil.copyDate(startDate);
            this.endDate = DateUtil.copyDate(endDate);
            this.mode = mode;
        }

        public void update(DateRange copy) {
            this.startDate = DateUtil.copyDate(copy.getStartDate());
            this.endDate = DateUtil.copyDate(copy.getEndDate());
            this.mode = copy.getMode();
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public boolean isSet() {
            return Mode.LATEST.equals(mode) && startDate != null || Mode.RANGE.equals(mode);

        }
    }

    public static class DateRangeChangedEvent {
        private DateRangeField source;

        public DateRangeChangedEvent(DateRangeField source) {
            this.source = source;
        }

        public DateRangeField getSource() {
            return source;
        }

        public Date getStartDate() {
            return getSource().getStartDate();
        }

        public Date getEndDate() {
            return getSource().getEndDate();
        }
    }

    public static interface DateRangeListener extends EventListener<DateRangeChangedEvent> {
    }
}
