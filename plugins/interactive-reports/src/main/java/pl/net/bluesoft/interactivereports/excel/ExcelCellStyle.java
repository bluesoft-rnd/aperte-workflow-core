package pl.net.bluesoft.interactivereports.excel;

import org.apache.poi.ss.usermodel.CellStyle;

/**
 * User: POlszewski
 * Date: 2014-07-03
 */
public class ExcelCellStyle {
	private String format;
	private ExcelColor bgColor;
	private ExcelFont font;
	private boolean wrapText = true;

	public enum Alignment {
		LEFT(CellStyle.ALIGN_LEFT),
		CENTER(CellStyle.ALIGN_CENTER),
		RIGHT(CellStyle.ALIGN_RIGHT);

		private final short index;

		Alignment(short index) {
			this.index = index;
		}

		public short getIndex() {
			return index;
		}
	}

	public enum VerticalAlignment {
		TOP(CellStyle.VERTICAL_TOP),
		CENTER(CellStyle.VERTICAL_CENTER),
		BOTTOM(CellStyle.VERTICAL_BOTTOM);

		private final short index;

		VerticalAlignment(short index) {
			this.index = index;
		}

		public short getIndex() {
			return index;
		}
	}

    public enum Border {
        NONE(CellStyle.BORDER_NONE),
        THIN(CellStyle.BORDER_THIN),
        MEDIUM(CellStyle.BORDER_MEDIUM),
        DASHED(CellStyle.BORDER_DASHED),
        HAIR(CellStyle.BORDER_HAIR),
        THICK(CellStyle.BORDER_THICK),
        DOUBLE(CellStyle.BORDER_DOUBLE),
        DOTTED(CellStyle.BORDER_DOTTED),
        MEDIUM_DASHED(CellStyle.BORDER_MEDIUM_DASHED),
        DASH_DOT(CellStyle.BORDER_DASH_DOT),
        MEDIUM_DASH_DOT(CellStyle.BORDER_MEDIUM_DASH_DOT),
        DASH_DOT_DOT(CellStyle.BORDER_DASH_DOT_DOT),
        MEDIUM_DASH_DOT_DOT(CellStyle.BORDER_MEDIUM_DASH_DOT_DOT),
        SLANTED_DASH_DOT (CellStyle.BORDER_SLANTED_DASH_DOT);

        private final short index;

        Border(short index) {
            this.index = index;
        }

        public short getIndex() {
            return index;
        }
    }

	private Alignment alignment;
	private VerticalAlignment verticalAlignment;
    private Border borderLeft, borderRight, borderTop, borderBottom;

	public ExcelCellStyle() {}

	public ExcelCellStyle(String format, ExcelColor bgColor, ExcelFont font, Alignment alignment, VerticalAlignment verticalAlignment,
						  Border borderLeft, Border borderRight, Border borderTop, Border borderBottom) {
		this.format = format;
		this.bgColor = bgColor;
		this.font = font;
		this.alignment = alignment;
		this.verticalAlignment = verticalAlignment;
		this.borderLeft = borderLeft;
		this.borderRight = borderRight;
		this.borderTop = borderTop;
		this.borderBottom = borderBottom;
	}

	public ExcelCellStyle copy() {
		return new ExcelCellStyle(format, bgColor, font, alignment, verticalAlignment, borderLeft, borderRight, borderTop, borderBottom);
	}

	public ExcelCellStyle copyFont() {
		if (font != null) {
			font = font.copy();
		}
		return this;
	}

	public boolean getWrapText() {
		return wrapText;
	}

	public void setWrapText(boolean wrapText) {
		this.wrapText = wrapText;
	}

	public String getFormat() {
		return format;
	}

	public ExcelCellStyle setFormat(String format) {
		this.format = format;
		return this;
	}

	public ExcelColor getBgColor() {
		return bgColor;
	}

