package gaia.cu9.ari.gaiaorbit.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import gaia.cu9.ari.gaiaorbit.data.group.HYGDataProvider;
import gaia.cu9.ari.gaiaorbit.data.group.IStarGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.BrightestStars;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.IAggregationAlgorithm;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.OctreeGenerator;
import gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup.ParticleGroupBinaryIO;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopConfInit;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopSysUtilsFactory;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.SysUtilsFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Generates an octree of star groups. Each octant should have only one object,
 * a star group.
 * 
 * @author tsagrista
 *
 */
public class OctreeGroupGeneratorTest implements IObserver {

    public static void main(String[] args) {
        OctreeGroupGeneratorTest ogt = new OctreeGroupGeneratorTest();
        JCommander jc = new JCommander(ogt, args);
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
    private int maxDepth = 6;

    @Parameter(names = "--maxpart", description = "Maximum number of objects in the densest node of a level")
    private int maxPart = 200000;

    @Parameter(names = "--minpart", description = "Minimum number of objects in a node under which we do not further break the octree")
    private int minPart = 30000;

    @Parameter(names = "--discard", description = "Whether to discard stars due to density")
    private boolean discard = false;

    @Parameter(names = { "-h", "--help" }, help = true)
    private boolean help = false;

    public OctreeGroupGeneratorTest() {
        super();
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

            ThreadLocalFactory.initialize(new SingleThreadLocalFactory());

            // Add notif watch
            EventManager.instance.subscribe(this, Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            generateOctree();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateOctree() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        IAggregationAlgorithm aggr = new BrightestStars(maxDepth, maxPart, minPart, discard);

        OctreeGenerator og = new OctreeGenerator(aggr);

        /** HIP **/
        HYGDataProvider hyg = new HYGDataProvider();

        /** CATALOG **/
        String fullLoaderClass = "gaia.cu9.ari.gaiaorbit.data.group." + loaderClass;
        IStarGroupDataProvider loader = (IStarGroupDataProvider) Class.forName(fullLoaderClass).newInstance();

        /** LOAD HYG **/
        Array<StarBean> listHip = hyg.loadData("data/hyg/hygxyz.bin");
        Map<Integer, StarBean> hips = new HashMap<Integer, StarBean>();
        for (StarBean p : listHip) {
            if (p.hip() > 0) {
                hips.put((int) p.hip(), p);
            }
        }

        /** LOAD CATALOG **/
        Array<StarBean> listGaia = (Array<StarBean>) loader.loadData(input);
        for (StarBean s : listGaia) {
            if (s.hip() > 0 && hips.containsKey(s.hip())) {
                // modify
                StarBean gaiastar = s;
                StarBean hipstar = hips.get(s.hip());

                gaiastar.name = hipstar.name;
                gaiastar.data[StarBean.I_HIP] = hipstar.data[StarBean.I_HIP];

                // Remove from hipparcos list
                listHip.removeValue(hipstar, true);
                hips.remove(hipstar.hip());

            }
            // Add to main list
            listHip.add(s);
        }
        Array<StarBean> list = listHip;

        Logger.info("Generating octree with " + list.size + " actual stars");

        OctreeNode octree = og.generateOctree(list);

        System.out.println(octree.toString(true));

        /** NUMBERS **/
        Logger.info("Octree generated with " + octree.numNodes() + " octants and " + list.size + " particles");
        Logger.info(aggr.getDiscarded() + " particles have been discarded due to density");

        /** WRITE METADATA **/
        File metadata = new File(outFolder, "metadata.bin");
        if (metadata.exists()) {
            metadata.delete();
        }
        metadata.createNewFile();

        Logger.info("Writing metadata (" + octree.numNodes() + " nodes): " + metadata.getAbsolutePath());

        MetadataBinaryIO metadataWriter = new MetadataBinaryIO();
        metadataWriter.writeMetadata(octree, new FileOutputStream(metadata));

        /** WRITE PARTICLES **/
        ParticleGroupBinaryIO particleWriter = new ParticleGroupBinaryIO();
        File particlesFolder = new File(outFolder + "/particles/");
        particlesFolder.mkdirs();
        writeParticlesToFiles(particleWriter, octree);

    }

    private void writeParticlesToFiles(ParticleGroupBinaryIO particleWriter, OctreeNode current) throws IOException {
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

}
