package view.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AssetManager {
    private static AssetManager instance;
    
    // --- SPRITE & IMAGE VARS ---
    private BufferedImage spriteSheet;
    private BufferedImage menuBackground; 
    private BufferedImage gameBackground;
    private final int SPRITE_SIZE = 102; 
    private final Map<String, BufferedImage> spriteCache = new HashMap<>();
    
    // --- AUDIO VARS ---
    private final Map<String, Clip> soundCache = new HashMap<>();
    private Clip currentBGM;

    private AssetManager() {
        loadSprites();
        loadSounds();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    //Sprites
    private void loadSprites() {
        String[] paths = {
            "/resources/sprites.png", 
            "/sprites.png",           
            "resources/sprites.png"   
        };

        for (String path : paths) {
            try {
                URL url = getClass().getResource(path);
                if (url != null) {
                    spriteSheet = ImageIO.read(url);
                    System.out.println("✅ BERHASIL memuat sprite dari: " + path);
                    break; 
                }
            } catch (IOException e) {
                System.err.println("Gagal membaca dari: " + path);
            }
        }
        if (spriteSheet == null) System.err.println("❌ ERROR: Sprites.png tidak ditemukan!");

        try {

            URL bgUrl = getClass().getResource("/resources/bg_menu.jpg");
            if (bgUrl == null) bgUrl = getClass().getResource("/bg_menu.jpg");
            
            if (bgUrl != null) {
                menuBackground = ImageIO.read(bgUrl);
                System.out.println("✅ BERHASIL memuat background menu");
            } else {
                System.err.println("⚠️ Warning: bg_menu.jpg tidak ditemukan");
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat background menu: " + e.getMessage());
        }
        try {
            URL gameBgUrl = getClass().getResource("/resources/GameWallpaper.jpg");
            if (gameBgUrl == null) gameBgUrl = getClass().getResource("/GameWallpaper.jpg"); // Fallback

            if (gameBgUrl != null) {
                gameBackground = ImageIO.read(gameBgUrl);
                System.out.println("✅ BERHASIL memuat background game");
            } else {
                System.err.println("⚠️ Warning: GameWallpaper.png tidak ditemukan");
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat background game: " + e.getMessage());
        }
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
    
    public BufferedImage getMenuBackground() {
        return menuBackground;
    }
    
    private BufferedImage createErrorSprite() {
        BufferedImage img = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.MAGENTA); 
        g.fillRect(0, 0, SPRITE_SIZE, SPRITE_SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, SPRITE_SIZE-1, SPRITE_SIZE-1);
        g.dispose();
        return img;
    }

    public BufferedImage getSpriteSheet() {
        return spriteSheet;
    }

    //Audio
    private void loadSounds() {
        loadSound("bgm_menu", "/resources/sounds/bgm_menu.wav");
        loadSound("bgm_game", "/resources/sounds/bgm_game.wav");
        
        loadSound("chop", "/resources/sounds/sfx_chop.wav");
        loadSound("dash", "/resources/sounds/sfx_dash.wav");
        loadSound("throw", "/resources/sounds/sfx_throw.wav");
        loadSound("boil", "/resources/sounds/sfx_boil.wav");
        loadSound("fry", "/resources/sounds/sfx_fry.wav");
        loadSound("serve", "/resources/sounds/sfx_serve.wav");
        loadSound("trash", "/resources/sounds/sfx_trash.wav");
        loadSound("wash", "/resources/sounds/sfx_wash.wav");
        loadSound("spin", "/resources/sounds/sfx_spin.wav");
        loadSound("win", "/resources/sounds/sfx_win.wav");
        loadSound("pickup", "/resources/sounds/sfx_pickup.wav");
        loadSound("place", "/resources/sounds/sfx_place.wav");
    }

    private void loadSound(String name, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return;
            
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            soundCache.put(name, clip);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading sound: " + name);
        }
    }

    public void playSound(String name) {
        Clip clip = soundCache.get(name);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void playBGM(String name) {
        stopBGM();
        currentBGM = soundCache.get(name);
        if (currentBGM != null) {
            currentBGM.setFramePosition(0);
            currentBGM.loop(Clip.LOOP_CONTINUOUSLY);
            currentBGM.start();
        }
    }

    public void stopBGM() {
        if (currentBGM != null && currentBGM.isRunning()) {
            currentBGM.stop();
        }
    }

    public BufferedImage getGameBackground() {
        return gameBackground;
    }
}