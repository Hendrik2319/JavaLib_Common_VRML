package net.schwarzbaer.java.lib.system;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

public class DateTimeFormatter {
	
	private final Calendar cal;

	public DateTimeFormatter() {
		cal = Calendar.getInstance(TimeZone.getTimeZone("CET"), Locale.GERMANY);
	}

	public String getTimeStr(long millis, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		cal.setTimeInMillis(millis);
		return getTimeStr(cal, Locale.ENGLISH, withTextDay, withDate, dateIsLong, withTime, withTimeZone);
	}

	public String getTimeStr(long millis, Locale locale, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		cal.setTimeInMillis(millis);
		return getTimeStr(cal, locale, withTextDay, withDate, dateIsLong, withTime, withTimeZone);
	}

	public String getTimeStr(long millis, Locale locale, String format) {
		cal.setTimeInMillis(millis);
		return String.format(locale, format, cal);
	}

	public static String getTimeStr(Calendar cal, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		return getTimeStr(cal, Locale.ENGLISH, withTextDay, withDate, dateIsLong, withTime, withTimeZone);
	}

	public static String getTimeStr(Calendar cal, Locale locale, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		String format = getFormatStr(withTextDay, withDate, dateIsLong, withTime, withTimeZone);
		return String.format(locale, format, cal);
	}

	public static String getFormatStr(boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		Vector<String> formatParts = new Vector<>(10);
		if (withTextDay) formatParts.add("%1$tA"+getColon(withDate || withTime || withTimeZone));
		if (withDate) {
			if (dateIsLong) {
				formatParts.add("%1$te.");
				formatParts.add("%1$tb" );
				formatParts.add("%1$tY"+getColon(withTime || withTimeZone));
			} else{
				formatParts.add("%1$td.%1$tm.%1$ty"+getColon(withTime || withTimeZone));
			}
		}
		if (withTime) formatParts.add("%1$tT");
		if (withTimeZone) formatParts.add("[%1$tZ:%1$tz]");
		
		String format = String.join(" ", formatParts);
		return format;
	}

	private static String getColon(boolean b) {
		return b ? "," : "";
	}
	
	public  static String getDurationStr_ms(long duration_ms) { return getDurationStr(duration_ms/1000, duration_ms % 1000); }
	public  static String getDurationStr(long duration_sec) { return getDurationStr(duration_sec, null); }
	private static String getDurationStr(long duration_sec, Long duration_ms) {
		long s =  duration_sec      %60;
		long m = (duration_sec/60  )%60;
		long h =  duration_sec/3600;

		String msStr = duration_ms==null ? "" : String.format(".%03d", duration_ms);
		
		if (duration_sec < 60) {
			return String.format("%d%s s", s, msStr);
		}
		
		if (duration_sec < 3600)
			return String.format("%d:%02d%s min", m, s, msStr);
		
		return String.format("%d:%02d:%02d%s h", h, m, s, msStr);
	}

	public long getTimeInMillis(int year, int month, int date, int hourOfDay, int minute, int second) {
		cal.clear();
		cal.set(year, month-1, date, hourOfDay, minute, second);
		return cal.getTimeInMillis();
	}
}
