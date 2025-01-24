package macro.hd.wallpapers.Utilily;

import android.content.res.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * GMT (UTC) Time Converter.
 * 
 * E.g.: GMTTime = "2009-10-09T00:37:22Z";
 * 
 */

/**
 * @author piyush.agarwal
 * 
 */
public class DateTimeUtils {
    
    public final static String FILE_NAME_FORMAT = "yyyy_MM_dd";
    public static final String TIME_ZONE = "GMT";
    public static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    public static final int DAYS_PER_WEEK = 7;
    public static final int DAYS_PER_MONTH = 30;
    public static final String DATE_FORMAT_DD_MM_YYYY = "dd.MM.yyyy";
    public static final String SERVER_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";  //2016-08-16T12:22:59+05:30
    public static final String SERVER_TIME_FORMAT2 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String SERVER_TIME_FORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'+'SS:SS";
    public static final String PURCHASE_HISTORY_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT_hh_mm = "hh:mm";
    public static final String DATE_FORMAT_DD_MM_YY2 = "dd MMM, yyyy";
    
    /**
     * Method to get the formatted time
     * 
     * @param millis
     *            the time in millisecods which is to be formatted.
     * @param format
     *            the format
     * @return the formatted time.
     */
    public static String getFormattedTime(long millis, String format ) {
        String time = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( format, Locale.getDefault( ) );
        time = simpleDateFormat.format( new Date( millis ) );
        return time;
    }
    
    /**
     * method to convert the string to appropriate date or time
     * 
     * @param param
     *            Input Date
     * @param inputFormat
     *            Format of input date
     * @param outputFormat
     *            Format of Output or resulted date
     * @return
     */
    public static String convertFromStringToDate(String param, String inputFormat,
                                                 String outputFormat ) {
        String toSet = null;
        try {
            long timeInMillis = convertDate( inputFormat, param );
            toSet = DateTimeUtils.getFormattedTime( timeInMillis, outputFormat );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        return toSet;
        
    }
    
    /**
     * Method used to return output date from current date + no. of days in outputFormat
     * 
     * @param outputFormat
     * @param days
     *            No. of days
     * @return String OutputFormat
     */
    public static String getFormatedDateFromCurrentDate(String outputFormat, int days ) {
        Calendar calender = getCalendar( );
        calender.add( Calendar.DATE, days );
        String formatedDate = getFormattedTime( calender.getTimeInMillis( ), outputFormat );
        return formatedDate;
    }
    
    /**
     * Get Current Calendar Object
     * 
     * @return Calendar
     */
    public static Calendar getCalendar( ) {
        Configuration userConfig = new Configuration( );
        Calendar cal;
        if ( userConfig.locale != null ) {
            cal = Calendar.getInstance( userConfig.locale );
        } else {
            cal = Calendar.getInstance( TimeZone.getDefault( ) );
        }
        return cal;
    }
    
    /**
     * Get Difference between two Timestamp given in millis
     * 
     * @param startTimestamp
     *            Given in Millis
     * @param endTimestamp
     *            Given in Millis
     * @return
     */
    public static int getDaysDifference( long startTimestamp, long endTimestamp ) {
        long diff = endTimestamp - startTimestamp;
        int days = -1;
        if ( diff > 0 ) {
            days = (int) ( diff / MILLIS_PER_DAY );
        }
        return days;
    }
    


    public static int getMinuteFromRawDate(String dateStr) throws DateFormatException {
        if ( dateStr == null ) {
            throw new DateFormatException("Error while fetching minute. Date is null.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( AppConstant.START_DATE_FORMAT );
        try {
            Date date = dateFormat.parse( dateStr );
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            return calendar.get( Calendar.MINUTE );
        } catch ( ParseException e ) {
            throw new DateFormatException(e);
        }

    }

    public static class DateFormatException extends Exception {

        public DateFormatException(String message) {
            super(message);
        }

        public DateFormatException(Throwable throwable) {
            super(throwable);
        }
    }


//    public static String convertDateToPurchase(String serverdate){
//        long datemiles = convertDate();
//    }
//    getFormattedTime

    public static int getDayFromRawDateServer(String dateStr) throws DateFormatException {
        if ( dateStr == null ) {
            throw new DateFormatException("Error while fetching day. Date is null.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( SERVER_TIME_FORMAT/*AppConstant.START_DATE_FORMAT*/ );
        try {
            Date date = dateFormat.parse( dateStr );
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            return  calendar.get( Calendar.DAY_OF_MONTH );
        } catch ( ParseException e ) {
            throw new DateFormatException(e);
        }
    }

    /**
     * Method to convert in time in millis
     *
     * @param inputFormat
     *            Input format of the given time
     * @param dateString
     *            Input Date
     * @return
     */
    public static long convertDate(String inputFormat, String dateString ) {
        long timeInMillis = -1;
        SimpleDateFormat format = new SimpleDateFormat( inputFormat, Locale.getDefault( ) );
        try {
            Date date = null;
            try {
                date = format.parse( dateString );
            } catch ( ParseException ex ) {
                format = new SimpleDateFormat( SERVER_TIME_FORMAT2 );
                date = format.parse( dateString );
            }
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            timeInMillis = calendar.getTimeInMillis( );
        } catch ( Exception e ) {

        }
        return timeInMillis;
    }


    public static int getDayFromRawDate(String dateStr) throws DateFormatException {
        if ( dateStr == null ) {
            throw new DateFormatException("Error while fetching day. Date is null.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( AppConstant.START_DATE_FORMAT );
        try {
            Date date = dateFormat.parse( dateStr );
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            return  calendar.get( Calendar.DAY_OF_MONTH );
        } catch ( ParseException e ) {
            throw new DateFormatException(e);
        }
    }

    public static int getMonthFromRawDate(String dateStr) throws DateFormatException  {
        if ( dateStr == null ) {
            throw new DateFormatException("Error while fetching month. Date is null.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( AppConstant.START_DATE_FORMAT );
        try {
            Date date = dateFormat.parse( dateStr );
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            return calendar.get( Calendar.MONTH );
        } catch ( ParseException e ) {
            throw new DateFormatException(e);
        }
    }

    public static int getYearFromRawDate(String dateStr) throws DateFormatException {
        if ( dateStr == null ) {
            throw new DateFormatException("Error while fetching year. Date is null.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( AppConstant.START_DATE_FORMAT );
        try {
            Date date = dateFormat.parse( dateStr );
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            return calendar.get( Calendar.YEAR );
        } catch ( ParseException e ) {
            throw new DateFormatException(e);
        }

    }

    public static int getHourFromRawDate(String dateStr) throws DateFormatException {
        if ( dateStr == null ) {
            throw new DateFormatException("Error while fetching hour. Date is null.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( AppConstant.START_DATE_FORMAT );
        try {
            Date date = dateFormat.parse( dateStr );
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( date );
            return calendar.get( Calendar.HOUR_OF_DAY );
        } catch ( ParseException e ) {
            throw new DateFormatException(e);
        }

    }
}
