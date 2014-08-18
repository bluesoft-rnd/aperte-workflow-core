package pl.net.bluesoft.interactivereports.excel;

import org.apache.poi.hssf.util.HSSFColor;

/**
 * User: POlszewski
 * Date: 2014-07-03
 */
public enum ExcelColor {
	BLACK(HSSFColor.BLACK.index),
	BROWN(HSSFColor.BROWN.index),
	OLIVE_GREEN(HSSFColor.OLIVE_GREEN.index),
	DARK_GREEN(HSSFColor.DARK_GREEN.index),
	DARK_TEAL(HSSFColor.DARK_TEAL.index),
	DARK_BLUE(HSSFColor.DARK_BLUE.index),
	INDIGO(HSSFColor.INDIGO.index),
	GREY_80_PERCENT(HSSFColor.GREY_80_PERCENT.index),
	ORANGE(HSSFColor.ORANGE.index),
	DARK_YELLOW(HSSFColor.DARK_YELLOW.index),
	GREEN(HSSFColor.GREEN.index),
	TEAL(HSSFColor.TEAL.index),
	BLUE(HSSFColor.BLUE.index),
	BLUE_GREY(HSSFColor.BLUE_GREY.index),
	GREY_50_PERCENT(HSSFColor.GREY_50_PERCENT.index),
	RED(HSSFColor.RED.index), 
	LIGHT_ORANGE(HSSFColor.LIGHT_ORANGE.index),
	LIME(HSSFColor.LIME.index),
	SEA_GREEN(HSSFColor.SEA_GREEN.index),
	AQUA(HSSFColor.AQUA.index),
	LIGHT_BLUE(HSSFColor.LIGHT_BLUE.index),
	VIOLET(HSSFColor.VIOLET.index),
	GREY_40_PERCENT(HSSFColor.GREY_40_PERCENT.index),
	PINK(HSSFColor.PINK.index),
	GOLD(HSSFColor.GOLD.index),
	YELLOW(HSSFColor.YELLOW.index),
	BRIGHT_GREEN(HSSFColor.BRIGHT_GREEN.index),
	TURQUOISE(HSSFColor.TURQUOISE.index),
	DARK_RED(HSSFColor.DARK_RED.index),
	SKY_BLUE(HSSFColor.SKY_BLUE.index),
	PLUM(HSSFColor.PLUM.index),
	GREY_25_PERCENT(HSSFColor.GREY_25_PERCENT.index),
	ROSE(HSSFColor.ROSE.index),
	LIGHT_YELLOW(HSSFColor.LIGHT_YELLOW.index),
	LIGHT_GREEN(HSSFColor.LIGHT_GREEN.index),
	LIGHT_TURQUOISE(HSSFColor.LIGHT_TURQUOISE.index),
	PALE_BLUE(HSSFColor.PALE_BLUE.index),
	LAVENDER(HSSFColor.LAVENDER.index),
	WHITE(HSSFColor.WHITE.index),
	CORNFLOWER_BLUE(HSSFColor.CORNFLOWER_BLUE.index),
	LEMON_CHIFFON(HSSFColor.LEMON_CHIFFON.index),
	MAROON(HSSFColor.MAROON.index),
	ORCHID(HSSFColor.ORCHID.index),
	CORAL(HSSFColor.CORAL.index),
	ROYAL_BLUE(HSSFColor.ROYAL_BLUE.index),
	LIGHT_CORNFLOWER_BLUE(HSSFColor.LIGHT_CORNFLOWER_BLUE.index),
	TAN(HSSFColor.TAN.index);
	
	private final short index;
	
	ExcelColor(short index) {
		this.index = index;
	}

	public short getIndex() {
		return index;
	}
}
