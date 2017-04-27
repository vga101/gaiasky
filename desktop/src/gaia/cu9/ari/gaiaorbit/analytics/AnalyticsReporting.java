package gaia.cu9.ari.gaiaorbit.analytics;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;
import com.brsanthu.googleanalytics.PageViewHit;

public class AnalyticsReporting {

    public static Future<GoogleAnalyticsResponse> report() {
        GoogleAnalytics ga = new GoogleAnalytics("UA-57580474-6");
        return ga.postAsync(new PageViewHit("http://gaiasky.com", "gaiasky"));
    }

    public static void main(String[] args) {
        Future<GoogleAnalyticsResponse> f = report();
        try {
            GoogleAnalyticsResponse gar = f.get(15, TimeUnit.SECONDS);

            System.out.println("Response status code: " + gar.getStatusCode());
            List<NameValuePair> result = gar.getPostedParms();
            if (result != null)
                for (NameValuePair pair : result) {
                    System.out.println(pair.toString());
                }
            else
                System.out.println("No params");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
