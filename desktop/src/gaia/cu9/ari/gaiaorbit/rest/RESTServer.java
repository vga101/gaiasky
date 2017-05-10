package gaia.cu9.ari.gaiaorbit.rest;

import gaia.cu9.ari.gaiaorbit.script.EventScriptingInterface;
import gaia.cu9.ari.gaiaorbit.script.IScriptingInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.before;
import static spark.Spark.setPort;
import static spark.Spark.path;

import java.util.HashMap;
import java.util.Map;

// JSON output
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * REST API for remote procedure calls
 * @author Volker Gaibler
 *
 * General information and remarks follows:
 *
 * WARNING: only allow and use this in a trusted environment. Incoming
 * commands are *not at all* checked! Remote command execution is generally
 * dangerous. This REST API was developed for an exhibition with an
 * isolated network.
 *
 * Methods from gaia.cu9.ari.gaiaorbit.script.IScriptingInterface can be 
 * called remotely via http to allow remote control.
 *
 * HTTP requests syntax is defined to follow the Java method interface 
 * definition closely.
 *
 * List/vector elements are separated by commas, except for strings where they
 * are given as multiple query parameters with the same name.
 *
 * Return values can be retrieved either as JSON objects (default) or text
 * strings (use the query parameter "returnFormat=text").
 * JSON objects contain more information in form of key-value pairs:
 * "success" indicates whether the API call was executed successful or not
 * "text" can give additional text information
 * "value" contains the return value
 */

/* NOTES & TODO:

- should probably call RESTServer.stop() when shutting down Gaia Sky (gracefully halts the server)
- println should eventually use the default way for Gaia Sky notifications
- return strings and return HTTP status handling: "ok", 200, ....
- urlencoding: python -c "import urllib; print urllib.quote('''$value''')")

Considerations for REST interface:
- GET: request representation of specified source
- HEAD: as get, but only metadata, no body
- OPTIONS: returns methods for specified URL
- POST: put or add entity as subordinate resource
- PUT: store entity under URL. Create if not yet existant.
- DELETE: deletes specified resource
- GET, HEAD, OPTIONS, TRACE are by defined as safe / nullipotent, i.e. having no side-effects, also might be cached
- PUT, DELETE are idempotent, i.e. same exposted state no matter how many times requested
- POST not necessarily idempotent
- PUT, DELETE, OPTIONS, TRACE not cacheable; others are

but: HTML standard for browsers only includes GET and POST
<form method="post" ...>
<input type="hidden" name="_method" value="put" />
...
</form>

<form method="get" action="/search.cgi">
<p><label>Search terms: <input type=search name=q></label></p>
<p><input type=submit></p>
</form>

<form method="post" action="/post-message.cgi">
 <p><label>Message: <input type=text name=m></label></p>
 <p><input type=submit value="Submit message"></p>
</form>


for testing with curl:
  curl "http://localhost:8080/api/displayMessageObject?id=12&message=jo&x=0.5&y=0.3&r=1.&g=0.3&b=0.4&a=0.9&fontSize=12"
  --> this is simple, cut cannot deal with spaces in strings
  
  curl "http://localhost:8080/api/setHeadlineMessage" --data-urlencode "headline=Hallo you" --data-urlencode "level=23"
  --> more effort, but works with spaces

but could also use "--data-ascii" (equivalent to "-d"). Or is there a difference?
When curl reads from a file (@filename), data-ascii strips newlines, while data-urlencode does not.
For variables on command line (with newline actually added (not \n)), works for both.

*/





/**
 * REST Server class to implement the REST API
 * @author Volker Gaibler
 *
 * Implemented with Spark, which launches an embedded jetty.
 * Spark recommends static context.
 *
 *
 * - initialized in desktop/src/gaia/cu9/ari/gaiaorbit/desktop/GaiaSkyDesktop.java
 *   with lazy initialization 
 *   TSS: "EventScriptingInterface needs to pull an asset manager from GaiaSky to load assets.
 *       Since GaiaSky only exists after the actual application has been launched and is running".
 */
