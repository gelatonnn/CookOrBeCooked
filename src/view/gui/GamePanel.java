package view.gui;

import items.core.*;
import items.utensils.Plate;
import java.awt.*;
import java.awt.geom.Point2D;
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
    private final int TILE_SIZE = 60;
    private final SpinOverlay spinOverlay = new SpinOverlay();

    // We can remove chefRenderPositions since we now have exact positions in the model!

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        int w = engine.getWorld().getWidth();
        int h = engine.getWorld().getHeight();

        this.setPreferredSize(new Dimension(w * TILE_SIZE, h * TILE_SIZE));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);

        model.engine.EffectManager.getInstance().setOnSpinStart(() -> {
            model.engine.EffectManager.EffectType target =
                    model.engine.EffectManager.getInstance().getPendingEffect();
            spinOverlay.start(target, () -> {
                model.engine.EffectManager.getInstance().applyPendingEffect(engine);
            });
        });

        // Render loop
        new Timer(16, e -> {
            spinOverlay.update();
            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawWorld(g2d);
        drawChefs(g2d);
        drawProjectiles(g2d); // NEW: Draw flying items
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

                g2d.setColor(new Color(139, 69, 19));
                g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g2d.setColor(new Color(160, 82, 45));
                g2d.drawRect(px, py, TILE_SIZE, TILE_SIZE);

                if (!tile.isWalkable()) {
                    if (tile instanceof StationTile stTile) {
                        drawStation(g2d, px, py, stTile.getStation());
                    } else {
                        BufferedImage wall = sprites.getSprite("wall");
                        if (wall != null)
                            g2d.drawImage(wall, px, py, TILE_SIZE, TILE_SIZE, null);
                        else {
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }

                if (tile instanceof WalkableTile wt && wt.getItem() != null) {
                    drawItem(g2d, px + 25, py + 25, wt.getItem(), 50); // Floor item
                }
            }
        }
    }

    private void drawStation(Graphics2D g2d, int x, int y, Station station) {
        String name = station.getName().toLowerCase();
        BufferedImage img = getStationSprite(name, station);

        if (img != null) {
            g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        }

        if (!(station instanceof stations.PlateStorage)) {
            Item storedItem = station.peek();
            if (storedItem != null) {
                int itemSize = (int)(TILE_SIZE * 0.6);
                int offset = (TILE_SIZE - itemSize) / 2;
                drawItem(g2d, x + offset, y + offset, storedItem, itemSize);
            }
        }
    }

    private BufferedImage getStationSprite(String name, Station station) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        if (station instanceof stations.LuckyStation) {
            BufferedImage img = sprites.getSprite("lucky_station");
            return (img != null) ? img : sprites.getSprite("crate_mystery");
        }
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
        return sprites.getSprite("counter");
    }

    private void drawItem(Graphics2D g2d, int x, int y, Item item, int size) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        String spriteName = item.getName().toLowerCase();

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
                    spriteName = dev.isCooking() ? "pot_cooking" : "boiling pot";
                } else if (devClassName.contains("pan")) {
                    spriteName = dev.isCooking() ? "pan_cooking" : "frying pan";
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
                drawContainerContents(g2d, x, y, item, size);
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

    private void drawContainerContents(Graphics2D g2d, int x, int y, Item container, int parentSize) {
        List<Preparable> contents = switch (container) {
            case Plate p -> p.getContents();
            case CookingDevice c -> c.getContents();
            default -> null;
        };
        if (contents != null && !contents.isEmpty()) {
            int offsetX = 0; int offsetY = 0; int i = 0;
            int ingSize = (int)(parentSize * 0.45);
            for (Preparable p : contents) {
                if (p instanceof Item item) {
                    BufferedImage ingImg = SpriteLibrary.getInstance().getSprite(item.getName().toLowerCase());
                    if (ingImg != null) {
                        int padding = (int)(parentSize * 0.2);
                        g2d.drawImage(ingImg, x + padding + offsetX, y + padding + offsetY, ingSize, ingSize, null);
                        offsetX += (ingSize / 2);
                        if (i % 2 == 1) { offsetX = 5; offsetY += (ingSize / 2); }
                    }
                }
                i++;
            }
        }
    }

    // NEW: Draw Flying Projectiles
    private void drawProjectiles(Graphics2D g2d) {
        List<GameEngine.Projectile> projectiles = engine.getProjectiles();
        for (GameEngine.Projectile p : projectiles) {
            int x = (int) (p.getX() * TILE_SIZE);
            int y = (int) (p.getY() * TILE_SIZE);
            int size = (int) (TILE_SIZE * 0.5); // Slightly smaller in air

            // Add shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillOval(x + 10, y + 40, size, size/3);

            // Draw item with a slight arc offset could be nice, but simple linear is fine
            drawItem(g2d, x + (TILE_SIZE-size)/2, y + (TILE_SIZE-size)/2 - 10, p.getItem(), size);
        }
    }

    private void drawChefs(Graphics2D g2d) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        List<Chef> chefs = engine.getChefs();

        for (int i = 0; i < chefs.size(); i++) {
            Chef c = chefs.get(i);

            // USE EXACT PIXEL COORDINATES from Model
            int px = (int) (c.getExactX() * TILE_SIZE);
            int py = (int) (c.getExactY() * TILE_SIZE);

            int step = (int)(System.currentTimeMillis() / 200) % 2; // Simple animation loop

            BufferedImage chefImg = sprites.getChefSprite(
                    i,
                    c.getDirection().name(),
                    c.getHeldItem() != null,
                    c.isBusy(),
                    step
            );

            if (chefImg != null) {
                g2d.drawImage(chefImg, px, py, TILE_SIZE, TILE_SIZE, null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(px, py, TILE_SIZE, TILE_SIZE);
            }

            g2d.setColor(new Color(255, 255, 255, 50));
            // Adjust direction indicator logic to use new Double positions logic roughly
            // Actually it just needs px/py
            int dirX = px + TILE_SIZE / 2 + (c.getDirection().dx * (TILE_SIZE/3)) - 5;
            int dirY = py + TILE_SIZE / 2 + (c.getDirection().dy * (TILE_SIZE/3)) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);

            if (c.getHeldItem() != null) {
                int itemSize = (int)(TILE_SIZE * 0.5);
                int itemX = px + (TILE_SIZE - itemSize) / 2;
                int itemY = py - (itemSize / 2);
                drawItem(g2d, itemX, itemY, c.getHeldItem(), itemSize);
            }

            if (c.getActionProgress() > 0) {
                drawProgressBar(g2d, px, py - 10, c.getActionProgress(), Color.GREEN);
            }
        }
    }

    private void drawProgressBar(Graphics2D g2d, int x, int y, float percentage, Color color) {
        int barWidth = TILE_SIZE - 20;
        int barHeight = 8;
        int screenX = x + 10;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(screenX, y, barWidth, barHeight);
        g2d.setColor(color);
        g2d.fillRect(screenX + 1, y + 1, (int)((barWidth - 2) * percentage), barHeight - 2);
    }

    @Override
    public void update() {
        // No longer relying on this for render loop, but kept for logic events if needed
    }
}