package gaia.cu9.ari.gaiaorbit.rest;

import gaia.cu9.ari.gaiaorbit.script.EventScriptingInterface;
import gaia.cu9.ari.gaiaorbit.script.IScriptingInterface;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;


/**
 * REST API for remote procedure calls
 *
 * @author Volker Gaibler, HITS
 *
 * WARNING: only allow and use this in a trusted environment. Incoming commands are
 * *not checked at all* before execution! Remote command execution is generally
 * dangerous. This REST API was developed for an exhibition with an isolated network.
 *
 * The API allows to call methods from the scripting interface
 * (gaia.cu9.ari.gaiaorbit.script.IScriptingInterface) remotely via HTTP for remote control.
 *
 * Syntax of API commands is set to be close to the Java method interface, but does not cover
 * it in all generality to permit simple usage. Particularly note that the REST server receives
 * strings from the client and will try to convert them into correct types.
 *
 * Commands require HTTP request parameter having the names for the formal parameters of the
 * script interface methods to allow simple construction of HTTP requests based on the scripting
 * interface source documentation. We use Java reflections with access to the formal parameter
 * names. Accordingly, the code needs to be compiled with "-parameters" (otherwise parameters
 * are named arg0, arg1, ...).
 *
 * Both GET and POST requests are accepted. Although GET requests are not supposed to have
 * side-effects, we include them for easy usage with a browser.
 *
 * Issue commands with a syntax like the following:
 * - http://localhost:8080/api/setCameraUp?up=[1.,0.,0.]
 * - http://localhost:8080/api/getScreenWidth
 * - http://localhost:8080/api/goToObject?name=Jupiter&angle=32.9&focusWait=2
 *
 * Give booleans, ints, floats, doubles, strings as they are, vectors comma-separated with
 * square brackets around: true, 42, 3.1, 3.14, Superstring, [1,2,3], [Do,what,they,told,ya].
 * Note that you might need to escape or url-encode characters in a browser for this
 * (e.g. spaces or "=").
 *
 * Response with return data is in JSON format, containing key/value pairs.
 * The "success" pair tells you about success/failure of the call,
 * the "value" pair gives the return value. Void methods will contain a "null" return value.
 * The "text" pair can give additional information on the call.
 *
 * The 'cmd_syntax' entry you get from the 'help' command (e.g. http://localhost:8080/api/help)
 * gives a summary of permitted commands and their return type. Details on the meaning of the
 * command and its parameters need to be found from the scripting API documention:
 * https://langurmonkey.github.io/gaiasky/javadoc/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.html
 *
 * To examine, what happens during an API call, set the default loglevel of SimpleLogger to
 * 'info' or lower (in core/build.gradle).
 *
 * Return values are given as JSON objects that contain key-value pairs:
 * - "success" indicates whether the API call was executed successful or not
 * - "text" may give additional text information
 * - "value" contains the return value or null if there is no return value
 *
 * For testing with curl, a call like the following allows will deal with url-encoding:
 * curl "http://localhost:8080/api/setHeadlineMessage" --data headline='Hi, how are you?'
 */


/**
 * REST Server class to implement the REST API
 * @author Volker Gaibler
 *
 * Implemented with Spark, which launches an embedded jetty.
 * Spark recommends static context.
 *
 * This gets initialized in core/src/gaia/cu9/ari/gaiaorbit/desktop/GaiaSkyDesktop.java
 * with some lazy initialization since Spark wants be be used in static context.
 */
public class RESTServer {

    /* Class variables: */

    /**
     * "Shutdown already triggered" flag.
     * stop() can be called multiple times (multiple events), but only processed once.
     */
    private static boolean shutdownTriggered = false;


    /**
     * Activated flag.
     * Calling API methods generally requires the GUI to be fully started and
     * all objects initialized, indicated by the "activated" flag. This flag is
     * set true through the activate() method that needs to be called
     * externally once the GUI is ready.
     */
    private static boolean activated = false;


    /**
     * Reference to Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger("[RESTServer]");


    /* Methods: */

    /**
     * Prints startup warning and current log level of SimpleLogger.
     */
    private static void printStartupInfo() {
        String s = System.getProperty("org.slf4j.simpleLogger.defaultLogLevel");
        System.out.println("Simple Logger defaultLogLevel = " + s);
        System.out.println("*** Warning: REST API server allows remote code execution! "
                + "Only use this functionality in a trusted environment! ***");


    }


