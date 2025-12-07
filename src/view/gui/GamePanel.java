package view.gui;

import items.core.CookingDevice;
import items.core.Item;
import items.core.ItemState;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameEngine;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.StationTile;
import stations.Station;
import utils.Position;
import view.Observer;

public class GamePanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int TILE_SIZE = 64;

    // Map untuk menyimpan posisi visual (X, Y pixel) setiap chef untuk animasi smooth
    private final Map<Chef, Point.Double> chefRenderPositions = new HashMap<>();

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        int mapWidth = engine.getWorld().getWidth();
        int mapHeight = engine.getWorld().getHeight();
        this.setPreferredSize(new Dimension(mapWidth * TILE_SIZE, mapHeight * TILE_SIZE));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Rendering Hint untuk hasil gambar lebih halus saat scaling/gerak
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawMap(g2d);
        drawChefs(g2d);
    }

    private void drawMap(Graphics2D g2d) {
        WorldMap map = engine.getWorld();
        SpriteLibrary sprites = SpriteLibrary.getInstance();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.getTile(new Position(x, y));
                int screenX = x * TILE_SIZE;
                int screenY = y * TILE_SIZE;

                // 1. Lantai
                g2d.drawImage(sprites.getSprite("floor"), screenX, screenY, TILE_SIZE, TILE_SIZE, null);

                // 2. Objek / Station / Tembok
                if (!tile.isWalkable()) {
                    if (tile instanceof StationTile st) {
                        drawStation(g2d, screenX, screenY, st.getStation());
                    } else {
                        g2d.drawImage(sprites.getSprite("wall"), screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                    }
                }
            }
        }
    }

    private void drawStation(Graphics2D g2d, int x, int y, Station station) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        String name = station.getName().toLowerCase();
        BufferedImage img;

        // Pemilihan Sprite Station
        if (name.contains("cutting")) img = sprites.getSprite("cutting station");
        else if (name.contains("cook") || name.contains("stove")) img = sprites.getSprite("cooking station");
        else if (name.contains("wash") || name.contains("sink")) img = sprites.getSprite("washing station");
        else if (name.contains("serve")) img = sprites.getSprite("serving station");
        else if (name.contains("ingredient")) img = sprites.getSprite("ingredient storage");
        else if (name.contains("trash")) img = sprites.getSprite("trash station");
        else if (name.contains("plate") && name.contains("storage")) img = sprites.getSprite("plate storage");
        else img = sprites.getSprite("counter"); // Default

        if (img != null) {
            g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        }

        // Gambar Item yang ditaruh di atas Station
        Item stored = station.peek();
        if (stored != null) {
            String spriteName = determineSpriteName(stored); // Helper method untuk status masak
            BufferedImage itemImg = sprites.getSprite(spriteName);
            
            if (itemImg != null) {
                // Gambar item agak besar di tengah station
                g2d.drawImage(itemImg, x + 12, y + 12, 40, 40, null);
            }
            
            // Khusus Piring: Gambar juga isinya (Ingredients)
            drawPlateContents(g2d, stored, x, y);
        }
    }

    private void drawChefs(Graphics2D g2d) {
        List<Chef> chefs = engine.getChefs();
        SpriteLibrary sprites = SpriteLibrary.getInstance();

        for (int i = 0; i < chefs.size(); i++) {
            Chef chef = chefs.get(i);
            
            // --- LOGIKA SLIDING / SMOOTH MOVEMENT ---
            double targetX = chef.getPos().x * TILE_SIZE;
            double targetY = chef.getPos().y * TILE_SIZE;

            if (!chefRenderPositions.containsKey(chef)) {
                chefRenderPositions.put(chef, new Point.Double(targetX, targetY));
            }
            Point.Double currentPos = chefRenderPositions.get(chef);

            double diffX = targetX - currentPos.x;
            double diffY = targetY - currentPos.y;
            double speed = 0.25; // Kecepatan interpolasi

            currentPos.x += diffX * speed;
            currentPos.y += diffY * speed;
            
            if (Math.abs(diffX) < 0.5) currentPos.x = targetX;
            if (Math.abs(diffY) < 0.5) currentPos.y = targetY;

            int screenX = (int) currentPos.x;
            int screenY = (int) currentPos.y;
            // ----------------------------------------

            // Gambar Chef
            String chefSprite = (i == 0) ? "chef1" : "chef2";
            BufferedImage chefImg = sprites.getSprite(chefSprite);

            if (chefImg != null) {
                g2d.drawImage(chefImg, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
            }

            // Indikator Arah
            g2d.setColor(new Color(255, 255, 255, 150));
            int dirX = screenX + TILE_SIZE / 2 + (chef.getDirection().dx * 24) - 5;
            int dirY = screenY + TILE_SIZE / 2 + (chef.getDirection().dy * 24) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);

            // Item di Tangan Chef
            Item heldItem = chef.getHeldItem();
            if (heldItem != null) {
                String spriteName = determineSpriteName(heldItem); // Cek status masak
                BufferedImage itemImg = sprites.getSprite(spriteName);
                
                if (itemImg != null) {
                    int itemSize = TILE_SIZE / 2;
                    int itemX = screenX + (TILE_SIZE - itemSize) / 2;
                    int itemY = screenY - (itemSize / 2); // Floating effect di atas kepala

                    g2d.drawImage(itemImg, itemX, itemY, itemSize, itemSize, null);
                    
                    // Jika chef bawa piring, gambar isinya juga (kecil)
                    drawPlateContents(g2d, heldItem, itemX - 10, itemY - 10); 
                }
            }
        }
    }

    // --- HELPER METHODS ---

    /**
     * Menentukan nama sprite berdasarkan status item (Burned/Cooked/Cooking)
     */
    private String determineSpriteName(Item item) {
        String baseName = item.getName().toLowerCase();

        if (item instanceof CookingDevice device) {
            boolean isBurned = false;
            boolean isCooked = false;
            
            // Cek isi alat masak
            if (!device.getContents().isEmpty()) {
                if (device.getContents().get(0) instanceof Item i) {
                     if (i.getState() == ItemState.BURNED) isBurned = true;
                     else if (i.getState() == ItemState.COOKED) isCooked = true;
                }
            }

            if (isBurned) return baseName + " burned";
            if (device.isCooking()) return baseName + " cooking";
            if (isCooked) return baseName + " cooked";
        }

        return baseName;
    }

    /**
     * Menggambar bahan-bahan kecil di atas piring
     */
    private void drawPlateContents(Graphics2D g2d, Item item, int x, int y) {
        if (item instanceof items.utensils.Plate plate) {
            SpriteLibrary sprites = SpriteLibrary.getInstance();
            List<items.core.Preparable> contents = plate.getContents();
            int offsetX = 0;
            
            for (items.core.Preparable p : contents) {
                if (p instanceof Item i) {
                    BufferedImage ingImg = sprites.getSprite(i.getName());
                    if (ingImg != null) {
                        // Gambar mini icon bahan
                        g2d.drawImage(ingImg, x + 10 + offsetX, y + 10, 20, 20, null);
                        offsetX += 10;
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        repaint();
    }
}