package pl.net.bluesoft.interactivereports.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2014-06-27
 */
public class ExcelBuilder {
	private static final Logger LOGGER = Logger.getLogger(ExcelBuilder.class.getName());

	private Workbook workbook;
	private Sheet sheet;
	private DataFormat dateFormat;
	private final Map<ExcelCellStyle, CellStyle> styles = new HashMap<ExcelCellStyle, CellStyle>();
	private final Map<ExcelFont, Font> fonts = new HashMap<ExcelFont, Font>();

	private int x, y, maxX = -1;
	private Row curRow;
	private Cell curCell;

	private int newRowX;

	public static ExcelBuilder createXlsBuilder() {
		return new ExcelBuilder(new HSSFWorkbook());
	}

	public static ExcelBuilder createXlsxBuilder() {
		return new ExcelBuilder(new XSSFWorkbook());
	}

	public static ExcelBuilder create(String extension) {
		if (extension.equalsIgnoreCase("xls")) {
			return createXlsBuilder();
		}
		if (extension.equalsIgnoreCase("xlsx")) {
			return createXlsxBuilder();
		}
		throw new IllegalArgumentException("Unsupported format: " + extension);
	}

	private ExcelBuilder(Workbook workbook) {
		this.workbook = workbook;
		this.dateFormat = workbook.createDataFormat();
	}

