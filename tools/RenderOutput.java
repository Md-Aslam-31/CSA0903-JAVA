import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Renders an unmodified captured stdout transcript as a readable PNG evidence image. */
public final class RenderOutput {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: RenderOutput input.txt output.png");
        String[] lines = Files.readString(Path.of(args[0])).split("\\R", -1);
        Font font = new Font("Consolas", Font.PLAIN, 18);
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D mg = measure.createGraphics();
        mg.setFont(font);
        FontMetrics fm = mg.getFontMetrics();
        int width = 48;
        for (String line : lines) width = Math.max(width, fm.stringWidth(line));
        int lineHeight = fm.getHeight() + 5;
        mg.dispose();
        int imageWidth = width + 48;
        int imageHeight = lineHeight * lines.length + 48;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(24, 24, 24));
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setFont(font);
        g.setColor(new Color(220, 220, 220));
        for (int i = 0; i < lines.length; i++) g.drawString(lines[i], 24, 28 + fm.getAscent() + i * lineHeight);
        g.dispose();
        Path target = Path.of(args[1]);
        Files.createDirectories(target.getParent());
        ImageIO.write(image, "png", target.toFile());
    }
}
