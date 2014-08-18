package pl.net.bluesoft.interactivereports.util;

import pl.net.bluesoft.util.lang.DateUtil;

import java.util.Date;

/**
 * User: POlszewski
 * Date: 2014-06-30
 */
public class Dates {
	public Date now() {
		return new Date();
	}

	public Date beginOfMonth() {
		return DateUtil.beginOfMonth(now());
	}
}
