package gaia.cu9.ari.gaiaorbit.rest;

import gaia.cu9.ari.gaiaorbit.script.EventScriptingInterface;
import gaia.cu9.ari.gaiaorbit.script.IScriptingInterface;


//import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.before;
import static spark.Spark.setPort;
import static spark.Spark.path;
//import spark.Request;
//import spark.Response;
//import spark.Route;
//import spark.QueryParamsMap;


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
 * Return values are .... tbd.
 *
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
 *
 *
 * - initialized in desktop/src/gaia/cu9/ari/gaiaorbit/desktop/GaiaSkyDesktop.java
 *   with lazy initialization 
 *   TSS: "EventScriptingInterface needs to pull an asset manager from GaiaSky to load assets.
 *       Since GaiaSky only exists after the actual application has been launched and is running".
 */
public class RESTServer {

    // reference for lazy initialization
    private static EventScriptingInterface esi = null;

    /** 
     * Prints message to console and as response. 
     * This is a development method, which may disappear later.
     */
    private static String message(String s) {
        /* returns its argument (for response), but also prints on terminal */
        System.out.println("[REST] " + s);
        return s;
    }

    /** 
     * Returns an instance of the EventScriptingInterface for the lazy initialization.
     */
    private static IScriptingInterface gs() {
        if (esi == null) {
            message("Lazy initialization of EventScriptingInterface...");
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

        // debug output
        if (ret != null) {
            String msg = "StringValues returns: " + ret.length + " elements.";
            for (int i = 0; i < ret.length; i++) {
                msg += "\n[" + i + "] = " + ret[i];
            }
            message(msg);
        }

        return ret;
    }


    /**
     * Handles the API call.
     * TODO: rename to handleApiCall
     */
    private static String handleApiCall(spark.Request request, spark.Response response) {
        message("Handling API call:");
        message("path param cmd = " + request.params(":cmd") + ", query params q = " + request.queryParams());
        message("Query string: " + request.queryString());

        String cmd = request.params(":cmd");
        //spark.QueryParamsMap qm = request.queryMap();  // see http://spark.screenisland.com/spark/QueryParamsMap.html

        try {

            /**
            --> Methods from IScriptingInterface (core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java).
            Listed in that order, consult that for the meaning of parameters.
            Some are omitted currently. 

            TODO: 
            - Return values: json? text?
            - For functions with parameters: 
              need to check that all necessary params are there, but no more (to catch errors!)
            */

            message("### API command: " + cmd);

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
                return message("OK");
            }

            /** 
             * activateRealTimeFrame.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/activateRealTimeFrame"
             */
            if (cmd.equals("activateRealTimeFrame")) {
                gs().activateRealTimeFrame();
                return message("OK");
            }

            /** 
             * activateSimulationTimeFrame.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/activateSimulationTimeFrame"
             */
            if (cmd.equals("activateSimulationTimeFrame")) {
                gs().activateSimulationTimeFrame();
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }

            /**
             * clearHeadlineMessage.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/clearHeadlineMessage" --data-urlencode "level=23"
             */
            if (cmd.equals("clearHeadlineMessage")) {
                gs().clearHeadlineMessage();
                return message("OK");
            }

            /**
             * clearSubheadMessage.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/clearSubheadMessage" --data-urlencode "level=23"
             */
            if (cmd.equals("clearSubheadMessage")) {
                gs().clearSubheadMessage();
                return message("OK");
            }

            /**
             * clearAllMessages.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/clearAllMessages" --data-urlencode "level=23"
             */
            if (cmd.equals("clearAllMessages")) {
                gs().clearAllMessages();
                return message("OK");
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
                return message("OK");
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
                message("text=\"" + request.queryParams("text") + "\".");
                return message("OK");
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
                    return message("OK");
                } else {
                    // without color specification
                    gs().displayImageObject(
                        intValues(request, "id")[0],
                        StringValues(request, "path")[0],
                        floatValues(request, "x")[0],
                        floatValues(request, "y")[0]
                    );
                    return message("OK");
                }
            }
 
            /**
             * removeAllObjects.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/removeAllObjects" -d dummypara=useless
             */
            if (cmd.equals("removeAllObjects")) {
                gs().removeAllObjects();
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }

            /**
             * disableInput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/disableInput"
             */
            if (cmd.equals("disableInput")) {
                gs().disableInput();
                return message("OK");
            }

            /**
             * enableInput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/enableInput"
             */
            if (cmd.equals("enableInput")) {
                gs().enableInput();
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }

            /**
             * setCameraFree.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFree"
             */
            if (cmd.equals("setCameraFree")) {
                gs().setCameraFree();
                return message("OK");
            }

            /**
             * setCameraFov1.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFov1"
             */
            if (cmd.equals("setCameraFov1")) {
                gs().setCameraFov1();
                return message("OK");
            }

            /**
             * setCameraFov2.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFov2"
             */
            if (cmd.equals("setCameraFov2")) {
                gs().setCameraFov2();
                return message("OK");
            }

            /**
             * setCameraFov1and2.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/setCameraFov1and2"
             */
            if (cmd.equals("setCameraFov1and2")) {
                gs().setCameraFov1and2();
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }

            /**
             * cameraStop.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraStop"
             */
            if (cmd.equals("cameraStop")) {
                gs().cameraStop();
                return message("OK");
            }

            /**
             * cameraCenter.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/cameraCenter"
             */
            if (cmd.equals("cameraCenter")) {
                gs().cameraCenter();
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }

            /**
             * startSimulationTime.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/startSimulationTime"
             */
            if (cmd.equals("startSimulationTime")) {
                gs().startSimulationTime();
                return message("OK");
            }

            /**
             * stopSimulationTime.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/stopSimulationTime"
             */
            if (cmd.equals("stopSimulationTime")) {
               gs().stopSimulationTime();
               return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }

            /**
             * isRenderOutputActive. Returns True/False.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/isRenderOutputActive"
             */
            if (cmd.equals("isRenderOutputActive")) {
                boolean ret = gs().isRenderOutputActive();
                if (ret) {
                    return message("True");
                } else {
                    return message("False");
                }
            }

            /**
             * getRenderOutputFps. Returns FPS setting (int).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getRenderOutputFps"
             */
            if (cmd.equals("getRenderOutputFps")) {
                int fps = gs().getRenderOutputFps();
                return message("" + fps);
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
                return message("OK");
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
                return message("OK");
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
                return message("" + dist);
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
                return message("OK");
            }

            /**
             * maximizeInterfaceWindow.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/maximizeInterfaceWindow"
             */
            if (cmd.equals("maximizeInterfaceWindow")) {
                gs().maximizeInterfaceWindow();
                return message("OK");
            }

            /**
             * minimizeInterfaceWindow.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/minimizeInterfaceWindow"
             */
            if (cmd.equals("minimizeInterfaceWindow")) {
                gs().minimizeInterfaceWindow();
                return message("OK");
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
                return message("OK");
            }

            /**
             * waitForInput.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/waitForInput"
             */
            if (cmd.equals("waitForInput")) {
                gs().waitForInput();
                return message("OK");
            }

            /**
             * waitForEnter.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/waitForEnter"
             */
            if (cmd.equals("waitForEnter")) {
                gs().waitForEnter();
                return message("OK");
            }

            /**
             * getScreenWidth. Returns screen width (int).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getScreenWidth"
             */
            if (cmd.equals("getScreenWidth")) {
                int width = gs().getScreenWidth();
                return message("" + width);
            }

            /**
             * getScreenHeight. Returns screen height (int).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getScreenHeight"
             */
            if (cmd.equals("getScreenHeight")) {
                int width = gs().getScreenHeight();
                return message("" + width);
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

                // return value:
                String msg = "";
                if (pas != null) {
                    for (int i = 0; i < 4; i++) {
                        msg += pas[i] + " ";
                    }
                } else {
                    msg += pas;
                }
                return message(msg);
            }

            /**
             * getVersionNumber. Returns version number (String).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/getVersionNumber"
             */
            if (cmd.equals("getVersionNumber")) {
                String version = gs().getVersionNumber();
                return message("" + version);
            }

            /**
             * waitFocus. Returns whether timeout ran out (boolean).
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/waitFocus?name=Uranus&timeoutMs=-1"
             */
            // TODO: not clear how / whether works.
            if (cmd.equals("waitFocus")) {
                boolean wf = gs().waitFocus(
                    StringValues(request, "name")[0],
                    longValues(request, "timeoutMs")[0]
                );
                return message("" + wf);
            }

            /**
             * startRecordingCameraPath.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/startRecordingCameraPath"
             */
            if (cmd.equals("startRecordingCameraPath")) {
                gs().startRecordingCameraPath();
                return message("OK");
            }

            /**
             * stopRecordingCameraPath.
             * Test:
             * curl -w "\n[curl: HTTP-Code=%{http_code}, time=%{time_total}]\n"   "http://localhost:8080/api/stopRecordingCameraPath"
             */
            if (cmd.equals("stopRecordingCameraPath")) {
                gs().stopRecordingCameraPath();
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
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
                return message("OK");
            }


            /* 
             * ----------------------------------------------
             * if nothing matched so far, API call is invalid
             * ----------------------------------------------
             */
            return message("ERROR. API call not recognized: '" + cmd + "'");

        } catch (Exception e) {
            message("Caught an exception: could not process request.");
            e.printStackTrace(System.err);
            return message("ERROR");
        }

    }


    /**
     * Initialize the REST server.
     *
     * Sets the routes and then passes the call to the handler.
     */
    public static void initialize(Integer port) {

        // check for valid TCP port (otherwise disabled)
        if (port < 1) {
            return;
        }

        try {

            message("Initializing REST API server on TCP port " + 1);
            message("WARNING: remote code execution! Only use this functionality in a trusted environment!");
            setPort(port);  // note: deprecated in newer Spark versions in favor of port()
            message("Lazy initialization of EventScriptingInterface.");
            message("Now setting routes...");

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
                    return handleApiCall(request, response);
                }
            });

            post("/api/:cmd", new spark.Route() {
                @Override
                public Object handle(spark.Request request, spark.Response response) {
                    return handleApiCall(request, response);
                }
            });

            message("Route setup done.");

        } catch (Exception e) {
           message("Caught an exception:");
           e.printStackTrace(System.err);
        }
    }

    /**
     * Stops the REST server gracefully.
     */
    public static void stop() {
        try {
            System.out.println("*************************** Now stopping the REST Server gracefully... ******************");
            stop();
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }
    
}