    /**
     * Sets the HTTP response in JSON format.
     * - Return values are returned under the "value" key.
     * - Additional keys may provide further data or information.
     * - The "text" key is encouraged for human-readable information.
     */
    private static String responseData(spark.Request request,
                                       spark.Response response,
                                       Map<String, Object> ret,
                                       boolean success) {

        String responseString;

        /* Content-Type */
        response.type("application/json");

        /* return value is null for void methods */
        ret.putIfAbsent("value", null);

        /* success key and HTTP status code */
        ret.put("success", success);
        if (success == true) {
            response.status(200);  // 200 OK
            ret.putIfAbsent("text", "OK");
        } else {
            response.status(400);  // 400 Bad Request
            ret.putIfAbsent("text", "Failed");
        }

        /* header */
        //response.header("FOO", "bar");

        /* request body */
        Gson gson = new GsonBuilder().serializeNulls().create();
        responseString = gson.toJson(ret);
        logger.debug("HTTP response body: {}.", responseString);
        return responseString;
    }


    /**
     * Log information on the request.
     */
    private static void loggerRequestInfo(spark.Request request) {
        logger.info("======== Handling API call via HTTP {}: ========", request.requestMethod());
        logger.info("* Parameter extracted:");
        logger.info("  command = {}", request.params(":cmd"));
        logger.info("* Request:");
        logger.info("  client IP = {}", request.ip());
        logger.info("  host = {}", request.host());
        logger.info("  userAgent = {}", request.userAgent());
        logger.info("  pathInfo = {}", request.pathInfo());
        logger.info("  servletPath = {}", request.servletPath());
        logger.info("  contextPath = {}", request.contextPath());
        logger.info("  url = {}", request.url());
        logger.info("  uri = {}", request.uri());
        logger.info("  protocol = {}", request.protocol());
        logger.info("* Body");
        logger.info("  contentType() = '{}'", request.contentType());
        logger.info("  params() = '{}'", request.params());
        logger.info("  body contentLenght() = {}", request.contentLength());
        // NOTE: when calling method body(), the body is consumed and queryParams() doesn't find
        // the parameters anymore!
        // logger.info("body() = '{}'", request.body());
        logger.info("* Query parameters");
        logger.info("  queryString() = '{}'", request.queryString());
        logger.info("  queryParams = {}", request.queryParams());
        for (String s : request.queryParams()) {
            logger.info("    '{}' => '{}'", s, request.queryParams(s));
        }
    }


