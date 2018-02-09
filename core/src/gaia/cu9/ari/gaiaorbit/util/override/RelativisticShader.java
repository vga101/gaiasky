package gaia.cu9.ari.gaiaorbit.util.override;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class RelativisticShader extends DefaultShader {

    public static class Inputs extends DefaultShader.Inputs {
        public final static Uniform vc = new Uniform("u_vc");
        public final static Uniform velDir = new Uniform("u_velDir");
    }

    public static class Setters extends DefaultShader.Setters {
        public final static Setter vc = new Setter() {
            @Override
            public boolean isGlobal(BaseShader shader, int inputID) {
                return false;
            }

            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                if (combinedAttributes.has(RelativisticEffectFloatAttribute.Vc))
                    shader.set(inputID, ((RelativisticEffectFloatAttribute) (combinedAttributes.get(RelativisticEffectFloatAttribute.Vc))).value);
            }
        };

        public final static Setter velDir = new Setter() {
            @Override
            public boolean isGlobal(BaseShader shader, int inputID) {
                return false;
            }

            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                if (combinedAttributes.has(Vector3Attribute.VelDir))
                    shader.set(inputID, ((Vector3Attribute) (combinedAttributes.get(Vector3Attribute.VelDir))).value);
            }
        };

    }

    // Material uniforms
    public final int u_vc;
    public final int u_velDir;

    public RelativisticShader(final Renderable renderable) {
        this(renderable, new Config());
    }

    public RelativisticShader(final Renderable renderable, final Config config) {
        this(renderable, config, createPrefix(renderable, config));
    }

    public RelativisticShader(final Renderable renderable, final Config config, final String prefix) {
        this(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(), config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
    }

    public RelativisticShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader, final String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    public RelativisticShader(final Renderable renderable, final Config config, final ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        u_vc = register(Inputs.vc, Setters.vc);
        u_velDir = register(Inputs.velDir, Setters.velDir);

    }

    public static String createPrefix(final Renderable renderable, final Config config) {
        String prefix = DefaultShader.createPrefix(renderable, config);
        final long mask = renderable.material.getMask();
        // Atmosphere ground only if camera height is set
        if ((mask & RelativisticEffectFloatAttribute.Vc) == RelativisticEffectFloatAttribute.Vc)
            prefix += "#define relativisticEffects\n";
        return prefix;
    }
}
