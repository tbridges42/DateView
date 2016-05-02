package us.bridgeses.dateview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Tony on 4/27/2016.
 *
 * This is a simple extension of {@link TextView} to make displaying and setting dates easier.
 * Use setDate to set the date either in milliseconds or {@link Date}. It will be displayed
 * according to the {@link DateFormat} set either in xml or through setFormat
 */
public class DateView extends TextView {

    private DateFormat format;
    private String noneString;
    private Locale locale;

    /**
     * Xtor
     * @param context The context in which the view is displayed
     * @param attr A set of attributes set through XML:
     *             dateview_date_format_str: If this is set, the format will be a SimpleDateFormat
     *                                       using the specified format. This setting overrides
     *                                       dateview_date_format and dateview_time_format
     *             dateview_date_format: Set how the date portion of the date and time is displayed.
     *                                   The values correspond to the constants in {@link DateFormat}
     *             dateview_time_format: Same as date format, but for the time portion.
     *             dateview_locale: Set the locale using a BCP 47 language tag. Null or invalid
     *                              tags will use the system default.
     *             dateview_none_string: Set the string displayed if the date is null or less than
     *                                   epoch. This is a convenience for date fields that are optional
     */
    public DateView(Context context, AttributeSet attr) {
        super(context, attr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attr,
                R.styleable.DateView, 0, 0);
        // TODO: This is kind of hideous. Clean it up
        try {
            locale = fetchLocale(array);
            // Attempt to get format string from xml
            String formatString = array.getString(R.styleable.DateView_dateview_date_format_str);
            if (formatString != null) {
                // If format string is specified, use it
                format = new SimpleDateFormat(formatString,
                        locale);
            }
            else {
                // If format string is not specified, attempt to get the format from xml
                format = fetchDateFormat(array);
            }
            // Attempt to get none string from xml
            noneString = array.getString(R.styleable.DateView_dateview_none_string);
            if (noneString == null) {
                // If no none string specified in xml, attempt to get from app resources
                noneString = fetchParentResource(context);
            }
            if (noneString == null) {
                // If no none string specified in xml or app resources, use from library resources
                noneString = getResources().getString(R.string.dateview_none);
            }
        }
        finally {
            array.recycle();
        }
    }

    private Locale fetchLocale(TypedArray array) {
        String localeCode = array.getString(R.styleable.DateView_dateview_locale);
        if (localeCode == null) {
            return Locale.getDefault();
        }
        String[] localeCodes = localeCode.split("-");

        switch(localeCodes.length) {
            case 1: {
                return new Locale(localeCodes[0]);
            }
            case 2: {
                return new Locale(localeCodes[0], localeCodes[1]);
            }
            case 3: {
                return new Locale(localeCodes[0], localeCodes[1], localeCodes[2]);
            }
        }

        Log.w("DateView", "Invalid locale string. Using default");
        return Locale.getDefault();
    }

    private DateFormat fetchDateFormat(TypedArray array) {
        int dateLength = array.getInt(R.styleable.DateView_dateview_date_format, -1);
        int timeLength = array.getInt(R.styleable.DateView_dateview_time_format, -1);
        if (dateLength < 0 && timeLength < 0) {
            return DateFormat.getDateTimeInstance();
        }
        if (dateLength < 0) {
            return DateFormat.getTimeInstance(timeLength, locale);
        }
        if (timeLength < 0) {
            return DateFormat.getDateInstance(dateLength, locale);
        }
        return DateFormat.getDateTimeInstance(dateLength, timeLength, locale);
    }

    private String fetchParentResource(Context context) {
        try {
            int id = context.getResources()
                    .getIdentifier("none", "string", context.getPackageName());
            if (id != 0) {
                return context.getResources().getString(id);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFormat(DateFormat format) {
        try {
            Date date;
            date = format.parse(getText().toString());
            this.format = format;
            setDate(date);
        }
        catch (ParseException e) {
            this.format = format;
        }
    }

    public void setFormat(String formatString) {
        try {
            Date date;
            date = format.parse(getText().toString());
            this.format = new SimpleDateFormat(formatString, locale);
            setDate(date);
        }
        catch (ParseException e) {
            this.format = new SimpleDateFormat(formatString, locale);
        }
    }

    public void setDate(long millis) {
        if (millis <= 0) {
            setText(noneString);
        }
        else {
            Date date = new Date(millis);
            setDate(date);
        }
    }

    public void setDate(Date time) {
        if (time == null) {
            setText(noneString);
        }
        else {
            setText(format.format(time));
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public String getNoneString() {
        return noneString;
    }

    public DateFormat getFormat() {
        return format;
    }
}
