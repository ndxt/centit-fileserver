package com.centit.test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.centit.support.algorithm.DatetimeOpt;

public class TestTimeComm {
	 //获得当前的时间戳
	  /**
	   * Gets the pre time stamp.
	   * 
	   * @param preStr the pre str
	   * 
	   * @return the pre time stamp
	   */
	  public static synchronized String getPreTimeStamp(String preStr)
	  {
	    String timeStr = "";
	    Calendar cal = Calendar.getInstance();
	    String str = Integer.toString(cal.get(1));
	    int month = 1 + cal.get(2);
	    int day = cal.get(5);
	    int hour = cal.get(11);
	    int minute = cal.get(12);
	    int second = cal.get(13);
	    int milliSecond = cal.get(14);
	    if (month < 10)
	      timeStr = timeStr + "0" + month;
	    else {
	      timeStr = timeStr + month;
	    }
	    if (day < 10)
	      timeStr = timeStr + "0" + day;
	    else
	      timeStr = timeStr + day;
	    if (hour < 10)
	      timeStr = timeStr + "0" + hour;
	    else
	      timeStr = timeStr + hour;
	    if (minute < 10)
	      timeStr = timeStr + "0" + minute;
	    else
	      timeStr = timeStr + minute;
	    if (second < 10)
	      timeStr = timeStr + "0" + second;
	    else
	      timeStr = timeStr + second;
	    if (milliSecond < 100) {
	      if (milliSecond >= 10)
	        timeStr = timeStr + "0" + milliSecond;
	      else
	        timeStr = timeStr + "00" + milliSecond;
	    }
	    else
	      timeStr = timeStr + milliSecond;
	    timeStr = "" + str + timeStr;
	    if (preStr != null)
	      timeStr = preStr.trim() + timeStr;
	    return timeStr;
	  }
	  
	  //获得以**字符串开头的的时间戳
	  /**
	   * Gets the time stamp.
	   * 
	   * @param unitStr the unit str
	   * 
	   * @return the time stamp
	   */
	  public static synchronized String getTimeStamp(String unitStr)
	  {
	    String timeStr = "";
	    Calendar cal = Calendar.getInstance();
	    String str = Integer.toString(cal.get(1));
	    int month = 1 + cal.get(2);
	    int day = cal.get(5);
	    int hour = cal.get(11);
	    int minute = cal.get(12);
	    int second = cal.get(13);
	    int milliSecond = cal.get(14);
	    if (month < 10)
	      timeStr = timeStr + "0" + month;
	    else {
	      timeStr = timeStr + month;
	    }
	    if (day < 10)
	      timeStr = timeStr + "0" + day;
	    else
	      timeStr = timeStr + day;
	    if (hour < 10)
	      timeStr = timeStr + "0" + hour;
	    else
	      timeStr = timeStr + hour;
	    if (minute < 10)
	      timeStr = timeStr + "0" + minute;
	    else
	      timeStr = timeStr + minute;
	    if (second < 10)
	      timeStr = timeStr + "0" + second;
	    else
	      timeStr = timeStr + second;
	    if (milliSecond < 100) {
	      if (milliSecond >= 10)
	        timeStr = timeStr + "0" + milliSecond;
	      else
	        timeStr = timeStr + "00" + milliSecond;
	    }
	    else
	      timeStr = timeStr + milliSecond;
	    timeStr = "" + str + timeStr;
	    if (unitStr != null)
	      timeStr = timeStr + unitStr.trim();
	    return timeStr;
	  }
	  
