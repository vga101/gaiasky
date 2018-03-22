package gaia.cu9.ari.gaiaorbit.util.format;

import java.time.Instant;

public interface IDateFormat {
    public String format(Instant date);

    public Instant parse(String date);
}