public class RESTServer {

    /* Class variables: */

    /**
     * Reference for lazy initialization.
     */
    private static EventScriptingInterface esi = null;

    /**
     * Reference to Logger
     */
    private static final Logger logger = LoggerFactory.getLogger("[RESTServer]");


    /* Methods: */

    /** 
     * Returns an instance of the EventScriptingInterface for the lazy initialization.
     */
    private static IScriptingInterface gs() {
        if (esi == null) {
            logger.info("Lazy initialization of EventScriptingInterface...");
            esi = new EventScriptingInterface();
        }
        return esi;
    }

    /**
     * Helper functions that get query parameters and convert to specific type.
     * They always return arrays, for the sake of generality, 
     * defined by separating commas in the string.
     * To get a single value, append the index [0] explicitly.
     */

    /**
     * Returns an array of boolean values from a query parameter string.
     */
    private static boolean[] booleanValues(spark.Request request, String param) {
        String s = request.queryParams(param);
        if (s == null) {
            throw new RuntimeException("query param " + param + " not present");
        }
        String[] elems = s.split(",");
        boolean[] ret = new boolean[elems.length];
        for (int i = 0; i < elems.length; i++) {
            ret[i] = Boolean.parseBoolean(elems[i]);
        }
        return ret;
    }

    /**
     * Returns an array of double values from a query parameter string.
     */
    private static double[] doubleValues(spark.Request request, String param) {
        String s = request.queryParams(param);
        if (s == null) {
            throw new RuntimeException("query param " + param + " not present");
        }
        String[] elems = s.split(",");
        double[] ret = new double[elems.length];
        for (int i = 0; i < elems.length; i++) {
            ret[i] = Double.parseDouble(elems[i]);
        }
        return ret;
    }

    /**
     * Returns an array of float values from a query parameter string.
     */
    private static float[] floatValues(spark.Request request, String param) {
        String s = request.queryParams(param);
        if (s == null) {
            throw new RuntimeException("query param " + param + " not present");
        }
        String[] elems = s.split(",");
        float[] ret = new float[elems.length];
        for (int i = 0; i < elems.length; i++) {
            ret[i] = Float.parseFloat(elems[i]);
        }
        return ret;
    }

    /**
     * Returns an array of int values from a query parameter string.
     */
    private static int[] intValues(spark.Request request, String param) {
        String s = request.queryParams(param);
        if (s == null) {
            throw new RuntimeException("query param " + param + " not present");
        }
        String[] elems = s.split(",");
        int[] ret = new int[elems.length];
        for (int i = 0; i < elems.length; i++) {
            ret[i] = Integer.parseInt(elems[i]);
        }
        return ret;
    }

    /**
     * Returns an array of long values from a query parameter string.
     */
    private static long[] longValues(spark.Request request, String param) {
        String s = request.queryParams(param);
        if (s == null) {
            throw new RuntimeException("query param " + param + " not present");
        }
        String[] elems = s.split(",");
        long[] ret = new long[elems.length];
        for (int i = 0; i < elems.length; i++) {
            ret[i] = Long.parseLong(elems[i]);
        }
        return ret;
    }

    /**
     * Returns an array of String values from a query parameter string.
     * Instead of splitting by a separator, an array of strings is created by 
     * having multiple query parameters with the same name.
     */
    private static String[] StringValues(spark.Request request, String param) {
//         String s = request.queryParams(param);
//         if (s == null) {
//             throw new RuntimeException("query param " + param + " not present");
//         }
//         String[] ret = s.split(",");
        String[] ret = request.queryParamsValues(param);
        return ret;
    }

