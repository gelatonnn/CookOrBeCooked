package view.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AssetManager {
    private static AssetManager instance;
    private BufferedImage spriteSheet;
    private final int SPRITE_SIZE = 102; 
    private final Map<String, BufferedImage> spriteCache = new HashMap<>();

    private AssetManager() {
        loadSpriteSheet();
    }

    private void loadSpriteSheet() {
        String[] paths = {
            "/resources/sprites.png", 
            "/sprites.png",           
            "resources/sprites.png"   
        };

        for (String path : paths) {
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    spriteSheet = ImageIO.read(is);
                    System.out.println("✅ BERHASIL memuat sprite dari: " + path);
                    System.out.println("   Ukuran: " + spriteSheet.getWidth() + "x" + spriteSheet.getHeight());
                    return; 
                }
            } catch (IOException e) {
                System.err.println("Gagal membaca dari: " + path);
            }
        }

        System.err.println("ERROR: Tidak bisa menemukan sprites.png di path manapun!");
        System.err.println("Pastikan file ada di folder 'src/resources/sprites.png'");
    }

    public static AssetManager getInstance() {
        if (instance == null) instance = new AssetManager();
        return instance;
    }

    public BufferedImage getSprite(int col, int row) {
        if (spriteSheet == null) return createErrorSprite();

        String key = col + "," + row;
        if (spriteCache.containsKey(key)) return spriteCache.get(key);

        if ((col * SPRITE_SIZE) + SPRITE_SIZE > spriteSheet.getWidth() ||
            (row * SPRITE_SIZE) + SPRITE_SIZE > spriteSheet.getHeight()) {
            return createErrorSprite(); 
        }

        BufferedImage sprite = spriteSheet.getSubimage(
            col * SPRITE_SIZE, 
            row * SPRITE_SIZE, 
            SPRITE_SIZE, 
            SPRITE_SIZE
        );
        
        spriteCache.put(key, sprite);
        return sprite;
    }
    
    private BufferedImage createErrorSprite() {
        BufferedImage img = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.MAGENTA); 
        g.fillRect(0, 0, SPRITE_SIZE, SPRITE_SIZE);
        g.setColor(java.awt.Color.BLACK);
        g.drawRect(0, 0, SPRITE_SIZE-1, SPRITE_SIZE-1);
        g.dispose();
        return img;
    }
}