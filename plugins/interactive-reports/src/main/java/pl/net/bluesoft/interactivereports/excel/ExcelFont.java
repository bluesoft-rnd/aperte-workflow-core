package pl.net.bluesoft.interactivereports.excel;

/**
 * User: POlszewski
 * Date: 2014-07-03
 */
public class ExcelFont {
	private String name;
	private Integer height;
	private ExcelColor color;
	private boolean bold;
	private boolean italic;

	public ExcelFont() {}

	private ExcelFont(String name, Integer height, ExcelColor color, boolean bold, boolean italic) {
		this.name = name;
		this.height = height;
		this.color = color;
		this.bold = bold;
		this.italic = italic;
	}

	public ExcelFont copy() {
		return new ExcelFont(name, height, color, bold, italic);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public ExcelColor getColor() {
		return color;
	}

	public void setColor(ExcelColor color) {
		this.color = color;
	}

	public boolean isBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExcelFont excelFont = (ExcelFont)o;

		if (bold != excelFont.bold) return false;
		if (italic != excelFont.italic) return false;
		if (color != excelFont.color) return false;
		if (height != null ? !height.equals(excelFont.height) : excelFont.height != null) return false;
		if (name != null ? !name.equals(excelFont.name) : excelFont.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (height != null ? height.hashCode() : 0);
		result = 31 * result + (color != null ? color.hashCode() : 0);
		result = 31 * result + (bold ? 1 : 0);
		result = 31 * result + (italic ? 1 : 0);
		return result;
	}
}
