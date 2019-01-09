package com.compiler.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.compiler.constants.Constants;

public class DateUtil {
	public static Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

	public static Date getPreviousDayDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getCurrentDate());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	public static Date getTwoWeeksAgo() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getCurrentDate());
		cal.add(Calendar.DAY_OF_MONTH, -15);
		return cal.getTime();
	}

	public static Date getNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return cal.getTime();
	}

	public static Date fromStrToDate(String time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static Date fromStrToLastSecondOfDay(String time) {
		Calendar cal = Calendar.getInstance();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			cal.setTime(format.parse(time));
			cal.set(Calendar.HOUR_OF_DAY, Constants.TWENTY_THREE);
			cal.set(Calendar.MINUTE, Constants.FIFTY_NINE);
			cal.set(Calendar.SECOND, Constants.FIFTY_NINE);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cal.getTime();
	}

	public static Date getConcatOfDateAndTime(Date date, String time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String[] split = time.split(":");
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
		cal.set(Calendar.MINUTE, Integer.parseInt(split[1]));
		cal.set(Calendar.SECOND, Constants.ZERO);
		return cal.getTime();
	}

	public static boolean isDateBeforeThanCurrentDate(Date date) {
		return date.before(getCurrentDate());
	}

	public static boolean isFirstDateBeforeThanSecondDate(Date date, Date date2) {
		return date.before(date2);
	}

	public static String getISOformat(Date date) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String nowAsISO = df.format(date);
		return nowAsISO;
	}
	
	public static String getISOformatWithoutTime(Date date) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String nowAsISO = df.format(date);
		return nowAsISO;
	}
}
