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

import gaia.cu9.ari.gaiaorbit.data.octreegen.BrightestStars;
import gaia.cu9.ari.gaiaorbit.data.octreegen.IAggregationAlgorithm;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.OctreeGenerator;
import gaia.cu9.ari.gaiaorbit.data.octreegen.ParticleDataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.stars.AbstractCatalogLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.CatalogFilter;
import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.OctreeSingleFileLoader;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopConfInit;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class OctreeGeneratorTest implements IObserver {

    public static enum Operation {
        LOAD_OCTREE, GENERATE_OCTREE
    }

    public static Operation operation = Operation.GENERATE_OCTREE;

    public static void main(String[] args) {
        OctreeGeneratorTest ogt = new OctreeGeneratorTest();
        JCommander jc = new JCommander(ogt, args);
        jc.setProgramName("OctreeGeneratorTest");
        if (ogt.help) {
            jc.usage();
        } else {
            ogt.run();
        }
    }

    @Parameter(names = { "-l", "--loader" }, description = "Name of the star loader class", required = true)
    private String loaderClass;

    @Parameter(names = { "-i", "--input" }, description = "Location of the input catalog", required = true)
    private String input;

    @Parameter(names = { "-o", "--output" }, description = "Output folder. Defaults to system temp")
    private String outFolder;

    @Parameter(names = { "-m", "--multifile" }, description = "Use multiple file mode, outputs each octree node data to its own file")
    private boolean multifile = true;

    @Parameter(names = "--maxdepth", description = "Maximum tree depth in levels")
    private int maxDepth = 15;

    @Parameter(names = "--maxpart", description = "Maximum number of objects in the densest node of a level")
    private int maxPart = 4000;

    @Parameter(names = "--minpart", description = "Minimum number of objects in a node under which we do not further break the octree")
    private int minPart = 200;

    @Parameter(names = "--discard", description = "Whether to discard stars due to density")
    private boolean discard = false;

    @Parameter(names = { "-h", "--help" }, help = true)
    private boolean help = false;

    public OctreeGeneratorTest() {
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

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            ConfInit.initialize(new DesktopConfInit(new FileInputStream(new File(ASSETS_LOC + "conf/global.properties")), new FileInputStream(new File(ASSETS_LOC + "data/dummyversion"))));

            I18n.initialize(new FileHandle(ASSETS_LOC + "i18n/gsbundle"));

            ThreadLocalFactory.initialize(new SingleThreadLocalFactory());

            // Add notif watch
            EventManager.instance.subscribe(this, Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            switch (operation) {
            case GENERATE_OCTREE:
                generateOctree();
                break;
            case LOAD_OCTREE:
                Gdx.files = new LwjglFiles();
                loadOctree();
                break;
            }

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
        HYGBinaryLoader hyg = new HYGBinaryLoader();
        hyg.initialize(new String[] { "data/hygxyz.bin" });
        hyg.addFilter(new CatalogFilter() {
            @Override
            public boolean filter(CelestialBody s) {
                return (s instanceof Particle) && ((Particle) s).appmag <= 5.5f;
            }
        });

        /** CATALOG **/
        String fullLoaderClass = "gaia.cu9.ari.gaiaorbit.data.stars." + loaderClass;
        AbstractCatalogLoader loader = (AbstractCatalogLoader) Class.forName(fullLoaderClass).newInstance();
        loader.initialize(new String[] { input });

        /** LOAD HYG **/
        Array<AbstractPositionEntity> listStars = hyg.loadData();
        Map<Integer, AbstractPositionEntity> hips = new HashMap<Integer, AbstractPositionEntity>();
        for (AbstractPositionEntity p : listStars) {
            if (p instanceof Star && ((Star) p).hip > 0) {
                hips.put(((Star) p).hip, p);
            }
        }

        /** LOAD CATALOG **/
        Array<? extends SceneGraphNode> listGaia = loader.loadData();
        for (SceneGraphNode sgn : listGaia) {
            Particle p = (Particle) sgn;
            if (p instanceof Star && hips.containsKey(((Star) p).hip)) {
                // modify
                Star gaiastar = (Star) p;
                Star hipstar = (Star) hips.get(((Star) p).hip);

                gaiastar.name = hipstar.name;
                gaiastar.hip = hipstar.hip;

                // Remove from hipparcos list
                listStars.removeValue(hipstar, true);
                hips.remove(hipstar.hip);

            }
            // Add to main list
            listStars.add(p);
        }

        // Initialise rgba color array and size
        for (AbstractPositionEntity p : listStars) {
            p.initialize();
        }

        Logger.info("Generating octree with " + listStars.size + " actual stars");

        OctreeNode octree = og.generateOctree(listStars);

        // Put all new particles in list
        listStars.clear();
        octree.addParticlesTo(listStars);

        System.out.println(octree.toString(true));

        /** NUMBERS **/
        Logger.info("Octree generated with " + octree.numNodes() + " octants and " + listStars.size + " particles");
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
        ParticleDataBinaryIO particleWriter = new ParticleDataBinaryIO();
        if (!multifile) {
            File particles = new File(outFolder, "particles.bin");
            if (particles.exists()) {
                particles.delete();
            }
            particles.createNewFile();

            Logger.info("Writing " + listStars.size + " particles to " + particles.getAbsolutePath());

            particleWriter.writeParticles(listStars, new BufferedOutputStream(new FileOutputStream(particles)));
        } else {
            // Walk each node of the octree and write the particles for each in
            // a different file
            File particlesFolder = new File(outFolder + "/particles/");
            particlesFolder.mkdirs();

            writeParticlesToFiles(particleWriter, octree);
        }

    }

    private void writeParticlesToFiles(ParticleDataBinaryIO particleWriter, OctreeNode current) throws IOException {
        // Write current
        if (current.childrenCount > 0) {
            File particles = new File(outFolder + "/particles/", "particles_" + String.format("%06d", current.pageId) + ".bin");
            Logger.info("Writing " + current.childrenCount + " particles of node " + current.pageId + " to " + particles.getAbsolutePath());
            particleWriter.writeParticles(current.objects, new BufferedOutputStream(new FileOutputStream(particles)));
        }

        // Write each child
        if (current.children != null)
            for (OctreeNode child : current.children) {
                if (child != null)
                    writeParticlesToFiles(particleWriter, child);
            }
    }

    private void loadOctree() throws FileNotFoundException {
        String[] files = new String[] { "data/hyg_particles.bin", "data/hyg_metadata.bin" };
        ISceneGraphLoader loader = new OctreeSingleFileLoader();
        loader.initialize(files);
        Array<? extends SceneGraphNode> l = loader.loadData();
        AbstractOctreeWrapper ow = null;
        for (SceneGraphNode n : l) {
            if (n instanceof AbstractOctreeWrapper) {
                ow = (AbstractOctreeWrapper) n;
                break;
            }
        }
        Logger.info(ow.root.toString());

    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            String message = "";
            boolean perm = false;
            for (int i = 0; i < data.length; i++) {
                if (i == data.length - 1 && data[i] instanceof Boolean) {
                    perm = (Boolean) data[i];
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
