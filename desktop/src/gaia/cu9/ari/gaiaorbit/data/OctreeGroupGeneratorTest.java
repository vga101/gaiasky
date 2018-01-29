package gaia.cu9.ari.gaiaorbit.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import gaia.cu9.ari.gaiaorbit.data.group.HYGDataProvider;
import gaia.cu9.ari.gaiaorbit.data.group.IStarGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.BrightestStars;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.IAggregationAlgorithm;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.IStarGroupIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.OctreeGenerator;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.StarGroupBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.StarGroupSerializedIO;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopConfInit;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopSysUtilsFactory;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.SysUtilsFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Generates an octree of star groups. Each octant should have only one object,
 * a star group.
 * 
 * @author tsagrista
 *
 */
public class OctreeGroupGeneratorTest implements IObserver {
    private static JCommander jc;
    private static String[] arguments;

    public static void main(String[] args) {
        arguments = args;
        OctreeGroupGeneratorTest ogt = new OctreeGroupGeneratorTest();
        jc = new JCommander(ogt, args);
        jc.setProgramName("OctreeGeneratorTest");
        if (ogt.help) {
            jc.usage();
        } else {
            ogt.run();
        }
    }

    @Parameter(names = { "-l", "--loader" }, description = "Name of the star group loader class", required = true)
    private String loaderClass;

    @Parameter(names = { "-i", "--input" }, description = "Location of the input catalog", required = true)
    private String input;

    @Parameter(names = { "-o", "--output" }, description = "Output folder. Defaults to system temp")
    private String outFolder;

    @Parameter(names = "--maxdepth", description = "Maximum tree depth in levels")
    private int maxDepth = 10;

    @Parameter(names = "--maxpart", description = "Number of objects in the densest node of a level")
    private int maxPart = 100000;

    @Parameter(names = "--minpart", description = "Number of objects in a node below which we do not further break the octree")
    private int minPart = 5000;

    @Parameter(names = "--discard", description = "Whether to discard stars due to density")
    private boolean discard = false;

    @Parameter(names = "--serialized", description = "Use the java serialization method instead of the binary format to output the particle files")
    private boolean serialized = false;

    @Parameter(names = "--pllxovererr", description = "Parallax over parallax error must be larger than this value for the star to be accepted")
    private double pllxovererr = 7d;

    @Parameter(names = "--pllxzeropoint", description = "Zero point value for the parallax in mas")
    private double pllxzeropoint = 0d;

    @Parameter(names = "--nfiles", description = "Caps the number of data files to load. Defaults to unlimited")
    private int fileNumCap = -1;

    @Parameter(names = "--xmatchfile", description = "Crossmatch file with source_id to hip")
    private String xmatchFile = null;

    @Parameter(names = { "-h", "--help" }, help = true)
    private boolean help = false;

    protected Map<Long, float[]> colors;

    public OctreeGroupGeneratorTest() {
        super();
        colors = new HashMap<Long, float[]>();
    }