    /**
     * Return the return value (data).
     * Can return ASCII string or JSON format through the HTTP response, depending on user wish.
     * Return values are returned under the "value" key.
     */
    private static String retData(spark.Request request, Map<String, Object> ret, boolean success) {

        String format = request.queryParams("returnFormat");
        String retstring;

        // default return format
        if (format == null) {
            format = "json";
        }

        // return value is null for void methods
        ret.putIfAbsent("value", null);

        // set success key
        ret.put("success", success);

        // add text value if not yet present
        if (success == true) {
            ret.putIfAbsent("text", "OK");
        } else {
            ret.putIfAbsent("text", "Failed");
        }

        // format of return value: JSON by default
        if (format.equals("text")) {
            /* plain text */
            Object v = ret.get("value");
            //if (v != null && v.getClass().isArray())
            if (v instanceof float[]) {
                float[] f = (float[]) v;  // explicit cast from Object to primitive array
                retstring = java.util.Arrays.toString(f);
                logger.info("v = " + retstring);
            } else {
                retstring = String.valueOf(v);
                logger.info("v = " + retstring);
            }
        } else {
            /* JSON */
            Gson gson = new GsonBuilder().serializeNulls().create();
            retstring = gson.toJson(ret);
        }
        logger.debug("Returning for {} format: {}.", format, retstring);
        return retstring;
    }

