package gaia.cu9.ari.gaiaorbit.desktop.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;

public class DesktopDateFormat implements IDateFormat {
    private DateFormat df;

    public DesktopDateFormat(String pattern) {
        df = new SimpleDateFormat(pattern);
    }

    public DesktopDateFormat(Locale loc, boolean date, boolean time) {
        assert date || time : "Formatter must include date or time";
        if (date && !time)
            df = DateFormat.getDateInstance(DateFormat.MEDIUM, loc);
        else if (!date && time)
            df = DateFormat.getTimeInstance(DateFormat.MEDIUM, loc);
        else if (date && time)
            df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, loc);
    }

    @Override
    public String format(Date date) {
        return df.format(date);
    }

    @Override
    public Date parse(String date) {
        try {
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
