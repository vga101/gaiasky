package gaia.cu9.ari.gaiaorbit.analytics;

import java.util.concurrent.Future;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;
import com.brsanthu.googleanalytics.PageViewHit;

public class AnalyticsReporting {

    public static Future<GoogleAnalyticsResponse> report() {
        GoogleAnalytics ga = new GoogleAnalytics("UA-57580474-6");
        return ga.postAsync(new PageViewHit("http://gaiasky.com", "gaiasky"));
    }

    public static void main(String[] args) {
        report();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
