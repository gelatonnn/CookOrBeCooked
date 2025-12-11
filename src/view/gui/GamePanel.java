package view.gui;

import items.core.*;
import items.utensils.Plate;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameEngine;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.*;
import stations.Station;
import utils.Position;
import view.Observer;

public class GamePanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int TILE_SIZE = 60;
    private final SpinOverlay spinOverlay = new SpinOverlay();

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

        drawWorld(g2d);
        drawFloorItems(g2d); // Changed: Draw items from list, not tile
        drawChefs(g2d);
        drawProjectiles(g2d);
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
                        if (wall != null) g2d.drawImage(wall, px, py, TILE_SIZE, TILE_SIZE, null);
                    }
                }
                // Floor items are now drawn separately
            }
        }
    }

    private void drawFloorItems(Graphics2D g2d) {
        for (GameEngine.FloorItem fi : engine.getFloorItems()) {
            int px = (int) (fi.x * TILE_SIZE);
            int py = (int) (fi.y * TILE_SIZE);
            drawItem(g2d, px + 5, py + 5, fi.item, 50);
        }
    }

    private void drawStation(Graphics2D g2d, int x, int y, Station station) {
        String name = station.getName().toLowerCase();
        BufferedImage img = getStationSprite(name, station);
        if (img != null) g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);

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
        if (station instanceof stations.LuckyStation) return sprites.getSprite("lucky_station");
        if (station instanceof stations.IngredientStorage) {
            if (name.contains("pasta")) return sprites.getSprite("crate_pasta");
            if (name.contains("meat")) return sprites.getSprite("crate_meat");
            if (name.contains("tomato")) return sprites.getSprite("crate_tomato");
            if (name.contains("shrimp")) return sprites.getSprite("crate_shrimp");
            if (name.contains("fish")) return sprites.getSprite("crate_fish");
        }
        if (name.contains("cutting")) return sprites.getSprite("cutting station");
        if (name.contains("cook")) return sprites.getSprite("cooking station");
        if (name.contains("wash")) return sprites.getSprite("washing station");
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
        if (item instanceof CookingDevice dev) {
            String devName = dev.getClass().getSimpleName().toLowerCase();
            if (devName.contains("pot")) spriteName = dev.isCooking() ? "pot_cooking" : "boiling pot";
            else if (devName.contains("pan")) spriteName = dev.isCooking() ? "pan_cooking" : "frying pan";
            isCooking = dev.isCooking();
        } else if (item instanceof Plate plate) {
            spriteName = plate.isClean() ? "plate" : "plate_dirty";
        }

        BufferedImage img = sprites.getSprite(spriteName);
        if (img != null) {
            g2d.drawImage(img, x, y, size, size, null);
            if (item instanceof Plate || item instanceof CookingDevice) {
                drawContainerContents(g2d, x, y, item, size);
            }
            if (isCooking) {
                g2d.setColor(Color.RED);
                g2d.fillRect(x, y - 5, (int)(size * (System.currentTimeMillis() % 1000) / 1000.0), 5);
            }
        }
    }

    private void drawContainerContents(Graphics2D g2d, int x, int y, Item container, int parentSize) {
        List<Preparable> contents = (container instanceof Plate p) ? p.getContents() : ((CookingDevice)container).getContents();
        if (contents != null && !contents.isEmpty()) {
            int offsetX = 0, offsetY = 0, i = 0;
            int ingSize = (int)(parentSize * 0.45);
            for (Preparable p : contents) {
                if (p instanceof Item item) {
                    BufferedImage ingImg = SpriteLibrary.getInstance().getSprite(item.getName().toLowerCase());
                    if (ingImg != null) {
                        g2d.drawImage(ingImg, x + 5 + offsetX, y + 5 + offsetY, ingSize, ingSize, null);
                        offsetX += (ingSize / 2);
                        if (i % 2 == 1) { offsetX = 5; offsetY += (ingSize / 2); }
                    }
                }
                i++;
            }
        }
    }

    private void drawProjectiles(Graphics2D g2d) {
        for (GameEngine.Projectile p : engine.getProjectiles()) {
            int x = (int) (p.getRenderX() * TILE_SIZE);
            int y = (int) (p.getRenderY() * TILE_SIZE);
            int size = (int) (TILE_SIZE * 0.6);

            // Shadow on ground
            g2d.setColor(new Color(0, 0, 0, 80));
            // Shadow stays at target height? No, shadow stays on floor y.
            // But we don't have exact floor Y in projectile class easily accessible for shadow,
            // so we estimate shadow based on non-arc Y.
            // p.getRenderY() includes arc. We want Y without arc for shadow.
            // Simplified: Draw shadow slightly below rendered item if arc is high, or just below render.
            // For correct look: Shadow should follow linear path, Item follows arc.
            // But we can just draw shadow offset.
            g2d.fillOval(x + 10, y + size + 5, size - 20, 10);

            drawItem(g2d, x, y, p.getItem(), size);
        }
    }

    private void drawChefs(Graphics2D g2d) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        List<Chef> chefs = engine.getChefs();

        for (int i = 0; i < chefs.size(); i++) {
            Chef c = chefs.get(i);
            int px = (int) (c.getExactX() * TILE_SIZE);
            int py = (int) (c.getExactY() * TILE_SIZE);

            int step = (int)(System.currentTimeMillis() / 200) % 2;
            BufferedImage chefImg = sprites.getChefSprite(i, c.getDirection().name(), c.getHeldItem() != null, c.isBusy(), step);
            if (chefImg != null) g2d.drawImage(chefImg, px, py, TILE_SIZE, TILE_SIZE, null);
            else { g2d.setColor(Color.BLUE); g2d.fillOval(px, py, TILE_SIZE, TILE_SIZE); }

            // Direction Indicator
            g2d.setColor(new Color(255, 255, 255, 100));
            int dirX = px + TILE_SIZE / 2 + (int)(c.getDirection().getXComponent() * 20) - 5;
            int dirY = py + TILE_SIZE / 2 + (int)(c.getDirection().getYComponent() * 20) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);

            if (c.getHeldItem() != null) {
                int itemSize = (int)(TILE_SIZE * 0.5);
                drawItem(g2d, px + (TILE_SIZE - itemSize) / 2, py - 10, c.getHeldItem(), itemSize);
            }
            if (c.getActionProgress() > 0) {
                g2d.setColor(Color.GREEN);
                g2d.fillRect(px, py - 10, (int)(TILE_SIZE * c.getActionProgress()), 8);
            }
        }
    }

    @Override public void update() {}
}