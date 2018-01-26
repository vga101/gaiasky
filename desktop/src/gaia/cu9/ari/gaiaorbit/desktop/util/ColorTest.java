package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.badlogic.gdx.math.MathUtils;

import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;

/**
 * Tests the color conversion tools
 * 
 * @author tsagrista
 *
 */
public class ColorTest {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter BP: ");
        float bp = readFloat(sc);
        System.out.print("Enter RP: ");
        float rp = readFloat(sc);

        float colorxp = bp - rp;
        double teff = Math.pow(10, 3.999 - 0.654 * colorxp + 0.709 * Math.pow(colorxp, 2) - 0.316 * Math.pow(colorxp, 3));

        //        System.out.print("Enter the color in kelvin (1000-50000): ");
        //        int teff = readInteger(sc);

        float[] rgb = ColourUtils.teffToRGB(teff);

        int ri = Math.round(rgb[0] * 255);
        int gi = Math.round(rgb[1] * 255);
        int bi = Math.round(rgb[2] * 255);
        String colorhex = String.format("#%02x%02x%02x", ri, gi, bi);
        System.out.println("Teff: " + teff + " K");
        System.out.println("Color: " + colorhex);
        System.out.println("r: " + rgb[0] + " g: " + rgb[1] + " b: " + rgb[2]);

        // Display color
        JFrame f = new JFrame("Display Colours");
        f.getContentPane().add(new ColorDisplay(rgb));
        f.setSize(300, 300);
        f.setLocation(100, 100);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);

    }

    private static float readFloat(Scanner sc) {
        try {
            float val = sc.nextFloat();
            return val;
        } catch (Exception e) {
            System.err.println("Input is not a valid float");
            System.exit(1);
        }
        return 0;
    }

    private static int readInteger(Scanner sc) {
        try {
            int val = sc.nextInt();
            MathUtils.clamp(val, 1000, 50000);
            return val;
        } catch (Exception e) {
            System.err.println("Input is not a valid integer, try again");
            System.exit(1);
        }
        return 0;
    }

    public static class ColorDisplay extends JComponent {
        private static final long serialVersionUID = 1L;
        BufferedImage image;
        Color color;

        public ColorDisplay(float[] rgb) {
            super();
            this.color = new Color(rgb[0], rgb[1], rgb[2]);
        }

        public void initialize() {
            int width = getSize().width;
            int height = getSize().height;
            int[] data = new int[width * height];
            int index = 0;

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    data[index++] = this.color.getRGB();
                }
            }
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, data, 0, width);
        }

        public void paint(Graphics g) {
            if (image == null)
                initialize();
            g.drawImage(image, 0, 0, this);
        }
    }

}
