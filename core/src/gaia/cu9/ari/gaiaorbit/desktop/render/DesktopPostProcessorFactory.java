package gaia.cu9.ari.gaiaorbit.desktop.render;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;

public class DesktopPostProcessorFactory extends PostProcessorFactory {
	DesktopPostProcessor instance = null;

    @Override
    public IPostProcessor getPostProcessor() {
    	if(instance == null){
    		instance = new DesktopPostProcessor();
    	}
        return instance;
    }

}