    public void run() {
        try {

            if (outFolder == null) {
                outFolder = System.getProperty("java.io.tmpdir");
            } else {
                if (!outFolder.endsWith("/"))
                    outFolder += "/";

                File outfolderFile = new File(outFolder);
                outfolderFile.mkdirs();
            }

            // Assets location
            String ASSETS_LOC = (System.getProperty("assets.location") != null ? System.getProperty("assets.location") : "");

            Gdx.files = new LwjglFiles();

            // Thread idx
            ThreadIndexer.initialize(new SingleThreadIndexer());

            // Sys utils
            SysUtilsFactory.initialize(new DesktopSysUtilsFactory());

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            ConfInit.initialize(new DesktopConfInit(new FileInputStream(new File(ASSETS_LOC + "conf/global.properties")), new FileInputStream(new File(ASSETS_LOC + "data/dummyversion"))));

            I18n.initialize(new FileHandle(ASSETS_LOC + "i18n/gsbundle"));

            ThreadLocalFactory.initialize(new SingleThreadLocalFactory());

            // Add notification watch
            EventManager.instance.subscribe(this, Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            OctreeNode octree = generateOctree();

            // Save arguments and structure
            StringBuffer argstr = new StringBuffer();
            for (int i = 0; i < arguments.length; i++) {
                argstr.append(arguments[i]).append(" ");
            }
            try (PrintStream out = new PrintStream(new FileOutputStream(outFolder + "log"))) {
                out.print(argstr);
                out.println();
                out.println();
                out.println("OCTREE (" + octree.numNodes() + " nodes, " + octree.countObjects() + " particles)");
                out.print(octree.toString(true));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OctreeNode generateOctree() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        long startMs = TimeUtils.millis();

        IAggregationAlgorithm aggr = new BrightestStars(maxDepth, maxPart, minPart, discard);

        OctreeGenerator og = new OctreeGenerator(aggr);

        /** HIP **/
        HYGDataProvider hyg = new HYGDataProvider();

        /** CATALOG **/
        String fullLoaderClass = "gaia.cu9.ari.gaiaorbit.data.group." + loaderClass;
        IStarGroupDataProvider loader = (IStarGroupDataProvider) Class.forName(fullLoaderClass).newInstance();
        loader.setParallaxOverError(pllxovererr);
        loader.setParallaxZeroPoint(pllxzeropoint);
        loader.setFileNumberCap(fileNumCap);

        /** LOAD HYG **/
        Array<StarBean> listHip = hyg.loadData("data/hyg/hygxyz.bin");
        Map<Integer, StarBean> hips = new HashMap<Integer, StarBean>();
        for (StarBean p : listHip) {
            if (p.hip() > 0) {
                hips.put(p.hip(), p);
            }
        }

        /** LOAD CATALOG **/
        @SuppressWarnings("unchecked")
        Array<StarBean> listGaia = (Array<StarBean>) loader.loadData(input);

        /** Check x-match file **/
        Map<Long, Integer> xmatchTable = null;
        if (xmatchFile != null && !xmatchFile.isEmpty()) {
            // Load xmatchTable
            xmatchTable = readXmatchTable(xmatchFile);
        }
        int hipnum = listHip.size;
        int hiphits = 0;
        for (StarBean s : listGaia) {
            // Check if star is also in HYG catalog
            if ((xmatchTable == null && (s.hip() > 0 && hips.containsKey(s.hip()))) || (xmatchTable != null && (xmatchTable.containsKey(s.id) && hips.containsKey(xmatchTable.get(s.id))))) {
                // Add name and hip number to gaia star
                StarBean gaiastar = s;
                StarBean hipstar = hips.get(xmatchTable.get(s.id));

                gaiastar.name = hipstar.name;
                gaiastar.data[StarBean.I_HIP] = hipstar.data[StarBean.I_HIP];

                // Remove from HYG list
                listHip.removeValue(hipstar, true);
                hips.remove(hipstar.hip());
                hiphits++;
            }
            // Add to main list
            listHip.add(s);
        }
        Logger.info(hiphits + " of " + hipnum + " HYG stars are also in Gaia");

        Array<StarBean> list = listHip;

        long loadingMs = TimeUtils.millis();
        double loadingSecs = ((loadingMs - startMs) / 1000.0);
        Logger.info("TIME STATS: Data loaded in " + loadingSecs + " seconds");

        Logger.info("Generating octree with " + list.size + " actual stars");

        OctreeNode octree = og.generateOctree(list);

        System.out.println(octree.toString(true));

        long generatingMs = TimeUtils.millis();
        double generatingSecs = ((generatingMs - loadingMs) / 1000.0);
        Logger.info("TIME STATS: Octree generated in " + generatingSecs + " seconds");

        /** NUMBERS **/
        Logger.info("Octree generated with " + octree.numNodes() + " octants and " + list.size + " particles");
        Logger.info(aggr.getDiscarded() + " particles have been discarded due to density");

        /** CLEAN CURRENT OUT DIR **/
        File metadataFile = new File(outFolder, "metadata.bin");
        delete(metadataFile);
        File particlesFolder = new File(outFolder, "particles/");
        delete(particlesFolder);

        /** WRITE METADATA **/
        metadataFile.createNewFile();

        Logger.info("Writing metadata (" + octree.numNodes() + " nodes): " + metadataFile.getAbsolutePath());

        MetadataBinaryIO metadataWriter = new MetadataBinaryIO();
        metadataWriter.writeMetadata(octree, new FileOutputStream(metadataFile));

        /** WRITE PARTICLES **/
        IStarGroupIO particleWriter = serialized ? new StarGroupSerializedIO() : new StarGroupBinaryIO();
        particlesFolder.mkdirs();
        writeParticlesToFiles(particleWriter, octree);

        long writingMs = TimeUtils.millis();
        double writingSecs = (writingMs - generatingMs) / 1000.0;
        double totalSecs = loadingSecs + generatingSecs + writingSecs;

        Logger.info("============");
        Logger.info("OCTREE STATS");
        Logger.info("============");
        Logger.info("Octants: " + octree.numNodes());
        Logger.info("Particles: " + list.size);
        Logger.info("Depth: " + octree.depth);
        Logger.info();
        Logger.info("================");
        Logger.info("FINAL TIME STATS");
        Logger.info("================");
        Logger.info("Loading: " + loadingSecs + " seconds");
        Logger.info("Generating: " + generatingSecs + " seconds");
        Logger.info("Writing: " + writingSecs + " seconds");
        Logger.info("Total: " + totalSecs + " seconds");

        return octree;
    }

    private void writeParticlesToFiles(IStarGroupIO particleWriter, OctreeNode current) throws IOException {
        // Write current
        if (current.ownObjects > 0) {
            File particles = new File(outFolder + "/particles/", "particles_" + String.format("%06d", current.pageId) + ".bin");
            Logger.info("Writing " + current.ownObjects + " particles of node " + current.pageId + " to " + particles.getAbsolutePath());
            particleWriter.writeParticles(current.objects, new BufferedOutputStream(new FileOutputStream(particles)));
        }

        // Write each child
        if (current.childrenCount > 0)
            for (OctreeNode child : current.children) {
                if (child != null)
                    writeParticlesToFiles(particleWriter, child);
            }
    }

    private Map<Long, Integer> readXmatchTable(String xmatchFile) {
        File xm = new File(xmatchFile);
        if (xm.exists()) {
            try {
                Map<Long, Integer> map = new HashMap<Long, Integer>();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xm)));
                // Skip header line
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    Long sourceid = Parser.parseLong(tokens[0]);
                    Integer hip = Parser.parseInt(tokens[1]);
                    map.put(sourceid, hip);
                }
                br.close();
                return map;
            } catch (Exception e) {
                Logger.error(e);
            }
        } else {
            Logger.error("Cross-match file '" + xmatchFile + "' does not exist");
        }
        return null;
    }

    private void delete(File element) {
        if (element.isDirectory()) {
            for (File sub : element.listFiles()) {
                delete(sub);
            }
        }
        element.delete();
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            String message = "";
            for (int i = 0; i < data.length; i++) {
                if (i == data.length - 1 && data[i] instanceof Boolean) {
                } else {
                    message += (String) data[i];
                    if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                        message += " - ";
                    }
                }
            }
            System.out.println(message);
            break;
        case JAVA_EXCEPTION:
            Exception e = (Exception) data[0];
            e.printStackTrace(System.err);
            break;
        default:
            break;
        }

    }

    protected void dumpToDiskCsv(Array<StarBean> data, String filename) {
        String sep = ", ";
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println("name, x[km], y[km], z[km], absmag, appmag, r, g, b");
            Vector3d gal = new Vector3d();
            for (StarBean star : data) {
                float[] col = colors.get(star.id);
                gal.set(star.x(), star.y(), star.z()).scl(Constants.U_TO_KM);
                //gal.mul(Coordinates.equatorialToGalactic());
                writer.println(star.name + sep + gal.x + sep + gal.y + sep + gal.z + sep + star.absmag() + sep + star.appmag() + sep + col[0] + sep + col[1] + sep + col[2]);
            }
            writer.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
