package view.gui;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;
import items.utensils.Plate;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameEngine;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.StationTile;
import model.world.tiles.WalkableTile;
import stations.IngredientStorage;
import stations.Station;
import utils.Position;
import view.Observer;

public class GamePanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int TILE_SIZE = 102; 

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        int w = engine.getWorld().getWidth();
        int h = engine.getWorld().getHeight();
        
        this.setPreferredSize(new Dimension(w * TILE_SIZE, h * TILE_SIZE));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawWorld(g2d);
        drawChefs(g2d);
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
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        String name = station.getName().toLowerCase();
        BufferedImage img = null;
        
        if (station instanceof IngredientStorage) {
            if (name.contains("pasta"))
                img = sprites.getSprite("crate_pasta");
            else if (name.contains("meat"))
                img = sprites.getSprite("crate_meat");
            else if (name.contains("tomato"))
                img = sprites.getSprite("crate_tomato");
            else if (name.contains("shrimp"))
                img = sprites.getSprite("crate_shrimp");
            else if (name.contains("fish"))
                img = sprites.getSprite("crate_fish");
            else
                img = sprites.getSprite("ingredient storage");
        } else {
            if (name.contains("cutting"))
                img = sprites.getSprite("cutting station");
            else if (name.contains("cook") || name.contains("stove"))
                img = sprites.getSprite("cooking station");
            else if (name.contains("wash") || name.contains("sink"))
                img = sprites.getSprite("washing station");
            else if (name.contains("serving"))
                img = sprites.getSprite("serving station");
            else if (name.contains("trash"))
                img = sprites.getSprite("trash station");
            else if (name.contains("plate"))
                img = sprites.getSprite("plate storage");
            else if (name.contains("assembly"))
                img = sprites.getSprite("assembly station");
            else img = sprites.getSprite("counter");
        }

        //Gambar Station
        if (img != null) {
            g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(station.getName().substring(0, Math.min(3, station.getName().length())), x+10, y+50);
        }

        //Stored Itemn
        if (!(station instanceof stations.PlateStorage)) {
            Item storedItem = station.peek();
            if (storedItem != null) {
                drawItem(g2d, x + 15, y + 10, storedItem, 70);
            }
        }
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
            BufferedImage chefImg = sprites.getChefSprite(i, c.getDirection().name(), c.getHeldItem() != null,
                    c.isBusy());

            int px = c.getX() * TILE_SIZE;
            int py = c.getY() * TILE_SIZE;

            if (chefImg != null) {
                g2d.drawImage(chefImg, px, py, TILE_SIZE, TILE_SIZE, null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(px, py, TILE_SIZE, TILE_SIZE);
            }

            g2d.setColor(new Color(255, 255, 255, 150));
            int dirX = px + TILE_SIZE / 2 + (c.getDirection().dx * 35) - 5;
            int dirY = py + TILE_SIZE / 2 + (c.getDirection().dy * 35) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);

            if (c.getHeldItem() != null) {
                drawItem(g2d, px + 25, py - 20, c.getHeldItem(), 50);
            }

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