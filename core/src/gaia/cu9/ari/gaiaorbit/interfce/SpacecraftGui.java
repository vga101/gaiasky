package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SpacecraftCamera;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnImageButton;

public class SpacecraftGui implements IGui {
    private Skin skin;
    protected Stage ui;

    private Container<HorizontalGroup> buttonContainer;
    private HorizontalGroup buttonRow;
    private OwnImageButton stabilise, stop;

    /** The spacecraft camera **/
    private SpacecraftCamera camera;

    /** Camera to render the attitude indicator system **/
    private PerspectiveCamera aiCam;

    /** Attitude indicator **/
    private ModelBatch mb;
    private DecalBatch db;
    private SpriteBatch sb;
    private Model aiModel;
    private ModelInstance aiModelInstance;
    private Texture aiTexture, aiPointerTexture, controlPadTexture, aiVelTex, aiAntivelTex;
    private Decal aiVelDec, aiAntivelDec;
    private Environment env;
    private Matrix4 aiTransform;
    private Viewport aiViewport;
    private DirectionalLight dlight;
    /** Reference to spacecraft camera rotation quaternion **/
    private Quaternion qf;
    /** Reference to spacecraft camera velocity vector **/
    private Vector3d vel;

    private int indicatorw, indicatorh;

    /** Aux vectors **/
    private Vector3 aux3f1, aux3f2;

    public SpacecraftGui(SpacecraftCamera camera) {
        super();
        this.camera = camera;
        this.qf = camera.getRotationQuaternion();
        this.vel = camera.vel;
        aux3f1 = new Vector3();
        aux3f2 = new Vector3();
    }

