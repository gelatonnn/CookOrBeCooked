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
    
    private final Map<Chef, Point2D.Double> chefRenderPositions = new HashMap<>();

    private final java.util.List<NotificationRequest> notificationQueue = new java.util.ArrayList<>();

    private record NotificationRequest(int x, int y, items.core.CookingDevice device) {}
    
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

    private void updateVisuals() {
        // 1. Update Timer Animasi
        animationTick++;

        // 2. Update Posisi Halus (Smooth Movement)
        for (Chef c : engine.getChefs()) {
            // Posisi Target (Grid * Ukuran Tile)
            double targetX = c.getX() * TILE_SIZE;
            double targetY = c.getY() * TILE_SIZE;

            if (!chefRenderPositions.containsKey(c)) {
                chefRenderPositions.put(c, new Point2D.Double(targetX, targetY));
            }
            Point2D.Double currentPos = chefRenderPositions.get(c);

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

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        notificationQueue.clear();
        drawWorld(g2d);
        drawChefs(g2d);
        drawAllNotifications(g2d);
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
                    int itemSize = 40; 
                    int offset = (TILE_SIZE - itemSize) / 2; 
                
                    drawItem(g2d, px + offset, py + offset, wt.getItem(), itemSize);
            }
            }
        }
    }

    private void drawStation(Graphics2D g2d, int x, int y, Station station) {
        String name = station.getName().toLowerCase();
        
        BufferedImage img = getStationSprite(name, station);

        // Gambar Station
        if (img != null) {
            g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        } else {
            // Fallback
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        }

        // Gambar Item di Atas Station
        if (!(station instanceof stations.PlateStorage)) {
            items.core.Item storedItem = station.peek();
            
            if (storedItem != null) {
                int itemSize = (int)(TILE_SIZE * 0.6); 
                
                int offsetX = (TILE_SIZE - itemSize) / 2;
                int offsetY = (TILE_SIZE - itemSize) / 2;
                
                if (station instanceof stations.CuttingStation) {
                    offsetY -= 8;
                    offsetX -= 5;
                }
                
                drawItem(g2d, x + offsetX, y + offsetY, storedItem, itemSize);
                
                if (storedItem instanceof items.core.CookingDevice device) {
                    notificationQueue.add(new NotificationRequest(x, y, device));
                }
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
                    if (dev.isCooking()) {
                        spriteName = "pot_cooking"; 
                    } else {
                        spriteName = "boiling pot"; 
                    }
                } else if (devClassName.contains("pan")) {
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
            int offsetX = 0;
            int offsetY = 0;
            int i = 0;
            
            int ingSize = (int)(parentSize * 0.45); 

            for (Preparable p : contents) {
                if (p instanceof Item item) {
                    String ingName = item.getName().toLowerCase();

                    ingName += switch (item.getState()) {
                    case COOKED -> "_cooked";
                    case BURNED -> "_burned";
                    case CHOPPED -> "_chopped"; 
                    default -> "";
                };

                    BufferedImage ingImg = SpriteLibrary.getInstance().getSprite(ingName);
                    
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
        g2d.fillRect(screenX + 1, y + 1, (int) ((barWidth - 2) * percentage), barHeight - 2);
    }

    private void drawCookedNotification(Graphics2D g2d, int x, int y, CookingDevice device) {
        // 1. Cek validasi: Panci kosong?
        if (device.getContents().isEmpty())
            return;

        // 2. Ambil item pertama
        items.core.Item firstItem = (items.core.Item) device.getContents().get(0);
        items.core.ItemState state = firstItem.getState();

        // 3. Cek Status: HANYA gambar jika sudah COOKED atau BURNED
        if (state == items.core.ItemState.COOKED || state == items.core.ItemState.BURNED) {

            BufferedImage cloud = SpriteLibrary.getInstance().getSprite("cloud");

            if (cloud != null) {
                int cloudSize = (int) (TILE_SIZE * 0.9);

                int cloudX = x + (TILE_SIZE / 2);
                int cloudY = y - (TILE_SIZE / 2);

                int panelWidth = getWidth();
                if (cloudX + cloudSize > panelWidth) {
                    cloudX = panelWidth - cloudSize;
                }

                if (cloudY < 0) {
                    cloudY = 0;
                }

                g2d.drawImage(cloud, cloudX, cloudY, cloudSize, cloudSize, null);

                String spriteName = firstItem.getName().toLowerCase();
                if (state == items.core.ItemState.COOKED)
                    spriteName += "_cooked";
                else if (state == items.core.ItemState.BURNED)
                    spriteName += "_burned";

                BufferedImage itemImg = SpriteLibrary.getInstance().getSprite(spriteName);

                if (itemImg != null) {
                    int itemSize = (int) (cloudSize * 0.55);

                    int itemX = cloudX + (cloudSize - itemSize) / 2;
                    int itemY = cloudY + (cloudSize - itemSize) / 2;

                    g2d.drawImage(itemImg, itemX, itemY - 3, itemSize, itemSize, null);
                }
            }
        }
    }
    
    private void drawAllNotifications(Graphics2D g2d) {
        for (NotificationRequest req : notificationQueue) {
            drawCookedNotification(g2d, req.x, req.y, req.device);
        }
    }

    @Override
    public void update() {
        // No longer relying on this for render loop, but kept for logic events if needed
    }
}