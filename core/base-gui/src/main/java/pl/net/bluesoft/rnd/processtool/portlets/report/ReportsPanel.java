package pl.net.bluesoft.rnd.processtool.portlets.report;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.text.NumberTextField;
import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import pl.net.bluesoft.rnd.processtool.dao.impl.ReportDAOImpl;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportDefinition;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportParam;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportParamType;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.math.BigDecimal;
import java.util.*;

import static org.aperteworkflow.util.vaadin.LocalizedFormats.getShortDateFormatStr;
import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.DateUtil.truncHours;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:11
 */
class ReportsPanel extends VerticalLayout {
	private final I18NSource i18NSource;

	private Select reportSelect;
	private FormLayout paramsLayout;
	private ReportResultTable table;
	private Button generateButton;

	private ReportDefinition reportDefinition;
	private final Map<ReportParam, Field> paramToComponent = new HashMap<ReportParam, Field>();

	public ReportsPanel(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
		buildLayout();
	}

	private void buildLayout() {
		reportSelect = new Select(i18NSource.getMessage("Raport"));
		reportSelect.setWidth("100%");
		reportSelect.setImmediate(true);
		reportSelect.setNullSelectionAllowed(false);
		reportSelect.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				reportSelected((String)event.getProperty().getValue());
			}
		});

		paramsLayout = new FormLayout();
		paramsLayout.setWidth("100%");

		addComponent(reportSelect);
		addComponent(paramsLayout);

		for (Map.Entry<String, String> entry : getReportDAO().getReportIds().entrySet()) {
			reportSelect.addItem(entry.getKey());
			reportSelect.setItemCaption(entry.getKey(), i18NSource.getMessage(entry.getValue()));
		}
	}

	private void reportSelected(String reportId) {
		removeReportParams();
		removeTable();

		reportDefinition = getReportDAO().getReportDefinition(reportId);

		if (generateButton == null) {
			generateButton = createGenerateReportButton();
			addComponent(generateButton, 2);
		}

		addReportParams();
	}

	private void removeTable() {
		if (table != null) {
			removeComponent(table);
		}
	}

	private ReportDAOImpl getReportDAO() {
		return new ReportDAOImpl(getThreadProcessToolContext().getHibernateSession());
	}

	private void addReportParams() {
		for (ReportParam reportParam : reportDefinition.getParams()) {
			Field paramField = createParamField(reportParam);
			((AbstractComponent)paramField).setImmediate(true);
			paramsLayout.addComponent(paramField);
			paramToComponent.put(reportParam, paramField);
		}
	}

	private void removeReportParams() {
		for (Field field : paramToComponent.values()) {
			paramsLayout.removeComponent(field);
		}
		paramToComponent.clear();
	}

	private Field createParamField(ReportParam reportParam) {
		String caption = i18NSource.getMessage(reportParam.getDescription());

		if (reportParam.getType() == ReportParamType.DATE) {
			PopupDateField date = new PopupDateField(caption);
			date.setDateFormat(getShortDateFormatStr(i18NSource));
			date.setResolution(DateField.RESOLUTION_DAY);
			return date;
		}
		else if (reportParam.getType() == ReportParamType.INT) {
			NumberTextField field = new NumberTextField(caption);
			field.setNullRepresentation("");
			return field;
		}
		else if (reportParam.getType() == ReportParamType.BOOL) {
			return new CheckBox(caption);
		}
		else if (reportParam.getType() == ReportParamType.TEXT && hasText(reportParam.getValues())) {
			Select select = new Select(caption);
			for (String value : reportParam.getValues().split(";")) {
				select.addItem(value);
			}
			return select;
		}
		else {
			return new TextField(caption);
		}
	}

	private Button createGenerateReportButton() {
		Button generateButton = new Button(i18NSource.getMessage("Generate"));

		generateButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				generateReport();
			}
		});
		return generateButton;
	}

	private void generateReport() {
		removeTable();

		List<Object[]> rows = issueQuery();

		table = new ReportResultTable(reportDefinition.getColumns(), i18NSource);
		table.setItems(rows);
		addComponent(table);
	}

	private List<Object[]> issueQuery() {
		SQLQuery query = getThreadProcessToolContext().getHibernateSession().createSQLQuery(reportDefinition.getQuery());

		for (Map.Entry<ReportParam, Field> entry : paramToComponent.entrySet()) {
			Object value = entry.getValue().getValue();

			if (value instanceof Collection<?>) {
				query.setParameterList(entry.getKey().getName(), (Collection<?>)value);
			}
			else {
				if (value instanceof String && ((String)value).isEmpty()) {
					value = null;
				}
				else if (value instanceof Date) {
					value = truncHours((Date)value);
				}
				else if (entry.getKey().getType() == ReportParamType.INT && value != null) {
					value = getBD(value).intValue();
				}
				query.setParameter(entry.getKey().getName(), value, getType(entry.getKey()));
			}
		}
		return (List<Object[]>)query.list();
	}

	private static BigDecimal getBD(Object value) {
		return value instanceof String ? new BigDecimal(((String)value).replace(",", ".")) : null;
	}

	private Type getType(ReportParam param) {
		switch (param.getType()) {
			case TEXT:
				return StandardBasicTypes.TEXT;
			case DATE:
				return StandardBasicTypes.DATE;
			case INT:
				return StandardBasicTypes.INTEGER;
			case BOOL:
				return StandardBasicTypes.BOOLEAN;
			default:
				throw new RuntimeException("Unhandled type: " + param.getType());
		}
	}
}
