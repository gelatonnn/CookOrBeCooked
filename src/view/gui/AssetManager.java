package view.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AssetManager {
    private static AssetManager instance;
    private BufferedImage spriteSheet;
    
    // Sesuaikan dengan ukuran asli per kotak di gambar Anda
    // Jika saya lihat dari gambar, sepertinya sekitar 16x16 atau 32x32 pixel per item.
    // Nanti Anda harus eksperimen angka ini jika gambarnya miring/terpotong.
    private final int SPRITE_SIZE = 16; 

    // Cache untuk menyimpan potongan gambar agar tidak dipotong berulang kali
    private Map<String, BufferedImage> spriteCache = new HashMap<>();

    private AssetManager() {
        try {
            // Memuat gambar dari folder resources
            spriteSheet = ImageIO.read(getClass().getResourceAsStream("/resources/sprites.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("GAGAL MEMUAT GAMBAR! Pastikan file ada di src/resources/sprites.jpg");
        }
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /**
     * Memotong gambar berdasarkan baris dan kolom di spritesheet.
     * col: Kolom ke-berapa (mulai dari 0, dari kiri ke kanan)
     * row: Baris ke-berapa (mulai dari 0, dari atas ke bawah)
     * w: Lebar (berapa kotak)
     * h: Tinggi (berapa kotak)
     */
    public BufferedImage getSprite(int col, int row, int w, int h) {
        String key = col + "," + row + "," + w + "," + h;
        if (spriteCache.containsKey(key)) {
            return spriteCache.get(key);
        }

        if (spriteSheet == null) return null;

        // Ambil potongan gambar (Subimage)
        BufferedImage sprite = spriteSheet.getSubimage(
                col * SPRITE_SIZE, 
                row * SPRITE_SIZE, 
                w * SPRITE_SIZE, 
                h * SPRITE_SIZE
        );
        
        spriteCache.put(key, sprite);
        return sprite;
    }
}