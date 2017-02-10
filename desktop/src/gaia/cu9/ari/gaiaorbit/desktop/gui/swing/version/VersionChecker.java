package gaia.cu9.ari.gaiaorbit.desktop.gui.swing.version;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.badlogic.gdx.utils.JsonReader;

import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.callback.Runnable;

public class VersionChecker implements Runnable {
    private static final int VERSIONCHECK_TIMEOUT_MS = 5000;
    private String stringUrl;

    public VersionChecker(String stringUrl) {
        this.stringUrl = stringUrl;
    }

    @Override
    public Object run() {
        Object result = null;
        try {
            URL url = new URL(stringUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(VERSIONCHECK_TIMEOUT_MS);
            con.setReadTimeout(VERSIONCHECK_TIMEOUT_MS);
            InputStream is = con.getInputStream();
            /* Now read the retrieved document from the stream. */
            JsonReader reader = new JsonReader();
            result = reader.parse(is);
            is.close();

        } catch (MalformedURLException e) {
            result = e.getLocalizedMessage();
        } catch (IOException e) {
            result = e.getLocalizedMessage();
        }
        return result;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