	public ExcelCellStyle setBgColor(ExcelColor bgColor) {
		this.bgColor = bgColor;
		return this;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public ExcelCellStyle setAlignment(Alignment alignment) {
		this.alignment = alignment;
		return this;
	}

	public ExcelCellStyle alignLeft() {
		return setAlignment(Alignment.LEFT);
	}

	public ExcelCellStyle alignCenter() {
		return setAlignment(Alignment.CENTER);
	}

	public ExcelCellStyle alignRight() {
		return setAlignment(Alignment.RIGHT);
	}

	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	public ExcelCellStyle setVerticalAlignment(VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
		return this;
	}

	public ExcelCellStyle valignTop() {
		return setVerticalAlignment(VerticalAlignment.TOP);
	}

	public ExcelCellStyle valignCenter() {
		return setVerticalAlignment(VerticalAlignment.CENTER);
	}

	public ExcelCellStyle valignBottom() {
		return setVerticalAlignment(VerticalAlignment.BOTTOM);
	}

	public ExcelFont getFont() {
		return font;
	}

	public ExcelCellStyle setFont(ExcelFont font) {
		this.font = font;
		return this;
	}

	public ExcelCellStyle setFontName(String name) {
		ensureFont().setName(name);
		return this;
	}

	public ExcelCellStyle setFontHeight(int height) {
		ensureFont().setHeight(height);
		return this;
	}

	public ExcelCellStyle setColor(ExcelColor color) {
		ensureFont().setColor(color);
		return this;
	}

	public ExcelCellStyle setBold(boolean bold) {
		ensureFont().setBold(bold);
		return this;
	}

	public ExcelCellStyle setItalic(boolean italic) {
		ensureFont().setItalic(italic);
		return this;
	}

	private ExcelFont ensureFont() {
		if (font == null) {
			font = new ExcelFont();
		}
		return font;
	}

    public ExcelCellStyle borderLeft(Border border) {
        this.borderLeft = border;
        return this;
    }

    public ExcelCellStyle borderRight(Border border) {
        this.borderRight = border;
        return this;
    }

    public ExcelCellStyle borderTop(Border border) {
        this.borderTop = border;
        return this;
    }

    public ExcelCellStyle borderBottom(Border border) {
        this.borderBottom = border;
        return this;
    }

    public ExcelCellStyle border(Border border) {
        borderLeft(border);
        borderRight(border);
        borderTop(border);
        borderBottom(border);
        return this;
    }

    public Border getBorderLeft() {
        return borderLeft;
    }

    public Border getBorderRight() {
        return borderRight;
    }

    public Border getBorderTop() {
        return borderTop;
    }

    public Border getBorderBottom() {
        return borderBottom;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExcelCellStyle that = (ExcelCellStyle)o;

		if (alignment != that.alignment) return false;
		if (bgColor != that.bgColor) return false;
		if (borderBottom != that.borderBottom) return false;
		if (borderLeft != that.borderLeft) return false;
		if (borderRight != that.borderRight) return false;
		if (borderTop != that.borderTop) return false;
		if (font != null ? !font.equals(that.font) : that.font != null) return false;
		if (format != null ? !format.equals(that.format) : that.format != null) return false;
		if (verticalAlignment != that.verticalAlignment) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = format != null ? format.hashCode() : 0;
		result = 31 * result + (bgColor != null ? bgColor.hashCode() : 0);
		result = 31 * result + (font != null ? font.hashCode() : 0);
		result = 31 * result + (alignment != null ? alignment.hashCode() : 0);
		result = 31 * result + (verticalAlignment != null ? verticalAlignment.hashCode() : 0);
		result = 31 * result + (borderLeft != null ? borderLeft.hashCode() : 0);
		result = 31 * result + (borderRight != null ? borderRight.hashCode() : 0);
		result = 31 * result + (borderTop != null ? borderTop.hashCode() : 0);
		result = 31 * result + (borderBottom != null ? borderBottom.hashCode() : 0);
		return result;
	}
}
