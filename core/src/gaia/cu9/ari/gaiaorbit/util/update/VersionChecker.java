package gaia.cu9.ari.gaiaorbit.util.update;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.JsonReader;

public class VersionChecker implements Runnable {
    private static final int VERSIONCHECK_TIMEOUT_MS = 5000;
    private String stringUrl;
    private EventListener listener;

    public VersionChecker(String stringUrl) {
        this.stringUrl = stringUrl;
    }

    @Override
    public void run() {

        HttpRequest request = new HttpRequest(HttpMethods.GET);
        request.setUrl(stringUrl);
        request.setTimeOut(VERSIONCHECK_TIMEOUT_MS);

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            public void handleHttpResponse(HttpResponse httpResponse) {
                JsonReader reader = new JsonReader();
                Object result = reader.parse(httpResponse.getResultAsStream());
                listener.handle(new VersionCheckEvent(result));
            }

            public void failed(Throwable t) {
                listener.handle(new VersionCheckEvent(true));
            }

            @Override
            public void cancelled() {
                listener.handle(new VersionCheckEvent(true));
            }
        });

    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

}