    public void initialize(AssetManager assetManager) {
        // User interface
        sb = GlobalResources.spriteBatch;
        ui = new Stage(new ScreenViewport(), sb);

        indicatorw = 300;
        indicatorh = 300;

        // init gui camera
        aiCam = new PerspectiveCamera(30, indicatorw, indicatorh);
        aiCam.near = (float) AbstractCamera.CAM_NEAR;
        aiCam.far = (float) AbstractCamera.CAM_FAR;
        aiCam.up.set(0, 1, 0);
        aiCam.direction.set(0, 0, 1);
        aiCam.position.set(0, 0, 0);

        // Init AI
        dlight = new DirectionalLight();
        dlight.color.set(1f, 1f, 1f, 1f);
        dlight.setDirection(-1f, .05f, .5f);
        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f), new ColorAttribute(ColorAttribute.Specular, .5f, .5f, .5f, 1f));
        env.add(dlight);
        db = new DecalBatch(new CameraGroupStrategy(aiCam));
        mb = new ModelBatch();

        assetManager.load("data/tex/attitudeindicator-2.png", Texture.class);
        assetManager.load("img/ai-pointer.png", Texture.class);
        assetManager.load("img/controlpad.png", Texture.class);
        assetManager.load("img/ai-vel.png", Texture.class);
        assetManager.load("img/ai-antivel.png", Texture.class);

    }

    /**
     * Constructs the interface
     */
    public void doneLoading(AssetManager assetManager) {
        skin = GlobalResources.skin;

        aiTexture = assetManager.get("data/tex/attitudeindicator-2.png", Texture.class);
        aiTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        aiPointerTexture = assetManager.get("img/ai-pointer.png", Texture.class);
        aiPointerTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        controlPadTexture = assetManager.get("img/controlpad.png", Texture.class);
        controlPadTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        aiVelTex = assetManager.get("img/ai-vel.png", Texture.class);
        aiVelTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        aiAntivelTex = assetManager.get("img/ai-antivel.png", Texture.class);
        aiAntivelTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        aiVelDec = Decal.newDecal(new TextureRegion(aiVelTex));
        aiAntivelDec = Decal.newDecal(new TextureRegion(aiAntivelTex));

        Material mat = new Material(new TextureAttribute(TextureAttribute.Diffuse, aiTexture), new ColorAttribute(ColorAttribute.Specular, 0.3f, 0.3f, 0.3f, 1f));
        aiModel = new ModelBuilder2().createSphere(1, 30, 30, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        aiTransform = new Matrix4();
        aiModelInstance = new ModelInstance(aiModel, aiTransform);
        aiViewport = new ExtendViewport(indicatorw, indicatorh, aiCam);

        buildGui();

    }

    private void buildGui() {
        buttonContainer = new Container<HorizontalGroup>();
        buttonRow = new HorizontalGroup();
        buttonRow.pad(0, 50, 10, 0);
        buttonRow.space(3);
        buttonRow.setFillParent(true);
        buttonRow.align(Align.bottomLeft);

        stabilise = new OwnImageButton(skin, "rec");
        stabilise.setName("stabilise");
        stabilise.setChecked(camera.isStabilising());
        stabilise.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.POST_NOTIFICATION, "Stabilise!");
                    return true;
                }
                return false;
            }
        });
        stabilise.addListener(new TextTooltip("Stabilise the camera yaw, pitch and roll movements", skin));

        stop = new OwnImageButton(skin, "rec");
        stop.setName("stop");
        stop.setChecked(camera.isStopping());
        stop.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.POST_NOTIFICATION, "Stop!");
                    return true;
                }
                return false;
            }
        });
        stop.addListener(new TextTooltip("Stop the camera forward movement", skin));

        buttonRow.addActor(stabilise);
        buttonRow.addActor(stop);

        buttonContainer.setActor(buttonRow);

        buttonContainer.pack();

        rebuildGui();
    }

    private void rebuildGui() {
        if (ui != null) {
            ui.addActor(buttonContainer);
        }

        /** CAPTURE SCROLL FOCUS **/
        ui.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;

                    if (ie.getType() == Type.mouseMoved) {
                        Actor scrollPanelAncestor = getScrollPanelAncestor(ie.getTarget());
                        ui.setScrollFocus(scrollPanelAncestor);
                    } else if (ie.getType() == Type.touchDown) {
                        if (ie.getTarget() instanceof TextField)
                            ui.setKeyboardFocus(ie.getTarget());
                    }
                }
                return false;
            }

            private Actor getScrollPanelAncestor(Actor actor) {
                if (actor == null) {
                    return null;
                } else if (actor instanceof ScrollPane) {
                    return actor;
                } else {
                    return getScrollPanelAncestor(actor.getParent());
                }
            }

        });
    }

    @Override
    public void dispose() {
        ui.dispose();
    }

    @Override
    public void update(float dt) {
        ui.act(dt);
    }

    @Override
    public void render(int rw, int rh) {
        /** ATTITUDE INDICATOR **/
        aiViewport.setCamera(aiCam);
        aiViewport.setWorldSize(indicatorw, indicatorh);
        aiViewport.setScreenBounds(0, 0, indicatorw, indicatorh);
        aiViewport.apply();

        mb.begin(aiCam);

        aiTransform.idt();

        aiTransform.translate(0, 0, 4);
        aiTransform.rotate(qf);
        aiTransform.rotate(0, 1, 0, 90);

        mb.render(aiModelInstance, env);

        mb.end();

        // VELOCITY INDICATORS IN NAVBALL
        // velocity
        if (!vel.isZero()) {
            aux3f1.set(vel.valuesf()).nor().scl(0.54f);
            aux3f1.mul(qf);
            aux3f1.add(0, 0, 4);

            // antivelocity
            aux3f2.set(vel.valuesf()).nor().scl(-0.54f);
            aux3f2.mul(qf);
            aux3f2.add(0, 0, 4);

            aiVelDec.setPosition(aux3f1);
            aiVelDec.setScale(0.003f);
            aiVelDec.lookAt(aiCam.position, aiCam.up);

            aiAntivelDec.setPosition(aux3f2);
            aiAntivelDec.setScale(0.003f);
            aiAntivelDec.lookAt(aiCam.position, aiCam.up);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            db.add(aiVelDec);
            db.add(aiAntivelDec);
            db.flush();
        }

        aiViewport.setWorldSize(rw, rh);
        aiViewport.setScreenBounds(0, 0, rw, rh);
        aiViewport.apply();

        // ai pointer
        sb.begin();
        //spriteBatch.draw(controlPadTexture, 0, 0);
        sb.draw(aiPointerTexture, indicatorw / 2 - 16, indicatorh / 2 - 16);
        sb.end();

        /** REST OF GUI **/
        ui.draw();

    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ui.getViewport().update(width, height, true);
                rebuildGui();
            }
        });
    }

    /**
     * Removes the focus from this Gui and returns true if the focus was in the GUI, false otherwise.
     * @return true if the focus was in the GUI, false otherwise.
     */
    public boolean cancelTouchFocus() {
        if (ui.getScrollFocus() != null) {
            ui.setScrollFocus(null);
            ui.setKeyboardFocus(null);
            return true;
        }
        return false;
    }

    @Override
    public Stage getGuiStage() {
        return ui;
    }

    @Override
    public void setSceneGraph(ISceneGraph sg) {
    }

    @Override
    public void setVisibilityToggles(ComponentType[] entities, boolean[] visible) {
    }

    @Override
    public Actor findActor(String name) {
        return ui.getRoot().findActor(name);
    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

    private String txt(String key, Object... params) {
        return I18n.bundle.format(key, params);
    }
}