    /**
     * Returns a declaration string for the given method.
     */
    private static String methodDeclarationString(Method method) {
        Parameter[] methodParams = method.getParameters();

        String ret = method.getName();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter p = methodParams[i];
            ret += String.format("%s%s=(%s)", ((i == 0) ? "?" : "&"),
                p.getName(),
                p.getType().getSimpleName());
        }
        // \u27F6 is "⟶"
        ret += String.format(" \u27F6 %s", method.getReturnType().getSimpleName());
        return ret;
    }


    /**
     * Returns a list of all matching method declaration strings.
     * To get a list of all method declarations, use empty string for methodname.
     */
    private static String[] getMethodDeclarationStrings(String methodname) {
        Class cisi = IScriptingInterface.class;
        Method[] allMethods = cisi.getDeclaredMethods();

        List<String> matchMethodsDeclarations = new ArrayList<String>();
        for (int i = 0; i < allMethods.length; i++) {
            if (methodname.length() == 0 || methodname.equals(allMethods[i].getName())) {
                String declaration = methodDeclarationString(allMethods[i]);
                matchMethodsDeclarations.add(declaration);
            }
        }
        Collections.sort(matchMethodsDeclarations);
        String[] ret = matchMethodsDeclarations.toArray(new String[0]);
        return ret;
    }


    /**
     * Converts an array-representing string and returns it as array of strings.
     * This defines how array need to be passed as HTTP request parameters:
     * comma-separated and enclosed in square brackets, e.g. "[var1,var2,var3]"
     */
    private static String[] splitArrayString(String arrayString) {
        int len = arrayString.length();
        if (len >= 2 && "[".equals(arrayString.substring(0,1))
                     && "]".equals(arrayString.substring(len-1,len))) {
            return arrayString.substring(1,len-1).split(",");
        } else {
            // probably an array should never be empty
            logger.warn("splitArrayString: '{}' is parsed as empty array!", arrayString);
            throw new IllegalArgumentException();
            // emtpy array
            //return new String[0];
        }
    }


    /**
     * Handles the API call.
     *
     * This is implemented via Java Reflections and gives access to all methods from
     * IScriptingInterface (core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java).
     * Additionally, it provides "special-purpose commands", see commented source block.
     *
     * Since HTTP request variables are all strings and do provide neither argument indices
     * nor argument types, there cannot be a direct translation to Java without adding
     * additional information (which would make the HTTP request harder to read and write.
     * If this ever becomes an issue, we could get optionally add a type to the parameters,
     * e.g. "distance_float=0.3f" and split by the underscore.
     */
    private static String handleApiCall(spark.Request request, spark.Response response) {

        // Logging basic request information
        loggerRequestInfo(request);

        // map containing information for http return response
        Map<String, Object> ret = new HashMap<String, Object>();

        // Only process API calls if already activated (GUI launched).
        if (!activated) {
            String msg = "GUI not yet initialized. Please wait...";
            logger.warn(msg);
            ret.put("text", msg);
            return responseData(request, response, ret, false);
        }

        // Extract command from http request
        String cmd = request.params(":cmd");

        // params
        Set<String> queryParams = request.queryParams();

        // get set of permitted API commands
        Class cisi = IScriptingInterface.class;
        Method[] allMethods = cisi.getDeclaredMethods();

        /* Special-treatment commands */
        if ("help".equals(cmd)) {
            logger.info("Help command received");
            ret.put("text", "Help: see 'cmd_syntax' for command reference. "
                + "Vectors are comma-separated.");
            ret.put("cmd_syntax", getMethodDeclarationStrings(""));
            return responseData(request, response, ret, true);
        } else if ("debugCall".equals(cmd)) {
            logger.info("debugCall received. What to do now?");
            ret.put("text", "debugCall data");
            return responseData(request, response, ret, true);
        }

        /* Method matching (name and parameters) */
        logger.info("Method matching...");
        int matchIndex = -1;
        boolean methodNameMatches = false;
        for (int i = 0; i < allMethods.length; i++) {
            logger.debug("match check cmd={} with method={}...", cmd, allMethods[i].getName());

            // name matches, but parameters may be different
            if (allMethods[i].getName().equals(cmd)) {
                logger.info("  [+] name matches");
                methodNameMatches = true;
                Parameter[] methodParams = allMethods[i].getParameters();

                // check if parameters present (and optionally type fits?)
                boolean allParamsFound = true;
                for (Parameter p : methodParams) {
                    if (!queryParams.contains(p.getName())) {
                        allParamsFound = false;
                        logger.info("  [+] method parameters not present");
                        break;  // no need to continue checking
                    }
                    // could test for parameter type here...
                }

                if (allParamsFound) {
                    logger.info("  [+] method parameters ok");
                    matchIndex = i;
                    break;  // no need to continue checking: the the first match
                }
            }
        }

        /* Handle matching result */
        if (matchIndex >= 0) {
            /* Found suitable method */
            logger.info("Suitable method found: {}",
                methodDeclarationString(allMethods[matchIndex]));

            Method matchMethod = allMethods[matchIndex];
            Parameter[] matchParameters = matchMethod.getParameters();
            Class matchReturnType = matchMethod.getReturnType();

            Object[] arguments = new Object[matchParameters.length];
            Class[] types = new Class[matchParameters.length];

            /* Prepare method arguments */
            logger.info("Preparing method arguments...");
            for (int i = 0; i < matchParameters.length; i++) {
                Parameter p = matchParameters[i];
                String stringValue = request.queryParams(p.getName());
                logger.info("  [+] handling parameter '{}'", p.getName());

                // Set type
                types[i] = p.getType();
                logger.info("  [+] parameter getType='{}', isPrimitive={}",
                        p.getType().getSimpleName(),
                        p.getType().isPrimitive());

                /*
                 * Set argument value, handling depends on type:
                 * We test against both primitives (.TYPE) and objects (.class).
                 * You might need to add more in the future if you trigger exceptions here...
                 */
                try {
                    if (Integer.TYPE.equals(types[i]) || Integer.class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type int");
                        arguments[i] = Integer.parseInt(stringValue);

                    } else if (Long.TYPE.equals(types[i]) || Long.class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type long");
                        arguments[i] = Long.parseLong(stringValue);

                    } else if (Float.TYPE.equals(types[i]) || Float.class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type float");
                        arguments[i] = Float.parseFloat(stringValue);

                    } else if (Double.TYPE.equals(types[i]) || Double.class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type double");
                        arguments[i] = Double.parseDouble(stringValue);

                    } else if (Boolean.TYPE.equals(types[i]) || Boolean.class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type boolean");
                        arguments[i] = Boolean.parseBoolean(stringValue);

                    } else if (int[].class.equals(types[i]) || Integer[].class.equals(types[i])) {
                        logger.info("handling parameter as type int[]");
                        String[] svec = splitArrayString(stringValue);
                        int[] dvec = new int[svec.length];
                        for (int vi = 0; vi < svec.length; vi++) {
                            dvec[vi] = Integer.parseInt(svec[vi]);
                        }
                        arguments[i] = dvec;
                        logger.info("  [+] argument={}", Arrays.toString(dvec));

                    } else if (float[].class.equals(types[i]) || Float[].class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type float[]");
                        String[] svec = splitArrayString(stringValue);
                        float[] dvec = new float[svec.length];
                        for (int vi = 0; vi < svec.length; vi++) {
                            dvec[vi] = Float.parseFloat(svec[vi]);
                        }
                        arguments[i] = dvec;
                        logger.info("  [+] argument={}", Arrays.toString(dvec));

                    } else if (double[].class.equals(types[i]) || Double[].class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type double[]");
                        String[] svec = splitArrayString(stringValue);
                        double[] dvec = new double[svec.length];
                        for (int vi = 0; vi < svec.length; vi++) {
                            dvec[vi] = Double.parseDouble(svec[vi]);
                        }
                        arguments[i] = dvec;
                        logger.info("  [+] argument={}", Arrays.toString(dvec));

                    } else if (String[].class.equals(types[i])) {
                        logger.info("  [+] handling parameter as type String[]");
                        String[] svec = splitArrayString(stringValue);
                        arguments[i] = svec;
                        logger.info("  [+] argument={}", Arrays.toString(svec));

                    } else {
                        logger.info("  [+] handling parameter as type String");
                        // String also if it is some other, will raise exception
                        arguments[i] = stringValue;
                    }

                } catch (IllegalArgumentException e) {
                    String msg = String.format("Argument failure with parameter '%s'", p.getName());
                    logger.warn(msg);
                    ret.put("text", msg);
                    ret.put("cmd_syntax", getMethodDeclarationStrings(cmd));
                    return responseData(request, response, ret, false);
                }
            }

            /* Invoke method */
            try {
                logger.debug("Invoking method...");
                // note: invoke may return null explicitly or because is void type
                Object retobj = matchMethod.invoke(EventScriptingInterface.instance(), arguments);
                if (retobj == null) {
                    logger.info("Method returned: '{}', return type is {}",
                        retobj,
                        matchReturnType);
                } else {
                    logger.info("Method returned: '{}', isArray={}",
                        retobj,
                        retobj.getClass().isArray());
                }
                ret.put("value", retobj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseData(request, response, ret, true);

        } else {
            /* No match: could not find matching method */
            logger.info("No suitable method found.");

            String msg;
            if (methodNameMatches) {
                msg = String.format("Failed: command name '%s' found, "
                    + "but arguments not compatible.See syntax in 'cmd_syntax'.", cmd);
                ret.put("cmd_syntax", getMethodDeclarationStrings(cmd));
            } else {
                msg = String.format("Failed: command name '%s' not found. "
                    + "See syntax in 'cmd_syntax'.", cmd);
                ret.put("cmd_syntax", getMethodDeclarationStrings(""));
            }
            logger.warn(msg);
            ret.put("text", msg);
            return responseData(request, response, ret, false);
        }
    }


    /**
     * Initialize the REST server.
     *
     * Sets the routes and then passes the call to the handler.
     */
    public static void initialize(Integer port) {

        /* Check for valid TCP port (otherwise considered as "disabled") */
        if (port < 0) {
            return;
        }

        try {
            printStartupInfo();

            logger.info("Starting REST API server on port {}", port);
            port(port);
            logger.info("Setting routes");

            /* Static file location */
            /* (add static HTML files with API use examples, a folder in the class path) */
            staticFiles.location("/rest-static");

            /* Route mapping */
            get("/api", (request, response) -> {
                response.redirect("/api/help");
                return response;
            });

            get("/api/:cmd", (request, response) -> {
                return handleApiCall(request, response);
            });

            post("/api/:cmd", (request, response) -> {
                return handleApiCall(request, response);
            });

            logger.info("Startup finished.");

        } catch (Exception e) {
           logger.error("Caught an exception during initialization:");
           e.printStackTrace(System.err);
        }
    }

    /**
     * Activate.
     * Set the "activated" flag for the server.
     */
    public static void activate() {
        activated = true;
    }

    /**
     * Stops the REST server gracefully.
     */
    public static void stop() {
        try {
            if (!shutdownTriggered) {
                shutdownTriggered = true;
                logger.info("Stopping server gracefully...");
                stop();
                logger.info("Server now stopped.");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

}
