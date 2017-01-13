package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.override.Vector3Attribute;

public class ModelComponent implements Disposable {
    private static ColorAttribute ambient;

    static {
        ambient = new ColorAttribute(ColorAttribute.AmbientLight, GlobalConf.scene.AMBIENT_LIGHT, GlobalConf.scene.AMBIENT_LIGHT, GlobalConf.scene.AMBIENT_LIGHT, 1f);
    }

    public static void toggleAmbientLight(boolean on) {
        if (on) {
            ambient.color.set(.7f, .7f, .7f, 1f);
        } else {
            ambient.color.set(GlobalConf.scene.AMBIENT_LIGHT, GlobalConf.scene.AMBIENT_LIGHT, GlobalConf.scene.AMBIENT_LIGHT, 1f);
        }
    }

    /**
     * Sets the ambient light
     * 
     * @param level
     *            Ambient light level between 0 and 1
     */
    public static void setAmbientLight(float level) {
        ambient.color.set(level, level, level, 1f);
    }

    public ModelInstance instance;
    public Environment env;
    /** Directional light **/
    public DirectionalLight dlight;

    public Map<String, Object> params;

    public String type, modelFile;

    public double scale = 1d;
    public boolean culling = true;
    /**
     * COMPONENTS
     */
    // Texture
    public TextureComponent tc;

    public ModelComponent() {
        this(true);
    }

    public ModelComponent(Boolean initEnvironment) {
        if (initEnvironment) {
            env = new Environment();
            env.set(ambient);
            // Direction from Sun to Earth
            dlight = new DirectionalLight();
            dlight.color.set(1f, 1f, 1f, 0f);
            env.add(dlight);
        }
    }

    public void initialize() {
        if (modelFile != null && Gdx.files.internal(modelFile).exists()) {
            AssetBean.addAsset(modelFile, Model.class);
        }

        if (tc != null) {
            tc.initialize();
        }
    }

    public void doneLoading(AssetManager manager, Matrix4 localTransform, float[] cc) {
        Model model = null;
        Map<String, Material> materials = null;

        if (modelFile != null && manager.isLoaded(modelFile)) {
            // Model comes from file (probably .obj or .g3db)
            model = manager.get(modelFile, Model.class);
            materials = new HashMap<String, Material>();
            if (model.materials.size == 0) {
                Material material = new Material();
                model.materials.add(material);
                materials.put("base", material);
            } else {
                materials.put("base", model.materials.first());
            }
        } else if (type != null) {
            // We create the model
            Pair<Model, Map<String, Material>> pair = ModelCache.cache.getModel(type, params, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
            model = pair.getFirst();
            materials = pair.getSecond();
        } else {
            // Data error!
            Logger.error(new RuntimeException("The 'model' element must contain either a 'type' or a 'model' attribute"));
        }
        // Clear base material
        materials.get("base").clear();

        // INITIALIZE MATERIAL
        if (tc != null) {
            tc.initMaterial(manager, materials, cc, culling);
        }

        // CREATE MAIN MODEL INSTANCE
        instance = new ModelInstance(model, localTransform);
    }

    public void addDirectionalLight(float r, float g, float b, float x, float y, float z) {
        DirectionalLight dl = new DirectionalLight();
        dl.set(r, g, b, x, y, z);
        env.add(dl);
    }

    public void dispose() {
        if (instance != null && instance.model != null)
            instance.model.dispose();
    }
    
    public void setTransparency(float alpha) {
        if (instance != null) {
            for (int i = 0; i < instance.materials.size; i++) {
                Material mat = instance.materials.get(i);
                BlendingAttribute ba = null;
                if (mat.has(BlendingAttribute.Type)) {
                    ba = (BlendingAttribute) mat.get(BlendingAttribute.Type);
                } else {
                    ba = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    mat.set(ba);
                }
                ba.opacity = alpha;
            }
        }
    }

    public void setTransparencyColor(float alpha) {
        if (instance != null) {
            ((ColorAttribute) instance.materials.get(0).get(ColorAttribute.Diffuse)).color.a = alpha;
        }
    }

    /**
     * Sets the type of the model to construct.
     * 
     * @param type
     *            The type. Currently supported types are
     *            sphere|cylinder|ring|disc.
     */
    public void setType(String type) {
        this.type = type;
    }

    public void setTexture(TextureComponent tc) {
        this.tc = tc;
    }

    /**
     * Sets the model file path (this must be a .g3db, .g3dj or .obj).
     * 
     * @param model
     */
    public void setModel(String model) {
        this.modelFile = model;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public void setScale(Long scale) {
        this.scale = scale;
    }

    public void setCulling(Boolean culling) {
        this.culling = culling;
    }

}
