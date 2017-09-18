package gaia.cu9.ari.gaiaorbit.client.format;

import java.util.Date;
import java.util.Locale;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;

public class GwtDateFormat implements IDateFormat {
    private DateTimeFormat df;

    public GwtDateFormat(String pattern) {
        df = DateTimeFormat.getFormat(pattern);
    }

    public GwtDateFormat(Locale loc, boolean date, boolean time) {
        assert date || time : "Formatter must include date or time";
        if (date && !time)
            df = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
        else if (!date && time)
            df = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
        else if (date && time)
            df = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
    }

    @Override
    public String format(Date date) {
        return df.format(date);
    }

    @Override
    public Date parse(String date) {
        return df.parse(date);
    }

}
