package view.gui;

import items.core.*;
import items.utensils.Plate;
import java.awt.*;
import java.awt.geom.Point2D; // IMPORT BARU
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameEngine;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.*;
import stations.*;
import utils.Position;
import view.Observer;

public class GamePanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int TILE_SIZE = 102;
    private final SpinOverlay spinOverlay = new SpinOverlay();
    
    private final Map<Chef, Point2D.Double> chefRenderPositions = new HashMap<>();
    
    private int animationTick = 0;
    private final float MOVEMENT_SPEED = 0.3f; 

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        int w = engine.getWorld().getWidth();
        int h = engine.getWorld().getHeight();
        
        this.setPreferredSize(new Dimension(w * TILE_SIZE, h * TILE_SIZE));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);

        model.engine.EffectManager.getInstance().setOnSpinStart(() -> {
            
            // Ambil target hasil
            model.engine.EffectManager.EffectType target = 
                model.engine.EffectManager.getInstance().getPendingEffect();
            
            // Mulai animasi
            spinOverlay.start(target, () -> {
                // Callback saat animasi selesai: Terapkan efeknya!
                model.engine.EffectManager.getInstance().applyPendingEffect(engine);
            });
        });
        
        // Memulai Timer khusus untuk animasi visual (60 FPS)
        // Ini terpisah dari GameLoop logic agar animasi tetap halus
        new Timer(16, e -> {
            updateVisuals();
            spinOverlay.update();
            repaint();
        }).start();
    }

    // Method baru untuk menghitung posisi halus (LERP) dan animasi
    private void updateVisuals() {
        // 1. Update Timer Animasi
        animationTick++;

        // 2. Update Posisi Halus (Smooth Movement)
        for (Chef c : engine.getChefs()) {
            // Posisi Target (Grid * Ukuran Tile)
            double targetX = c.getX() * TILE_SIZE;
            double targetY = c.getY() * TILE_SIZE;

            // Ambil posisi visual saat ini, atau inisialisasi jika belum ada
            if (!chefRenderPositions.containsKey(c)) {
                chefRenderPositions.put(c, new Point2D.Double(targetX, targetY));
            }
            Point2D.Double currentPos = chefRenderPositions.get(c);

            // RUMUS LERP (Linear Interpolation):
            // Posisi Baru = Posisi Lama + (Jarak ke Target * Kecepatan)
            double newX = currentPos.x + (targetX - currentPos.x) * MOVEMENT_SPEED;
            double newY = currentPos.y + (targetY - currentPos.y) * MOVEMENT_SPEED;

            // Jika jarak sangat kecil, langsung tempel (snap) biar tidak getar
            if (Math.abs(targetX - newX) < 1.0) newX = targetX;
            if (Math.abs(targetY - newY) < 1.0) newY = targetY;

            // Simpan posisi baru
            currentPos.setLocation(newX, newY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Agar gambar pixel art tetap tajam saat bergerak
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawWorld(g2d);
        drawChefs(g2d);
        spinOverlay.draw((Graphics2D)g, getWidth(), getHeight());
    }

    private void drawWorld(Graphics2D g2d) {
        WorldMap map = engine.getWorld();
        SpriteLibrary sprites = SpriteLibrary.getInstance();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.getTile(new Position(x, y));
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                //LANTAI 
                g2d.setColor(new Color(139, 69, 19));
                g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g2d.setColor(new Color(160, 82, 45));
                g2d.drawRect(px, py, TILE_SIZE, TILE_SIZE);

                //OBJEK 
                if (!tile.isWalkable()) {
                    if (tile instanceof StationTile stTile) {
                        drawStation(g2d, px, py, stTile.getStation());
                    } else {
                        // Wall
                        BufferedImage wall = sprites.getSprite("wall");
                        if (wall != null)
                            g2d.drawImage(wall, px, py, TILE_SIZE, TILE_SIZE, null);
                        else {
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }

                //ITEM DI LANTAI
                if (tile instanceof WalkableTile wt && wt.getItem() != null) {
                    drawItem(g2d, px + 25, py + 25, wt.getItem(), 50);
                }
            }
        }
    }
    
    private void drawStation(Graphics2D g2d, int x, int y, Station station) {
        String name = station.getName().toLowerCase();
        
        // --- UBAH BAGIAN INI ---
        // Panggil method helper yang baru kita buat
        BufferedImage img = getStationSprite(name, station);
        // -----------------------

        //Gambar Station
        if (img != null) {
            g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        } else {
            // Fallback jika gambar gagal dimuat
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("?", x+40, y+50);
        }

        // Gambar Text Nama Station (Optional, untuk debug)
        // g2d.setColor(Color.WHITE);
        // g2d.drawString(station.getName(), x, y);

        // Gambar Item di Atas Station
        if (!(station instanceof stations.PlateStorage)) {
            Item storedItem = station.peek();
            if (storedItem != null) {
                drawItem(g2d, x + 15, y + 10, storedItem, 70);
            }
        }
    }

    // Tambahkan method ini di bagian bawah class GamePanel
    private BufferedImage getStationSprite(String name, Station station) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();

        // --- TAMBAHAN BARU: Lucky Station ---
        if (station instanceof stations.LuckyStation) {
            // Pastikan Anda sudah punya sprite bernama "lucky_station" di sprites.png
            // Jika belum ada, gunakan fallback ke "crate" atau "counter" sementara
            BufferedImage img = sprites.getSprite("lucky_station");
            return (img != null) ? img : sprites.getSprite("crate_mystery"); 
        }
        // ------------------------------------

        if (station instanceof IngredientStorage) {
            if (name.contains("pasta")) return sprites.getSprite("crate_pasta");
            if (name.contains("meat")) return sprites.getSprite("crate_meat");
            if (name.contains("tomato")) return sprites.getSprite("crate_tomato");
            if (name.contains("shrimp")) return sprites.getSprite("crate_shrimp");
            if (name.contains("fish")) return sprites.getSprite("crate_fish");
            return sprites.getSprite("ingredient storage");
        }

        if (name.contains("cutting")) return sprites.getSprite("cutting station");
        if (name.contains("cook") || name.contains("stove")) return sprites.getSprite("cooking station");
        if (name.contains("wash") || name.contains("sink")) return sprites.getSprite("washing station");
        if (name.contains("serving")) return sprites.getSprite("serving station");
        if (name.contains("trash")) return sprites.getSprite("trash station");
        if (name.contains("plate")) return sprites.getSprite("plate storage");
        if (name.contains("assembly")) return sprites.getSprite("assembly station");

        return sprites.getSprite("counter"); // Default sprite
    }

    private void drawItem(Graphics2D g2d, int x, int y, Item item, int size) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        String spriteName = item.getName().toLowerCase();
        
        // -- Status Item --
        spriteName += switch (item.getState()) {
            case COOKED -> "_cooked";
            case BURNED -> "_burned";
            case CHOPPED -> "_chopped";
            default -> "";
        };

        boolean isCooking = false;
        switch (item) {
            case CookingDevice dev -> {
                String devClassName = dev.getClass().getSimpleName().toLowerCase();
                if (devClassName.contains("pot")) {
                    // PERUBAHAN DISINI: Cek apakah sedang masak?
                    if (dev.isCooking()) {
                        spriteName = "pot_cooking"; // Panci ada airnya
                    } else {
                        spriteName = "boiling pot"; // Panci kosong
                    }
                } else if (devClassName.contains("pan")) {
                    // Opsional: Lakukan hal yang sama untuk Frying Pan jika ada sprite "pan_cooking"
                    if (dev.isCooking()) {
                        spriteName = "pan_cooking";
                    } else {
                        spriteName = "frying pan";
                    }
                }
                isCooking = dev.isCooking();
            }
            case Plate plate -> {
                spriteName = plate.isClean() ? "plate" : "plate_dirty";
            }
            default -> {}
        }

        BufferedImage img = sprites.getSprite(spriteName);
        if (img != null) {
            g2d.drawImage(img, x, y, size, size, null);
            
            if (item instanceof Plate || item instanceof CookingDevice) {
                drawContainerContents(g2d, x, y, item);
            }

            if (isCooking) {
                drawCookingIndicator(g2d, x, y, size);
            }
        }
    }

    private void drawCookingIndicator(Graphics2D g2d, int x, int y, int size) {        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y - 5, size, 5);
        g2d.setColor(Color.RED);
        g2d.fillRect(x, y - 5, (int)(size * (System.currentTimeMillis() % 1000) / 1000.0), 7);
    }

    private void drawContainerContents(Graphics2D g2d, int x, int y, Item container) {
        List<Preparable> contents = switch (container) {
            case Plate p -> p.getContents();
            case CookingDevice c -> c.getContents();
            default -> null;
        };

        if (contents != null && !contents.isEmpty()) {
            int offsetX = 0;
            int offsetY = 0;
            int i = 0;

            for (Preparable p : contents) {
                if (p instanceof Item item) {
                    String ingName = item.getName().toLowerCase();
                    switch (item.getState()) {
                        case COOKED -> ingName += "_cooked";
                        case CHOPPED -> ingName += "_chopped";
                        case BURNED -> ingName += "_burned";
                        default -> {} 
                    }
                    
                    BufferedImage ingImg = SpriteLibrary.getInstance().getSprite(ingName);
                    
                    if (ingImg != null) {
                        g2d.drawImage(ingImg, x + 15 + offsetX, y + 10 + offsetY, 25, 25, null);
                        offsetX += 12;
                        if (i % 2 == 1) { 
                            offsetX = 5;
                            offsetY += 10;
                        }
                    }
                }
                i++;
            }
        }
    }

    private void drawChefs(Graphics2D g2d) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        List<Chef> chefs = engine.getChefs();

        for (int i = 0; i < chefs.size(); i++) {
            Chef c = chefs.get(i);
            
            // Ambil posisi visual dari Map (Interpolated Position)
            Point2D.Double renderPos = chefRenderPositions.get(c);
            
            // Fallback jika belum ada di map (misal frame pertama)
            int px = (renderPos != null) ? (int) renderPos.x : c.getX() * TILE_SIZE;
            int py = (renderPos != null) ? (int) renderPos.y : c.getY() * TILE_SIZE;

            // Hitung kecepatan animasi
            // Jika sedang bergerak (jarak visual vs logic jauh), animasi cepat
            // Jika diam, animasi lambat
            boolean isMoving = false;
            if (renderPos != null) {
                double dist = Math.abs(renderPos.x - c.getX() * TILE_SIZE) + Math.abs(renderPos.y - c.getY() * TILE_SIZE);
                isMoving = dist > 5.0; 
            }
            
            // step dibagi 10 biar tidak terlalu cepat kedipnya
            // Jika bergerak, speed animasi lebih cepat (dibagi 5)
            int step = animationTick / (isMoving ? 5 : 15); 

            BufferedImage chefImg = sprites.getChefSprite(
                i, 
                c.getDirection().name(), 
                c.getHeldItem() != null,
                c.isBusy(),
                step // Parameter baru
            );

            // Gambar Chef
            if (chefImg != null) {
                g2d.drawImage(chefImg, px, py, TILE_SIZE, TILE_SIZE, null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(px, py, TILE_SIZE, TILE_SIZE);
            }

            // Indikator Arah (Opsional, transparan)
            g2d.setColor(new Color(255, 255, 255, 50));
            int dirX = px + TILE_SIZE / 2 + (c.getDirection().dx * 35) - 5;
            int dirY = py + TILE_SIZE / 2 + (c.getDirection().dy * 35) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);

            // Draw Held Item (Item melayang mengikuti posisi halus chef)
            if (c.getHeldItem() != null) {
                // Pastikan method drawItem ada atau copy ulang dari kode lama
                drawItem(g2d, px + 25, py - 20, c.getHeldItem(), 50); 
            }

            // Draw Progress Bar (Misal sedang memotong)
            if (c.getActionProgress() > 0) {
                drawProgressBar(g2d, px, py - 10, c.getActionProgress(), Color.GREEN);
            }
        }
    }

    private void drawProgressBar(Graphics2D g2d, int x, int y, float percentage, Color color) {
        int barWidth = TILE_SIZE - 20;
        int barHeight = 8;
        int screenX = x + 10; // Center bar horizontally

        // Background Bar (Hitam/Abu)
        g2d.setColor(Color.BLACK);
        g2d.fillRect(screenX, y, barWidth, barHeight);
        
        // Foreground Bar (Progress)
        g2d.setColor(color);
        g2d.fillRect(screenX + 1, y + 1, (int)((barWidth - 2) * percentage), barHeight - 2);
    }

    @Override
    public void update() {
        repaint();
    }
}