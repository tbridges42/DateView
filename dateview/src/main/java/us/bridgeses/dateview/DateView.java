package us.bridgeses.dateview;

import android.content.Context;
import android.content.res.TypedArray;
import android.nfc.FormatException;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.concurrent.Exchanger;


/**
 * Created by Tony on 4/27/2016.
 */
public class DateView extends TextView {

    private DateFormat format;
    private String noneString;
    private Locale locale = Locale.US;

    public DateView(Context context, AttributeSet attr) {
        super(context, attr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attr,
                R.styleable.DateView, 0, 0);
        // TODO: This is kind of hideous. Clean it up
        try {
            locale = getLocale(array);
            // Attempt to get format string from xml
            String formatString = array.getString(R.styleable.DateView_dateview_date_format_str);
            if (formatString != null) {
                // If format string is specified, use it
                format = new SimpleDateFormat(formatString,
                        locale);
            }
            else {
                // If format string is not specified, attempt to get the format from xml
                format = getDateFormat(array);
            }
            // Attempt to get none string from xml
            noneString = array.getString(R.styleable.DateView_dateview_none_string);
            if (noneString == null) {
                // If no none string specified in xml, attempt to get from app resources
                noneString = getParentResource(context);
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

    private Locale getLocale(TypedArray array) {
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

    private DateFormat getDateFormat(TypedArray array) {
        int dateLength = array.getInt(R.styleable.DateView_dateview_date_format, -1);
        int timeLength = array.getInt(R.styleable.DateView_dateview_time_format, -1);
        if (dateLength < 0 && timeLength < 0) {
            return DateFormat.getDateTimeInstance();
        }
        if (dateLength < 0) {
            return DateFormat.getTimeInstance(timeLength, getLocale(array));
        }
        if (timeLength < 0) {
            return DateFormat.getDateInstance(dateLength, getLocale(array));
        }
        return DateFormat.getDateTimeInstance(dateLength, timeLength, getLocale(array));
    }

    private String getParentResource(Context context) {
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
        this.format = format;
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
        setText(format.format(time));
    }
}