	public void createSheet() {
		String name = new String();
		if (sheet != null) {
			autoSizeColumns();
			maxX = -1;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date today = Calendar.getInstance().getTime();
		name = "raport_"  + dateFormat.format(today);
		this.sheet = workbook.createSheet(name);

	}

	public void autoSizeColumn(int columnIndex, boolean autosize)
	{
		this.sheet.autoSizeColumn(columnIndex, autosize);
	}

	public void setColumnWidth(int columnIndex, int width)
	{
		this.sheet.autoSizeColumn(columnIndex, false);
		this.sheet.setColumnWidth(columnIndex, width);
	}

	public void createSheet(String name) {
		if (sheet != null) {
			autoSizeColumns();
			maxX = -1;
		}
		this.sheet = workbook.createSheet(name);
	}

	public void autoSizeColumns() {
		if (sheet == null) {
			return;
		}
		for (int i = 0; i <= maxX; ++i) {
			sheet.autoSizeColumn(i, true);
		}
	}

	public void toStream(OutputStream output) {
		autoSizeColumns();

		try {
			workbook.write(output);
			output.close();
		}
		catch (Exception e) {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
				}
			}
			throw new RuntimeException(e);
		}
	}

	public byte[] toByteArray() {
		ByteArrayOutputStream output = new ByteArrayOutputStream(8 * 1024);
		toStream(output);
		return output.toByteArray();
	}

	private Row getCurrentRow() {
		if (curRow == null) {
			curRow = createRow(y);
		}
		return curRow;
	}

	private Row createRow(int y) {
		Row row = sheet.getRow(y);
		if (row == null) {
			row = sheet.createRow(y);
		}
		return row;
	}

	private Cell getCurrentCell() {
		if (curCell == null) {
			Row row = getCurrentRow();
			curCell = createCell(row, x);
		}
		return curCell;
	}

	private Cell createCell(Row row, int x) {
		Cell cell = row.getCell(x);
		if (cell == null) {
			cell = row.createCell(x);
		}
		return cell;
	}

	private CellStyle getStyle(ExcelCellStyle excelStyle) {
		CellStyle style = styles.get(excelStyle);

		if (style == null) {
			style = workbook.createCellStyle();
			if (excelStyle.getFormat() != null) {
				style.setDataFormat(dateFormat.getFormat(excelStyle.getFormat()));
			}
			if (excelStyle.getBgColor() != null) {
				style.setFillForegroundColor(excelStyle.getBgColor().getIndex());
				style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			}
			if (excelStyle.getFont() != null) {
				style.setFont(getFont(excelStyle.getFont()));
			}
			if (excelStyle.getAlignment() != null) {
				style.setAlignment(excelStyle.getAlignment().getIndex());
			}
			if (excelStyle.getVerticalAlignment() != null) {
				style.setVerticalAlignment(excelStyle.getVerticalAlignment().getIndex());
			}
            if (excelStyle.getBorderLeft() != null){
                style.setBorderLeft(excelStyle.getBorderLeft().getIndex());
            }
            if (excelStyle.getBorderRight() != null){
                style.setBorderRight(excelStyle.getBorderRight().getIndex());
            }
            if (excelStyle.getBorderTop() != null){
                style.setBorderTop(excelStyle.getBorderTop().getIndex());
            }
            if (excelStyle.getBorderBottom() != null){
                style.setBorderBottom(excelStyle.getBorderBottom().getIndex());
            }
			style.setWrapText(excelStyle.getWrapText()); //boolean, not Boolean

			styles.put(excelStyle, style);
		}
		return style;
	}

	private Font getFont(ExcelFont excelFont) {
		Font font = fonts.get(excelFont);

		if (font == null) {
			font = workbook.createFont();
			if (excelFont.getName() != null) {
				font.setFontName(excelFont.getName());
			}
			if (excelFont.getColor() != null) {
				font.setColor(excelFont.getColor().getIndex());
			}
			if (excelFont.getHeight() != null) {
				font.setFontHeightInPoints((short)excelFont.getHeight().intValue());
			}
			if (excelFont.isBold()) {
				font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			}
			if (excelFont.isItalic()) {
				font.setItalic(true);
			}
			fonts.put(excelFont, font);
		}
		return font;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public ExcelBuilder setNewRowX() {
		return setNewRowX(x);
	}

	public ExcelBuilder setNewRowX(int newRowX) {
		this.newRowX = newRowX;
		return this;
	}

	public ExcelBuilder nextRow() {
		return nextRow(1);
	}

	public ExcelBuilder nextRow(int rowCount) {
		return moveTo(newRowX, y + rowCount);
	}

	public ExcelBuilder nextCol() {
		return nextCol(1);
	}

	public ExcelBuilder nextCol(int colCount) {
		return moveBy(colCount, 0);
	}

	public ExcelBuilder moveBy(int xOffset, int yOffset) {
		return moveTo(x + xOffset, y + yOffset);
	}

	public ExcelBuilder moveTo(int x, int y) {
		if (y != this.y) {
			curCell = null;
			curRow = null;
			this.y = y;
		}
		if (x != this.x) {
			curCell = null;
			this.x = x;
			this.maxX = Math.max(this.maxX, this.x);
		}
		return this;
	}

	public ExcelBuilder setValue(double value) {
		return setValue(value, (ExcelCellStyle)null);
	}

	public ExcelBuilder setValue(double value, String format) {
		return setValue(value, new ExcelCellStyle().setFormat(format));
	}

	public ExcelBuilder setValue(double value, ExcelCellStyle style) {
		getCurrentCell().setCellValue(value);
		applyStyle(style);
		return nextCol();
	}

	public ExcelBuilder setValue(double value, int numCols, int numRows, ExcelCellStyle style) {
		return merge(numCols, numRows, style).setValue(value);
	}

	public ExcelBuilder setValue(double value, int numCols, int numRows, String format) {
		return merge(numCols, numRows, new ExcelCellStyle().setFormat(format)).setValue(value);
	}

	public ExcelBuilder setValue(String value) {
		return setValue(value, null);
	}

	public ExcelBuilder setValue(String value, ExcelCellStyle style) {
		getCurrentCell().setCellValue(value);
		applyStyle(style);
		return nextCol();
	}

	public ExcelBuilder setValue(String value, int numCols) {
		return setValue(value, numCols, 1, null);
	}

	public ExcelBuilder setValue(String value, int numCols, int numRows) {
		return setValue(value, numCols, numRows, null);
	}

	public ExcelBuilder setValue(String value, int numCols, ExcelCellStyle style) {
		return setValue(value, numCols, 1, style);
	}

	public ExcelBuilder setValue(String value, int numCols, int numRows, ExcelCellStyle style) {
		return merge(numCols, numRows, style).setValue(value, null).nextCol(numCols - 1);
	}

	public ExcelBuilder setValue(Date value) {
		return setValue(value, (ExcelCellStyle)null);
	}

	public ExcelBuilder setValue(Date value, ExcelCellStyle style) {
		getCurrentCell().setCellValue(value);
		applyStyle(style);
		return nextCol();
	}

	public ExcelBuilder merge(int numCols, int numRows, ExcelCellStyle style) {
		if (numCols > 1 || numRows > 1) {
			CellRangeAddress region = new CellRangeAddress(y, y + numRows - 1, x, x + numCols - 1);

			applyStyle(region, style);

			sheet.addMergedRegion(region);
		}
		else {
			applyStyle(style);
		}

		return this;
	}

	private void applyStyle(ExcelCellStyle style) {
		if (style != null) {
			getCurrentCell().setCellStyle(getStyle(style));
		}
	}

	private void applyStyle(CellRangeAddress region, ExcelCellStyle style) {
		if (style != null) {
			CellStyle cellStyle = getStyle(style);

			for (int rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); ++rowNum) {
				Row row = createRow(rowNum);

				for (int colNum = region.getFirstColumn(); colNum <= region.getLastColumn(); ++colNum) {
					Cell cell = createCell(row, colNum);

					cell.setCellStyle(cellStyle);
				}
			}
		}
	}
}