	  //获得年份
	  /**
	   * Gets the year.
	   * 
	   * @return the year
	   */
	  public static String getYear()
	  {
	    Calendar cal = Calendar.getInstance();
	    return Integer.toString(cal.get(1));
	  }
	  //获得月份
	  /**
	   * Gets the month.
	   * 
	   * @return the month
	   */
	  public static String getMonth()
	  {
	    Calendar cal = Calendar.getInstance();
	    String result = "";
	    int temp = cal.get(2) + 1;
	    result = Integer.toString(temp);

	    if (temp < 10) {
	      result = "0" + result;
	    }

	    return result;
	  }
	//获得日期
	  /**
	 * Gets the day.
	 * 
	 * @return the day
	 */
	public static String getDay()
	  {
	    Calendar cal = Calendar.getInstance();
	    String result = "";
	    int temp = cal.get(5);
	    result = Integer.toString(temp);

	    if (temp < 10) {
	      result = "0" + result;
	    }

	    return result;
	  }
	//获得小时
	  /**
	 * Gets the hour.
	 * 
	 * @return the hour
	 */
	public static String getHour()
	  {
	    Calendar cal = Calendar.getInstance();
	    String result = "";
	    int temp = cal.get(11);
	    result = Integer.toString(temp);
	    if (temp < 10) {
	      result = "0" + result;
	    }
	    return result;
	  }
	//获得分钟
	  /**
	 * Gets the minute.
	 * 
	 * @return the minute
	 */
	public static String getMinute()
	  {
	    Calendar cal = Calendar.getInstance();
	    String result = "";
	    int temp = cal.get(12);
	    result = Integer.toString(temp);
	    if (temp < 10) {
	      result = "0" + result;
	    }
	    return result;
	  }
	//获得秒
	  /**
	 * Gets the second.
	 * 
	 * @return the second
	 */
	public static String getSecond()
	  {
	    Calendar cal = Calendar.getInstance();
	    String result = "";
	    int temp = cal.get(13);
	    result = Integer.toString(temp);
	    if (temp < 10) {
	      result = "0" + result;
	    }
	    return result;
	  }
	//获得分秒
	  /**
	 * Gets the milli second.
	 * 
	 * @return the milli second
	 */
	public static String getMilliSecond()
	  {
	    Calendar cal = Calendar.getInstance();
	    String result = "";
	    int temp = cal.get(14);
	    result = Integer.toString(temp);
	    if ((temp < 100) && (temp >= 10)) {
	      result = "0" + result;
	    }
	    else if (temp < 10) {
	      result = "00" + result;
	    }
	    return result;
	  }
	  //获得星期几
	  /**
	   * Gets the day of week.
	   * 
	   * @return the day of week
	   */
	  public static String getDayOfWeek()
	  {
	    Calendar cal = Calendar.getInstance();
	    return Integer.toString(cal.get(7));
	  }
	  //获得当期日期
	  /**
	   * Gets the date.
	   * 
	   * @return the date
	   */
	  public static java.sql.Date getDate()
	  {
	    String strDate = getYear() + "-" + getMonth() + "-" + getDay();
	    java.sql.Date sqlDate = java.sql.Date.valueOf(strDate);
	    return sqlDate;
	  }
	  //把日期格式化成ORC数据格式
	  /**
	   * Trans date to oracle.
	   * 
	   * @param date the date
	   * 
	   * @return the string
	   */
	  public static String transDateToOracle(String date)
	  {
	    String resultStr = "";
	    if ((date == null) || (date.equals(""))) {
	      return "";
	    }
	    String year = date.substring(0, date.indexOf('-'));
	    date = date.substring(5);
	    String month = 
	      Integer.valueOf(date.substring(0, date.indexOf('-'))).toString();
	    String day = Integer.valueOf(date.substring(date.indexOf('-') + 1)).toString();
	    //if (Comm.getConnDBName().equals("DB2"))
	    //  resultStr = year + "-" + month + "-" + day;
	   // else {
	      resultStr = day + "-" + month + "月" + "-" + year;
	   // }

	    return resultStr;
	  }
	  //获得当前日期
	  /**
	   * Gets the nYR date.
	   * 
	   * @return the nYR date
	   */
	  public static String getNYRDate()
	  {
	    return getYear() + "-" + getMonth() + "-" + getDay();
	  }
	  //获得时分秒
	  /**
	   * Gets the sFM date.
	   * 
	   * @return the sFM date
	   */
	  public static String getSFMDate()
	  {
	    return getHour() + ":" + getMinute() + ":" + getSecond();
	  }

	  /**
	   * Gets the one date.
	   * 
	   * @param strDate the str date
	   * @param distance the distance
	   * 
	   * @return the one date
	   * 
	   * @throws Exception the exception
	   */
	  public static String getOneDate(String strDate, int distance)
	    throws Exception
	  {
	    DateFormat df = DateFormat.getDateInstance();
	    Calendar cal = new GregorianCalendar();

	    StringTokenizer stk = 
	      new StringTokenizer(strDate, "-", false);
	    if (stk.countTokens() != 3) {
	      throw new Exception("不合法的日期字符串！");
	    }
	    int[] date = new int[3];
	    int i = 0;
	    while (stk.hasMoreTokens()) {
	      String tmp = stk.nextToken().trim();
	      date[i] = Integer.parseInt(tmp);
	      i++;
	    }

	    cal.set(date[0], date[1] - 1, date[2], 0, 0, 0);
	    cal.add(5, distance);
	    java.util.Date d = cal.getTime();
	    return df.format(d);
	  }
	  //获得当前时间戳
	  /**
	   * Gets the current time stamp.
	   * 
	   * @return the current time stamp
	   */
	  public static Timestamp getCurrentTimeStamp() {
	    Calendar cal = Calendar.getInstance();
	    //return new Timestamp( cal.getTimeInMillis());
	    int year = cal.get(1);
	    int month = 1 + cal.get(2);
	    int day = cal.get(5);
	    int hour = cal.get(11);
	    int minute = cal.get(12);
	    int second = cal.get(13);
	    int milliSecond = cal.get(14);
	    return Timestamp.valueOf(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + milliSecond);
	  }
	  
	public static void main(String[] args) throws Exception {		
		System.out.println(getOneDate("2016-5-1", 5));
		
		System.out.println(DatetimeOpt.convertDateToString(
				DatetimeOpt.currentUtilDate(),"yyyyMMddHHmmssSSS"));
		System.out.println(DatetimeOpt.convertDateToString(
				DatetimeOpt.currentUtilDate(),"HH:mm:ss"));
		System.out.println(getPreTimeStamp(""));
		System.out.println(transDateToOracle("2016-5-1"));
		System.out.println(getPreTimeStamp(""));
		System.out.println(getTimeStamp("unitStr"));
	}
}
