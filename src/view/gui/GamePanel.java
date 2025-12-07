package view.gui;

import model.chef.Chef;
import model.engine.GameEngine;
import model.world.WorldMap;
import model.world.tiles.StationTile;
import model.world.Tile;
import items.core.Item;
import stations.Station;
import utils.Position;
import view.Observer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int TILE_SIZE = 64;

    // Map untuk menyimpan posisi visual (X, Y pixel) setiap chef untuk animasi
    // Key: Chef, Value: Point.Double (Koordinat presisi tinggi)
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
        
        // Biar gambar smooth saat bergerak
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

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

                // Lantai
                g2d.drawImage(sprites.getSprite("floor"), screenX, screenY, TILE_SIZE, TILE_SIZE, null);

                // Objek / Station
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

        if (name.contains("cutting")) img = sprites.getSprite("cutting station");
        else if (name.contains("cook") || name.contains("stove")) img = sprites.getSprite("cooking station");
        else if (name.contains("wash") || name.contains("sink")) img = sprites.getSprite("washing station");
        else if (name.contains("serve")) img = sprites.getSprite("serving station");
        else if (name.contains("ingredient")) img = sprites.getSprite("ingredient storage");
        else if (name.contains("trash")) img = sprites.getSprite("trash station");
        else img = sprites.getSprite("counter");

        if (img != null) g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);

        // Gambar Item di atas Station
        Item stored = station.peek();
        if (stored != null) {
            BufferedImage itemImg = sprites.getSprite(stored.getName());
            g2d.drawImage(itemImg, x + 16, y + 16, TILE_SIZE/2, TILE_SIZE/2, null);
        }
    }

    private void drawChefs(Graphics2D g2d) {
        List<Chef> chefs = engine.getChefs();
        SpriteLibrary sprites = SpriteLibrary.getInstance();

        for (int i = 0; i < chefs.size(); i++) {
            Chef chef = chefs.get(i);
            
            // --- LOGIKA SLIDING / SMOOTH MOVEMENT ---
            
            // 1. Hitung Target Posisi (Grid -> Pixel)
            double targetX = chef.getPos().x * TILE_SIZE;
            double targetY = chef.getPos().y * TILE_SIZE;

            // 2. Ambil Posisi Render Terakhir
            if (!chefRenderPositions.containsKey(chef)) {
                // Jika belum ada (awal game), set langsung ke target
                chefRenderPositions.put(chef, new Point.Double(targetX, targetY));
            }
            Point.Double currentPos = chefRenderPositions.get(chef);

            // 3. Hitung Selisih (Jarak)
            double diffX = targetX - currentPos.x;
            double diffY = targetY - currentPos.y;

            // 4. LERP (Linear Interpolation) - Kecepatan Geser
            // Semakin besar angkanya (misal 0.5), semakin cepat/snappy
            // Semakin kecil (misal 0.1), semakin licin/lambat
            double speed = 0.25; 
            
            // Update posisi render mendekati target
            currentPos.x += diffX * speed;
            currentPos.y += diffY * speed;
            
            // Snap jika jarak sudah sangat dekat (biar pixel perfect saat berhenti)
            if (Math.abs(diffX) < 0.5) currentPos.x = targetX;
            if (Math.abs(diffY) < 0.5) currentPos.y = targetY;

            // --- END LOGIKA SLIDING ---

            int screenX = (int) currentPos.x;
            int screenY = (int) currentPos.y;

            // Pilih Sprite
            String spriteName = (i == 0) ? "chef1" : "chef2";
            BufferedImage chefImg = sprites.getSprite(spriteName);

            if (chefImg != null) {
                g2d.drawImage(chefImg, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
            }

            // Indikator Arah
            g2d.setColor(new Color(255, 255, 255, 150));
            int dirX = screenX + TILE_SIZE / 2 + (chef.getDirection().dx * 24) - 5;
            int dirY = screenY + TILE_SIZE / 2 + (chef.getDirection().dy * 24) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);

            // Item di tangan (mengikuti posisi smooth)
            Item heldItem = chef.getHeldItem();
            if (heldItem != null) {
                BufferedImage itemImg = sprites.getSprite(heldItem.getName());
                int itemSize = TILE_SIZE / 2;
                int itemX = screenX + (TILE_SIZE - itemSize) / 2;
                int itemY = screenY - (itemSize / 2); // Floating effect

                if (itemImg != null) {
                    g2d.drawImage(itemImg, itemX, itemY, itemSize, itemSize, null);
                }
            }
        }
    }

    @Override
    public void update() {
        repaint();
    }
}