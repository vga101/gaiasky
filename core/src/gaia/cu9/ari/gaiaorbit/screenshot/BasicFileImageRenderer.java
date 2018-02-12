package gaia.cu9.ari.gaiaorbit.screenshot;

import gaia.cu9.ari.gaiaorbit.screenshot.ImageRenderer.ImageType;

/**
 * Renders image files synchronously.
 * 
 * @author Toni Sagrista
 *
 */
public class BasicFileImageRenderer implements IFileImageRenderer {

    @Override
    public String saveScreenshot(String absoluteLocation, String baseFileName, int w, int h, boolean immediate, ImageType type) {
        return ImageRenderer.renderToImageGl20(absoluteLocation, baseFileName, w, h, type);
    }

    @Override
    public void flush() {
        // Nothing to do
    }

}
