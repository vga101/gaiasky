package gaia.cu9.ari.gaiaorbit.analytics;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.PageViewHit;

public class AnalyticsReporting {

    public static void report() {
        GoogleAnalytics ga = new GoogleAnalytics("UA-57580474-5");
        ga.postAsync(new PageViewHit("https://gaiasky.com", "Gaia Sky launch"));
    }

    public static void main(String[] args) {
        report();
    }
}
