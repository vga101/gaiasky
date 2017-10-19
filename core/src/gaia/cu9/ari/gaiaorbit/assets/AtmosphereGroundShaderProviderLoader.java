package gaia.cu9.ari.gaiaorbit.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.util.override.GroundShaderProvider;

public class AtmosphereGroundShaderProviderLoader<T extends AtmosphereGroundShaderProviderLoader.AtmosphereGroundShaderProviderParameter> extends AsynchronousAssetLoader<GroundShaderProvider, T> {

    GroundShaderProvider shaderProvider;

    public AtmosphereGroundShaderProviderLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, T parameter) {
        shaderProvider = new GroundShaderProvider(Gdx.files.internal(parameter.vertexShader), Gdx.files.internal(parameter.fragmentShader));
    }

    @Override
    public GroundShaderProvider loadSync(AssetManager manager, String fileName, FileHandle file, AtmosphereGroundShaderProviderParameter parameter) {
        return shaderProvider;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AtmosphereGroundShaderProviderParameter parameter) {
        return null;
    }

    public static class AtmosphereGroundShaderProviderParameter extends AssetLoaderParameters<GroundShaderProvider> {
        String vertexShader;
        String fragmentShader;

        public AtmosphereGroundShaderProviderParameter(String vertexShader, String fragmentShader) {
            super();
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

    }

}
