import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * One-off generator for the 6 upgrader item textures (programmer art placeholders,
 * replace with real art any time). Run: java GenTextures.java <outputDir>
 * Icon: dark device frame with an up-arrow tinted in the Powah tier color
 * (colors taken from owmii.powah.block.Tier).
 */
public class GenTextures {
    // tier name -> Powah tier color (ARGB ignored, RGB used)
    private static final Map<String, Integer> TIERS = new LinkedHashMap<>() {{
        put("basic", 0xA3AB9F);
        put("hardened", 0xBBA993);
        put("blazing", 0xE4B040);
        put("niotic", 0x13EED2);
        put("spirited", 0xAFE241);
        put("nitro", 0xD7746C);
    }};

    private static final int FRAME = 0xFF2B2B36;      // dark frame
    private static final int BODY = 0xFF47475A;        // panel
    private static final int BODY_LIGHT = 0xFF58586E;  // panel highlight

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args[0]);
        Files.createDirectories(outDir);
        for (var entry : TIERS.entrySet()) {
            BufferedImage img = draw(entry.getValue());
            Path out = outDir.resolve("upgrader_" + entry.getKey() + ".png");
            ImageIO.write(img, "PNG", out.toFile());
            System.out.println("wrote " + out);
        }
    }

    private static BufferedImage draw(int tierRgb) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int tier = 0xFF000000 | tierRgb;
        int tierDark = 0xFF000000 | scale(tierRgb, 0.55);
        int tierLight = 0xFF000000 | lighten(tierRgb, 0.35);

        // Frame + panel
        for (int y = 1; y <= 14; y++) {
            for (int x = 1; x <= 14; x++) {
                boolean border = x == 1 || x == 14 || y == 1 || y == 14;
                img.setRGB(x, y, border ? FRAME : (y <= 7 ? BODY_LIGHT : BODY));
            }
        }
        // Bottom band in the tier color (tier identity stripe)
        for (int y = 12; y <= 13; y++) {
            for (int x = 2; x <= 13; x++) {
                img.setRGB(x, y, y == 12 ? tier : tierDark);
            }
        }
        // Up arrow (tip at y=3, shaft down to y=11)
        arrow(img, 3, new int[]{7, 8}, tierLight);
        arrow(img, 4, new int[]{6, 7, 8, 9}, tier);
        arrow(img, 5, new int[]{5, 6, 7, 8, 9, 10}, tier);
        for (int y = 6; y <= 11; y++) {
            arrow(img, y, new int[]{7, 8}, y == 6 ? tier : tierDark);
        }
        return img;
    }

    private static void arrow(BufferedImage img, int y, int[] xs, int color) {
        for (int x : xs) {
            img.setRGB(x, y, color);
        }
    }

    private static int scale(int rgb, double factor) {
        int r = (int) (((rgb >> 16) & 0xFF) * factor);
        int g = (int) (((rgb >> 8) & 0xFF) * factor);
        int b = (int) ((rgb & 0xFF) * factor);
        return (r << 16) | (g << 8) | b;
    }

    private static int lighten(int rgb, double amount) {
        int r = (int) (((rgb >> 16) & 0xFF) + (255 - ((rgb >> 16) & 0xFF)) * amount);
        int g = (int) (((rgb >> 8) & 0xFF) + (255 - ((rgb >> 8) & 0xFF)) * amount);
        int b = (int) ((rgb & 0xFF) + (255 - (rgb & 0xFF)) * amount);
        return (r << 16) | (g << 8) | b;
    }
}
