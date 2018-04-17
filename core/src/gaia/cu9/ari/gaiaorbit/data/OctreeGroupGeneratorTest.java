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
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.NotificationsListener;
import gaia.cu9.ari.gaiaorbit.util.SysUtilsFactory;
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
public class OctreeGroupGeneratorTest {
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

    @Parameter(names = "--maxpart", description = "Maximum number of objects in an octant")
    private int maxPart = 100000;

    @Parameter(names = "--serialized", description = "Use the java serialization method instead of the binary format to output the particle files")
    private boolean serialized = false;

    @Parameter(names = "--pllxerrfaint", description = "Parallax error factor for faint (gmag>=13.1) stars, acceptance criteria as a percentage of parallax error with respect to parallax, in [0..1]")
    private double pllxerrfaint = 0.125;

    @Parameter(names = "--pllxerrbright", description = "Parallax error factor for bright (gmag<13.1) stars, acceptance criteria as a percentage of parallax error with respect to parallax, in [0..1]")
    private double pllxerrbright = 0.25;

    @Parameter(names = "--adaptivepllx", description = "On by default, this enables the adaptive parallax criterion, which relaxes the threshold for bright stars to avoid artifacts")
    private boolean adaptivepllx = true;

    @Parameter(names = "--pllxzeropoint", description = "Zero point value for the parallax in mas")
    private double pllxzeropoint = 0d;

    @Parameter(names = "--nfiles", description = "Caps the number of data files to load. Defaults to unlimited")
    private int fileNumCap = -1;

    @Parameter(names = "--xmatchfile", description = "Crossmatch file with source_id to hip")
    private String xmatchFile = null;

    @Parameter(names = { "-c", "--magcorrections" }, description = "Flag to apply magnitude and color corrections for extinction and reddening")
    private boolean magCorrections = false;

    @Parameter(names = { "-h", "--help" }, help = true)
    private boolean help = false;

    protected Map<Long, float[]> colors;

    private NotificationsListener nl;

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

            // Sys utils
            SysUtilsFactory.initialize(new DesktopSysUtilsFactory());

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            ConfInit.initialize(new DesktopConfInit(new FileInputStream(new File(ASSETS_LOC + "conf/global.properties")), new FileInputStream(new File(ASSETS_LOC + "data/dummyversion"))));

            I18n.initialize(new FileHandle(ASSETS_LOC + "i18n/gsbundle"));

            // Add notification watch
            EventManager.instance.subscribe((nl = new NotificationsListener()), Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            generateOctree();

            // Save arguments and structure
            StringBuffer argstr = new StringBuffer();
            for (int i = 0; i < arguments.length; i++) {
                argstr.append(arguments[i]).append(" ");
            }
            try (PrintStream out = new PrintStream(new FileOutputStream(outFolder + "log"))) {
                out.print(argstr);
                out.println();
                out.println();
                for (String msg : nl.logMessages) {
                    out.println(msg);
                }
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

        IAggregationAlgorithm aggr = new BrightestStars(25, maxPart, maxPart, false);

        OctreeGenerator og = new OctreeGenerator(aggr);

        /** HIP **/
        HYGDataProvider hyg = new HYGDataProvider();

        /** CATALOG **/
        String fullLoaderClass = "gaia.cu9.ari.gaiaorbit.data.group." + loaderClass;
        IStarGroupDataProvider loader = (IStarGroupDataProvider) Class.forName(fullLoaderClass).newInstance();
        loader.setParallaxErrorFactorFaint(pllxerrfaint);
        loader.setParallaxErrorFactorBright(pllxerrbright);
        loader.setParallaxZeroPoint(pllxzeropoint);
        loader.setFileNumberCap(fileNumCap);
        loader.setMagCorrections(magCorrections);
        long[] cpm = loader.getCountsPerMag();

        /** LOAD HYG **/
        Array<StarBean> listHip = hyg.loadData("data/hyg/hygxyz.bin");

        /** LOAD CATALOG **/
        @SuppressWarnings("unchecked")
        Array<StarBean> listGaia = (Array<StarBean>) loader.loadData(input);

        /** Check x-match file **/
        Map<Long, Integer> xmatchTable = null;
        if (xmatchFile != null && !xmatchFile.isEmpty()) {
            // Load xmatchTable
            xmatchTable = readXmatchTable(xmatchFile);
        }
        int gaianum = listGaia.size;
        int gaiahits = 0;
        for (StarBean s : listGaia) {
            // Check if star is also in HYG catalog
            if ((xmatchTable == null || (xmatchTable != null && !xmatchTable.containsKey(s.id)))) {
                // No hit, add to main list
                listHip.add(s);
            } else {
                // Keep HIP star, ignore Gaia star
                gaiahits++;
            }
        }
        Logger.info(gaiahits + " of " + gaianum + " Gaia stars discarded due to being matched to a HIP star");

        // Main list is listHip
        Array<StarBean> list = listHip;

        // Free some memory
        listGaia.clear();
        listGaia = null;

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

        int[][] stats = octree.stats();

        if (cpm != null) {
            Logger.info("=================");
            Logger.info("STAR COUNTS STATS");
            Logger.info("=================");
            for (int level = 0; level < cpm.length; level++) {
                Logger.info("Magnitude " + level + ": " + cpm[level] + " stars");
            }
            Logger.info();
        }

        Logger.info("============");
        Logger.info("OCTREE STATS");
        Logger.info("============");
        Logger.info("Octants: " + octree.numNodes());
        Logger.info("Particles: " + list.size);
        Logger.info("Depth: " + octree.getMaxDepth());
        int level = 0;
        for (int[] levelinfo : stats) {
            Logger.info("   Level " + level + ": " + levelinfo[0] + " octants, " + levelinfo[1] + " stars");
            level++;
        }
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
