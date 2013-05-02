package com.sanxing.sesame.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
	private static SimpleDateFormat dateForamt = new SimpleDateFormat(
			"yyyyMMdd");

	private static SimpleDateFormat timeForamt = new SimpleDateFormat("HHmmss");

	public static String parseDate(Date date) {
		return dateForamt.format(date);
	}

	public static Date strToDate(String sDate) {
		try {
			return dateForamt.parse(sDate);
		} catch (ParseException e) {
		}
		return null;
	}

	public static String getCurrentDate() {
		return dateForamt.format(new Date());
	}

	public static String getCurrentTime() {
		return timeForamt.format(new Date());
	}

	public static int compare(String date1, String date2) {
		int iYear = Integer.parseInt(date1.substring(0, 4));
		int iMonth = Integer.parseInt(date1.substring(4, 6)) - 1;
		int iDay = Integer.parseInt(date1.substring(6, 8));
		GregorianCalendar ca1 = new GregorianCalendar(iYear, iMonth, iDay);

		iYear = Integer.parseInt(date2.substring(0, 4));
		iMonth = Integer.parseInt(date2.substring(4, 6)) - 1;
		iDay = Integer.parseInt(date2.substring(6, 8));
		GregorianCalendar ca2 = new GregorianCalendar(iYear, iMonth, iDay);
		return ca1.compareTo(ca2);
	}

	public static int getdaysbetween(String startDate, String endDate) {
		int iYear = Integer.parseInt(startDate.substring(0, 4));
		int iMonth = Integer.parseInt(startDate.substring(4, 6)) - 1;
		int iDay = Integer.parseInt(startDate.substring(6, 8));
		GregorianCalendar ca1 = new GregorianCalendar(iYear, iMonth, iDay);

		iYear = Integer.parseInt(endDate.substring(0, 4));
		iMonth = Integer.parseInt(endDate.substring(4, 6)) - 1;
		iDay = Integer.parseInt(endDate.substring(6, 8));
		GregorianCalendar ca2 = new GregorianCalendar(iYear, iMonth, iDay);

		int year1 = ca1.get(1);
		int year2 = ca2.get(1);

		int dayofYear1 = ca1.get(6);
		int dayofYear2 = ca2.get(6);

		int ip = 0;
		for (int i = year1; i < year2; ++i) {
			if (isLeapyear(i)) {
				ip += 366;
			} else {
				ip += 365;
			}
		}

		int temp = ip + dayofYear2 - dayofYear1 + 1;
		return temp;
	}

	public static boolean isLeapyear(int year) {
		boolean isproyear = false;
		if ((((year % 400 == 0) ? 1 : 0) | (((year % 100 != 0) && (year % 4 == 0)) ? 1
				: 0)) != 0)
			isproyear = true;
		else {
			isproyear = false;
		}
		return isproyear;
	}

	public static String getFirstDayOfMonth() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(5, 1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getLastDayOfMonth() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(5, 1);
		calendar.roll(5, -1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getDateFormat(Date date) {
		return dateForamt.format(date);
	}

	public static String getDateFormat(Date date, SimpleDateFormat dateFormat) {
		return dateFormat.format(date);
	}

	public static String getFirstDayOfMonth(Integer year, Integer month) {
		Calendar calendar = new GregorianCalendar();
		if (year == null) {
			year = Integer.valueOf(calendar.get(1));
		}
		if (month == null) {
			month = Integer.valueOf(calendar.get(2));
		}
		calendar.set(year.intValue(), month.intValue(), 1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getLastDayOfMonth(Integer year, Integer month) {
		Calendar calendar = new GregorianCalendar();
		if (year == null) {
			year = Integer.valueOf(calendar.get(1));
		}
		if (month == null) {
			month = Integer.valueOf(calendar.get(2));
		}
		calendar.set(year.intValue(), month.intValue(), 1);
		calendar.roll(5, -1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getLastDayOfMonth(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(5, 1);
		calendar.roll(5, -1);
		return dateForamt.format(calendar.getTime());
	}

	public static boolean isLastDayOfMonth(Date date) {
		String lastTime = getLastDayOfMonth(date);
		String time = dateForamt.format(date);

		return (time.equalsIgnoreCase(lastTime));
	}

	public static String getFirstDayOfYear() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(6, 1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getLastDayOfYear() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(6, 1);
		calendar.roll(6, -1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getFirstDayOfYear(Integer year) {
		Calendar calendar = new GregorianCalendar();
		if (year == null) {
			year = Integer.valueOf(calendar.get(1));
		}
		calendar.set(year.intValue(), 0, 1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getLastDayOfYear(Integer year) {
		Calendar calendar = new GregorianCalendar();
		if (year == null) {
			year = Integer.valueOf(calendar.get(1));
		}
		calendar.set(year.intValue(), 0, 1);
		calendar.roll(6, -1);
		return dateForamt.format(calendar.getTime());
	}

	public static String getLastDayOfYear(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(6, 1);
		calendar.roll(6, -1);
		return dateForamt.format(calendar.getTime());
	}

	public static boolean isLastDayOfYear(Date date) {
		String lastTime = getLastDayOfYear(date);
		String time = dateForamt.format(date);

		return (time.equalsIgnoreCase(lastTime));
	}

	public static Date getNextDay(Date today) {
		Calendar ca = new GregorianCalendar();
		ca.setTime(today);
		ca.add(6, 1);
		return ca.getTime();
	}

	public static Date getLastDay(Date today) {
		Calendar ca = new GregorianCalendar();
		ca.setTime(today);
		ca.add(6, -1);
		return ca.getTime();
	}

	public static int getDayMonth(Date today) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		return (calendar.get(2) + 1);
	}

	public static int getDayYear(Date today) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		return calendar.get(1);
	}
}