    /**
     * Handles the API call.
     */
    private static String handleApiCall(spark.Request request, spark.Response response, String httpmethod) {
        logger.info("Handling API call via HTTP " + httpmethod + ":");
        logger.info("path param cmd = " + request.params(":cmd") + ", query params q = " + request.queryParams());
        logger.info("Query string: " + request.queryString());

        String cmd = request.params(":cmd");

        // information for return object
        Map<String, Object> ret = new HashMap<String, Object>();

        try {

            /**
            --> Methods from IScriptingInterface (core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java).
            Listed in that order, consult that for the meaning of parameters.
            Some are omitted currently. 
            */

            logger.info("=== Processing API command: {} ===", cmd);
            logger.debug(" debug output ****************");

            /** 
             * preloadTextures.
             * Paths either absolute or relative to android/assets/
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/preloadTextures" -d paths="img/gaiaskylogo.png"
             */
            if (cmd.equals("preloadTextures")) {
                gs().preloadTextures(
                    StringValues(request, "paths")
                );
                return retData(request, ret, true);
            }

            /** 
             * activateRealTimeFrame.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/activateRealTimeFrame"
             */
            if (cmd.equals("activateRealTimeFrame")) {
                gs().activateRealTimeFrame();
                return retData(request, ret, true);
            }

            /** 
             * activateSimulationTimeFrame.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/activateSimulationTimeFrame"
             */
            if (cmd.equals("activateSimulationTimeFrame")) {
                gs().activateSimulationTimeFrame();
                return retData(request, ret, true);
            }

            /**
             * setHeadlineMessage.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setHeadlineMessage" --data-urlencode "headline=I'm alive"'!!!' --data-urlencode "level=23"
             * ['!!!' quoting is for bash to prevent history expansion]
             */
            if (cmd.equals("setHeadlineMessage")) {
                gs().setHeadlineMessage(
                    StringValues(request, "headline")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setSubheadMessage.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setSubheadMessage" --data-urlencode "subhead=Test subhead" --data-urlencode "level=23"
             */
            if (cmd.equals("setSubheadMessage")) {
                gs().setSubheadMessage(
                    StringValues(request, "subhead")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * clearHeadlineMessage.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/clearHeadlineMessage" --data-urlencode "level=23"
             */
            if (cmd.equals("clearHeadlineMessage")) {
                gs().clearHeadlineMessage();
                return retData(request, ret, true);
            }

            /**
             * clearSubheadMessage.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/clearSubheadMessage" --data-urlencode "level=23"
             */
            if (cmd.equals("clearSubheadMessage")) {
                gs().clearSubheadMessage();
                return retData(request, ret, true);
            }

            /**
             * clearAllMessages.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/clearAllMessages" --data-urlencode "level=23"
             */
            if (cmd.equals("clearAllMessages")) {
                gs().clearAllMessages();
                return retData(request, ret, true);
            }

            /**
             * displayMessageObject. For one-line text (no line wrapping). But accepts line breaks.
             * Observation: can call multiple times with same id, then object is updated (but *not* for changes of fontSize).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/displayMessageObject?id=11&x=0.2&y=0.6&r=1.&g=0.3&b=0.4&a=0.9&fontSize=800" -d message="The non-line-broken text. Except
             where explicit."
             */
            if (cmd.equals("displayMessageObject")) {
                gs().displayMessageObject(
                    intValues(request, "id")[0],
                    StringValues(request, "message")[0],
                    floatValues(request, "x")[0],
                    floatValues(request, "y")[0],
                    floatValues(request, "r")[0],
                    floatValues(request, "g")[0],
                    floatValues(request, "b")[0],
                    floatValues(request, "a")[0],
                    floatValues(request, "fontSize")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * displayTextObject. For multi-line text (line wrapping performed). Accepts line breaks.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/displayTextObject?id=12&x=0.5&y=0.3&r=1.&g=0.3&b=0.4&a=0.9&fontSize=800&maxWidth=0.2&maxHeight=0.2" -d text="This is the line wrapped text that will be fitted in...."
             */
            if (cmd.equals("displayTextObject")) {
                gs().displayTextObject(
                    intValues(request, "id")[0],
                    StringValues(request, "text")[0],
                    floatValues(request, "x")[0],
                    floatValues(request, "y")[0],
                    floatValues(request, "maxWidth")[0],
                    floatValues(request, "maxHeight")[0],
                    floatValues(request, "r")[0],
                    floatValues(request, "g")[0],
                    floatValues(request, "b")[0],
                    floatValues(request, "a")[0],
                    floatValues(request, "fontSize")[0]
                );
                logger.info("text=\"" + request.queryParams("text") + "\".");
                return retData(request, ret, true);
            }

            /** displayImageObject. 
             * Better use path relative to Gaia Sky than absolute (not recommended; see interface source.)
             * Paths either absolute or relative to android/assets/
             * WORKS ONLY IN ASYNCHRONOUS MODE.
             * Observation: need to "preloadTextures" before "displayImageObject" works (otherwise GUI halts).
             *
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/preloadTextures" -d paths="img/vr.png" -d paths="img/gaiaskylogo.png"
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/displayImageObject?id=22&x=0.3&y=0.3&r=1&g=1&b=1&a=0.5" -d path="img/gaiaskylogo.png"
             */
            if (cmd.equals("displayImageObject")) {
                if (request.queryParams("r") != null) {
                    // with color specification
                    gs().displayImageObject(
                        intValues(request, "id")[0],
                        StringValues(request, "path")[0],
                        floatValues(request, "x")[0],
                        floatValues(request, "y")[0],
                        floatValues(request, "r")[0],
                        floatValues(request, "g")[0],
                        floatValues(request, "b")[0],
                        floatValues(request, "a")[0]
                    );
                    return retData(request, ret, true);
                } else {
                    // without color specification
                    gs().displayImageObject(
                        intValues(request, "id")[0],
                        StringValues(request, "path")[0],
                        floatValues(request, "x")[0],
                        floatValues(request, "y")[0]
                    );
                    return retData(request, ret, true);
                }
            }
 
            /**
             * removeAllObjects.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/removeAllObjects" -d dummypara=useless
             */
            if (cmd.equals("removeAllObjects")) {
                gs().removeAllObjects();
                return retData(request, ret, true);
            }

            /**
             * removeObject.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/removeObject?id=11"
             */
            if (cmd.equals("removeObject")) {
                gs().removeObject(
                    intValues(request, "id")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * removeObjects.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/removeObjects?ids=11,12"
             */
            if (cmd.equals("removeObjects")) {
                gs().removeObjects(
                    intValues(request, "ids")
                );
                return retData(request, ret, true);
            }

            /**
             * disableInput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/disableInput"
             */
            if (cmd.equals("disableInput")) {
                gs().disableInput();
                return retData(request, ret, true);
            }

            /**
             * enableInput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/enableInput"
             */
            if (cmd.equals("enableInput")) {
                gs().enableInput();
                return retData(request, ret, true);
            }

            /**
             * setCameraFocus.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFocus?focusName=Mars"
             */
            if (cmd.equals("setCameraFocus")) {
                gs().setCameraFocus(
                    StringValues(request, "focusName")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setCameraLock.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraLock?lock=true"
             */
            // TODO: the test above is successful, but then causes Exception in thread "LWJGL Application"
            if (cmd.equals("setCameraLock")) {
                gs().setCameraLock(
                    booleanValues(request, "lock")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setCameraFree.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFree"
             */
            if (cmd.equals("setCameraFree")) {
                gs().setCameraFree();
                return retData(request, ret, true);
            }

            /**
             * setCameraFov1.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFov1"
             */
            if (cmd.equals("setCameraFov1")) {
                gs().setCameraFov1();
                return retData(request, ret, true);
            }

            /**
             * setCameraFov2.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFov2"
             */
            if (cmd.equals("setCameraFov2")) {
                gs().setCameraFov2();
                return retData(request, ret, true);
            }

            /**
             * setCameraFov1and2.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFov1and2"
             */
            if (cmd.equals("setCameraFov1and2")) {
                gs().setCameraFov1and2();
                return retData(request, ret, true);
            }

            /**
             * setCameraPosition.
             * Observation: seem to be in millon km, not as in interface docs.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraPosition?vec=3e0,0.,0."
             */
            if (cmd.equals("setCameraPostion") || cmd.equals("setCameraPosition") ) {
                gs().setCameraPostion( // sic!
                    doubleValues(request, "vec")
                );
                return retData(request, ret, true);
            }

            /**
             * setCameraDirection.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraDirection?dir=1.,0.,1."
             */
            if (cmd.equals("setCameraDirection")) {
                gs().setCameraDirection(
                    doubleValues(request, "dir")
                );
                return retData(request, ret, true);
            }

            /**
             * setCameraUp.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraUp?up=1.,0.,1."
             */
            if (cmd.equals("setCameraUp")) {
                gs().setCameraUp(
                    doubleValues(request, "up")
                );
                return retData(request, ret, true);
            }

            /**
             * setCameraSpeed.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraSpeed?speed=10."
             */
            if (cmd.equals("setCameraSpeed")) {
                gs().setCameraSpeed(
                    floatValues(request, "speed")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setRotationCameraSpeed.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setRotationCameraSpeed?speed=10."
             */
            if (cmd.equals("setRotationCameraSpeed")) {
                gs().setRotationCameraSpeed(
                    floatValues(request, "speed")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setTurningCameraSpeed.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setTurningCameraSpeed?speed=10."
             */
            if (cmd.equals("setTurningCameraSpeed")) {
                gs().setTurningCameraSpeed(
                    floatValues(request, "speed")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * cameraForward.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraForward?value=0.5"
             */
            if (cmd.equals("cameraForward")) {
                gs().cameraForward(
                    doubleValues(request, "value")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * cameraRotate.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraRotate?deltaX=0.5&deltaY=-0.5"
             */
            if (cmd.equals("cameraRotate")) {
                gs().cameraRotate(
                    doubleValues(request, "deltaX")[0],
                    doubleValues(request, "deltaY")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * cameraRoll.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraRoll?roll=0.2"
             */
            if (cmd.equals("cameraRoll")) {
                gs().cameraRoll(
                    doubleValues(request, "roll")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * cameraTurn.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraTurn?deltaX=0.2&deltaY=-0.7"
             */
            if (cmd.equals("cameraTurn")) {
                gs().cameraTurn(
                    doubleValues(request, "deltaX")[0],
                    doubleValues(request, "deltaY")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * cameraStop.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraStop"
             */
            if (cmd.equals("cameraStop")) {
                gs().cameraStop();
                return retData(request, ret, true);
            }

            /**
             * cameraCenter.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraCenter"
             */
            if (cmd.equals("cameraCenter")) {
                gs().cameraCenter();
                return retData(request, ret, true);
            }

            /**
             * setFov.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setFov?newFov=30."
             */
            if (cmd.equals("setFov")) {
                gs().setFov(
                    floatValues(request, "newFov")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setVisibility. ---> TODO: currently visible=true toggles instead of setting true!
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setVisibility?name=Stars&visible=true"
             */
            if (cmd.equals("setVisibility")) {
                gs().setVisibility(
                    StringValues(request, "name")[0],
                    booleanValues(request, "visible")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setAmbientLight.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setAmbientLight?value=20."
             */
            if (cmd.equals("setAmbientLight")) {
                gs().setAmbientLight(
                    floatValues(request, "value")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setSimulationTime.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setSimulationTime?time=3827292834729834"
             */
            if (cmd.equals("setSimulationTime")) {
                gs().setSimulationTime(
                    longValues(request, "time")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * startSimulationTime.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/startSimulationTime"
             */
            if (cmd.equals("startSimulationTime")) {
                gs().startSimulationTime();
                return retData(request, ret, true);
            }

            /**
             * stopSimulationTime.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/stopSimulationTime"
             */
            if (cmd.equals("stopSimulationTime")) {
               gs().stopSimulationTime();
               return retData(request, ret, true);
            }

            /**
             * setSimulationPace.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setSimulationPace?pace=2."
             */
            if (cmd.equals("setSimulationPace")) {
                gs().setSimulationPace(
                    doubleValues(request, "pace")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * setStarBrightness.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setStarBrightness?brightness=30."
             */
            if (cmd.equals("setStarBrightness")) {
                gs().setStarBrightness(
                    floatValues(request, "brightness")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * configureRenderOutput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/configureRenderOutput?width=400&height=300&fps=20&folder=.&namePrefix=prefix"
             */
            if (cmd.equals("configureRenderOutput")) {
                gs().configureRenderOutput(
                    intValues(request, "width")[0],
                    intValues(request, "height")[0],
                    intValues(request, "fps")[0],
                    StringValues(request, "folder")[0],
                    StringValues(request, "namePrefix")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * isRenderOutputActive. Returns True/False.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/isRenderOutputActive"
             */
            if (cmd.equals("isRenderOutputActive")) {
                boolean active = gs().isRenderOutputActive();
                ret.put("value", active);
                return retData(request, ret, true);
            }

            /**
             * getRenderOutputFps. Returns FPS setting (int).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getRenderOutputFps"
             */
            if (cmd.equals("getRenderOutputFps")) {
                int fps = gs().getRenderOutputFps();
                ret.put("value", fps);
                return retData(request, ret, true);
            }

            /**
             * setFrameOutput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setFrameOutput?active=true"
             */
            if (cmd.equals("setFrameOutput")) {
                gs().setFrameOutput(
                    booleanValues(request, "active")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * goToObject.
             * ONLY WORKS IN ASYNCHRONOUS MODE.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/goToObject?name=Jupiter&distance=20.&focusWait=3."
             */
            if (cmd.equals("goToObject")) {
                //
                // three method calls possible:
                //
                if (request.queryParams("distance") != null) {
                    if (request.queryParams("focusWait") != null) {
                        // three argument call
                        gs().goToObject(
                            StringValues(request, "name")[0],
                            doubleValues(request, "distance")[0],
                            floatValues(request, "focusWait")[0]
                        );
                    } else {
                        // two argument call
                        gs().goToObject(
                            StringValues(request, "name")[0],
                            doubleValues(request, "distance")[0]
                        );
                    }
                } else {
                    // one argument call
                    gs().goToObject(
                            StringValues(request, "name")[0]
                    );
                }
                return retData(request, ret, true);
            }

            /**
             * getDistanceTo. Returns distance in km (double).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getDistanceTo?objectName=Uranus"
             */
            if (cmd.equals("getDistanceTo")) {
                double dist = gs().getDistanceTo(
                    StringValues(request, "objectName")[0]
                );
                ret.put("value", dist);
                return retData(request, ret, true);
            }

            /**
             * setGuiScrollPosition.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setGuiScrollPosition?pixelY=10."
             */
            if (cmd.equals("setGuiScrollPosition")) {
                gs().setGuiScrollPosition(
                    floatValues(request, "pixelY")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * maximizeInterfaceWindow.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/maximizeInterfaceWindow"
             */
            if (cmd.equals("maximizeInterfaceWindow")) {
                gs().maximizeInterfaceWindow();
                return retData(request, ret, true);
            }

            /**
             * minimizeInterfaceWindow.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/minimizeInterfaceWindow"
             */
            if (cmd.equals("minimizeInterfaceWindow")) {
                gs().minimizeInterfaceWindow();
                return retData(request, ret, true);
            }

            /**
             * setGuiPosition.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setGuiPosition?x=20.&y=10."
             */
            if (cmd.equals("setGuiPosition")) {
                gs().setGuiPosition(
                    floatValues(request, "x")[0],
                    floatValues(request, "y")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * waitForInput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/waitForInput"
             */
            if (cmd.equals("waitForInput")) {
                gs().waitForInput();
                return retData(request, ret, true);
            }

            /**
             * waitForEnter.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/waitForEnter"
             */
            if (cmd.equals("waitForEnter")) {
                gs().waitForEnter();
                return retData(request, ret, true);
            }

            /**
             * getScreenWidth. Returns screen width (int).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getScreenWidth"
             */
            if (cmd.equals("getScreenWidth")) {
                int width = gs().getScreenWidth();
                ret.put("value", width);
                return retData(request, ret, true);
            }

            /**
             * getScreenHeight. Returns screen height (int).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getScreenHeight"
             */
            if (cmd.equals("getScreenHeight")) {
                int height = gs().getScreenHeight();
                ret.put("value", height);
                return retData(request, ret, true);
            }

            /**
             * getPositionAndSizeGui. Returns a size and position of requested GUI element (4 floats: 2 position, 2 size).
             * WORKS ONLY IN ASYNCHRONOUS MODE.
             * examples of "name" are in android/assets/scripts/tutorial/tutorial.py
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getPositionAndSizeGui" -d name="ambient light"
             */
            if (cmd.equals("getPositionAndSizeGui")) {
                float[] pas = gs().getPositionAndSizeGui(
                    StringValues(request, "name")[0]
                );
                ret.put("value", pas);
                return retData(request, ret, true);
            }

            /**
             * getVersionNumber. Returns version number (String).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getVersionNumber"
             */
            if (cmd.equals("getVersionNumber")) {
                String version = gs().getVersionNumber();
                ret.put("value", version);
                return retData(request, ret, true);
            }

            /**
             * waitFocus. Blocks until the focus is the object indicated by the name
             * (e.g. by parallel running requests or user input).
             * There is an optional time out to return earlier.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/waitFocus?name=Uranus&timeoutMs=-1"
             */
            // TODO: not clear how / whether works.
            if (cmd.equals("waitFocus")) {
                boolean wf = gs().waitFocus(
                    StringValues(request, "name")[0],
                    longValues(request, "timeoutMs")[0]
                );
                ret.put("value", wf);
                return retData(request, ret, true);
            }

            /**
             * startRecordingCameraPath.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/startRecordingCameraPath"
             */
            if (cmd.equals("startRecordingCameraPath")) {
                gs().startRecordingCameraPath();
                return retData(request, ret, true);
            }

            /**
             * stopRecordingCameraPath.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/stopRecordingCameraPath"
             */
            if (cmd.equals("stopRecordingCameraPath")) {
                gs().stopRecordingCameraPath();
                return retData(request, ret, true);
            }

            /**
             * runCameraRecording.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/runCameraRecording?path=path/to/file"
             */
            if (cmd.equals("runCameraRecording")) {
                gs().runCameraRecording(
                    StringValues(request, "path")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * sleep.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/sleep?seconds=3."
             */
            if (cmd.equals("sleep")) {
                gs().sleep(
                    floatValues(request, "seconds")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * sleepFrames.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/sleepFrames?frames=30"
             */
            if (cmd.equals("sleepFrames")) {
                gs().sleepFrames(
                    intValues(request, "frames")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * expandGuiComponent.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/expandGuiComponent?name=ObjectsComponent"
             */
            if (cmd.equals("expandGuiComponent")) {
                gs().expandGuiComponent(
                    StringValues(request, "name")[0]
                );
                return retData(request, ret, true);
            }

            /**
             * collapseGuiComponent.
             * components from core/build/classes/main/gaia/cu9/ari/gaiaorbit/interfce/components/
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/collapseGuiComponent?name=ObjectsComponent"
             */
            if (cmd.equals("collapseGuiComponent")) {
                gs().collapseGuiComponent(
                    StringValues(request, "name")[0]
                );
                return retData(request, ret, true);
            }


            /* 
             * -------------------------------------------------
             * if nothing matched so far, API command is invalid
             * -------------------------------------------------
             */
            logger.info("API command unknown: '" + cmd + "'");
            ret.put("text", "API command unknown.");
            return retData(request, ret, false);

        } catch (Exception e) {
            logger.error("Caught an exception: could not process request.");
            e.printStackTrace(System.err);
            ret.put("text", "Exception during API call. Bad syntax?");
            return retData(request, ret, false);
        }

    }


    /**
     * Initialize the REST server.
     *
     * Sets the routes and then passes the call to the handler.
     */
    public static void initialize(Integer port) {

        // check for valid TCP port (otherwise considered as "disabled")
        if (port < 1) {
            return;
        }

        try {
            logger.info("Starting REST API server on port {}", port);
            logger.info("Warning: remote code execution! Only use this functionality in a trusted environment!");
            setPort(port);  // note: deprecated in newer Spark versions in favor of port()
            logger.info("Lazy initialization of EventScriptingInterface.");
            logger.info("Setting routes");

            // now preferred for Spark 2 (with lambda expressions, so only Java 8+):
            // get("/", (request, response) -> {
            //     return "Hey there";
            // });
            //
            // Spark 1 (Java 7):
            // NOTE: routes are matched the order they are defined!

            get("/", new spark.Route() {
                @Override
                public Object handle(spark.Request request, spark.Response response) {
                    return "Welcome to Gaia Sky REST API.";
                    //"\nCheck out this link: <a href=\"http://localhost:8080/api/ask?weather=good\">here</a>";
                }
            });

            get("/api/:cmd", new spark.Route() {
                @Override
                public Object handle(spark.Request request, spark.Response response) {
                    return handleApiCall(request, response, "get");
                }
            });

            post("/api/:cmd", new spark.Route() {
                @Override
                public Object handle(spark.Request request, spark.Response response) {
                    return handleApiCall(request, response, "post");
                }
            });

            logger.info("Startup finished.");

        } catch (Exception e) {
           logger.error("Caught an exception:");
           e.printStackTrace(System.err);
        }
    }

    /**
     * Stops the REST server gracefully.
     */
    public static void stop() {
        try {
            logger.info("*************************** Now stopping the REST Server gracefully... ******************");
            stop();
            logger.info("REST server now stopped.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

}
