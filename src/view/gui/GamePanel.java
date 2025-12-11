package view.gui;

import items.core.*;
import items.utensils.Plate;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameEngine;
import model.orders.Order;
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

    // UI Elements
    private Font pixelFont;
    private Font pixelFontSmall;
    private JButton btnRecipe, btnHelp, btnExit;

    // Layout Variables
    private int cx = 0;
    private int cy = 0;
    private double scale = 1.0;

    private final java.util.List<NotificationRequest> notificationQueue = new java.util.ArrayList<>();
    private record NotificationRequest(int x, int y, items.core.CookingDevice device) {}

    public GamePanel(GameEngine engine, Runnable onExitClicked) {
        this.engine = engine;
        this.setBackground(new Color(20, 20, 25));
        this.setDoubleBuffered(true);
        this.setLayout(null);

        // Load Fonts
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 14f);
        this.pixelFontSmall = loadPixelFont("/resources/fonts/PressStart2P.ttf", 9f);

        // --- INIT TOMBOL ---
        initButtons(onExitClicked);

        // Effect Listener
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

    private void initButtons(Runnable onExitClicked) {
        // 1. Recipe
        btnRecipe = createPixelButton("RECIPE", new Color(41, 173, 255));
        btnRecipe.addActionListener(e -> {
            showModelessDialog("RECIPE BOOK", getRecipeContent());
            // Paksa fokus balik ke game setelah dialog tutup
            this.requestFocusInWindow();
        });
        add(btnRecipe);

        // 2. Help
        btnHelp = createPixelButton("HELP", new Color(0, 228, 54));
        btnHelp.addActionListener(e -> {
            showModelessDialog("CONTROLS", getHelpContent());
            this.requestFocusInWindow();
        });
        add(btnHelp);

        // 3. Exit
        btnExit = createPixelButton("EXIT", new Color(255, 0, 77));
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Quit to Main Menu?", "Exit Game", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (onExitClicked != null) onExitClicked.run();
            } else {
                this.requestFocusInWindow();
            }
        });
        add(btnExit);
    }

    // --- POSISI TOMBOL BARU ---
    @Override
    public void doLayout() {
        super.doLayout();

        int frameW = getWidth();
        int frameH = getHeight();
        int mapW = engine.getWorld().getWidth() * TILE_SIZE;
        int mapH = engine.getWorld().getHeight() * TILE_SIZE;

        double s = Math.min((double)frameW / mapW, (double)frameH / mapH);
        int marginX = (int)((frameW - mapW * s) / 2);

        // Pastikan ada margin kiri yg cukup
        if (marginX > 130) {
            int btnW = 120;
            int btnH = 45;

            // Posisi X di tengah margin kiri
            int btnX = (marginX - btnW) / 2;

            // --- POSISI Y BARU ---

            // Info Box (Timer/Score) tingginya sekitar 100px + margin atas 20px = 120px.
            // Kita taruh tombol Recipe dan Help tepat di bawahnya.

            int startY = 140; // 120 + 20px gap

            btnRecipe.setBounds(btnX, startY, btnW, btnH);
            btnHelp.setBounds(btnX, startY + 60, btnW, btnH); // 45 (tinggi) + 15 (gap)

            // Exit Button: Di Kiri Bawah (Bottom Left)
            int bottomMargin = 40;
            btnExit.setBounds(btnX, frameH - btnH - bottomMargin, btnW, btnH);

            setButtonsVisible(true);
        } else {
            setButtonsVisible(false);
        }
    }

    private void setButtonsVisible(boolean visible) {
        btnRecipe.setVisible(visible);
        btnHelp.setVisible(visible);
        btnExit.setVisible(visible);
    }

    // --- BUTTON HELPER ---
    private JButton createPixelButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                Color color = baseColor;
                if (getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2);
                } else if (getModel().isRollover()) {
                    color = baseColor.brighter();
                }

                int w = getWidth(); int h = getHeight(); int stroke = 3;
                g2.setColor(color); g2.fillRect(0, 0, w, h);
                g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(stroke)); g2.drawRect(stroke/2, stroke/2, w-stroke, h-stroke);

                g2.setColor(new Color(255, 255, 255, 80)); g2.fillRect(stroke, stroke, w-stroke*2, 3);
                g2.setColor(new Color(0, 0, 0, 50)); g2.fillRect(stroke, h-stroke-3, w-stroke*2, 3);

                g2.setColor(Color.WHITE); g2.setFont(pixelFontSmall);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText()))/2;
                int ty = (h - fm.getHeight())/2 + fm.getAscent();

                g2.setColor(Color.BLACK); g2.drawString(getText(), tx+2, ty+2);
                g2.setColor(Color.WHITE); g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- FIX UTAMA: Mencegah tombol mencuri fokus keyboard ---
        btn.setFocusable(false);
        // --------------------------------------------------------

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // 1. SCALING
        int frameW = getWidth();
        int frameH = getHeight();
        int mapW = engine.getWorld().getWidth() * TILE_SIZE;
        int mapH = engine.getWorld().getHeight() * TILE_SIZE;

        scale = Math.min((double)frameW / mapW, (double)frameH / mapH);
        cx = (int)((frameW - mapW * scale) / 2);
        cy = (int)((frameH - mapH * scale) / 2);

        // 2. BACKGROUND & UI
        drawBackgroundPattern(g2d, frameW, frameH);

        if (cx > 50) {
            drawLeftSidebarInfo(g2d);
            drawRightSidebarOrders(g2d);
        }

        // 3. GAME WORLD
        AffineTransform oldAT = g2d.getTransform();
        g2d.translate(cx, cy);
        g2d.scale(scale, scale);

        Shape originalClip = g2d.getClip();
        g2d.clipRect(0, 0, mapW, mapH);

        notificationQueue.clear();
        drawWorld(g2d);
        drawChefs(g2d);
        drawProjectiles(g2d);
        drawAllNotifications(g2d);

        g2d.setClip(originalClip);
        g2d.setTransform(oldAT);

        // 4. OVERLAY
        spinOverlay.draw((Graphics2D)g, frameW, frameH);

        if (!isValid()) validate();
    }

    private void drawBackgroundPattern(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(40, 30, 30));
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(new Color(50, 40, 40));
        int patternSize = 40;
        for (int y = 0; y < h; y += patternSize) {
            for (int x = 0; x < w; x += patternSize) {
                if ((x/patternSize + y/patternSize) % 2 == 0) {
                    g2d.fillRect(x, y, patternSize, patternSize);
                }
            }
        }
    }

    private void drawLeftSidebarInfo(Graphics2D g2d) {
        int boxX = cx / 2 - 60;
        if (boxX < 10) boxX = 10;
        int boxY = 20;
        int boxW = 120;
        int boxH = 100;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        int time = engine.getClock().getTimeRemaining();
        g2d.setFont(pixelFontSmall);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("TIME", boxX + 10, boxY + 25);

        g2d.setFont(pixelFont);
        if (time <= 30) g2d.setColor(new Color(255, 0, 77));
        else g2d.setColor(Color.WHITE);
        String timeStr = String.format("%02d:%02d", time/60, time%60);
        g2d.drawString(timeStr, boxX + 10, boxY + 45);

        g2d.setFont(pixelFontSmall);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("SCORE", boxX + 10, boxY + 70);

        g2d.setFont(pixelFont);
        g2d.setColor(new Color(255, 236, 39));
        g2d.drawString(String.valueOf(engine.getOrders().getScore()), boxX + 10, boxY + 90);
    }

    private void drawRightSidebarOrders(Graphics2D g2d) {
        int frameW = getWidth();
        int marginWidth = (frameW - (int)(engine.getWorld().getWidth() * TILE_SIZE * scale)) / 2;
        int startX = frameW - marginWidth + (marginWidth - 120)/2;
        int startY = 20;

        g2d.setColor(Color.WHITE);
        g2d.setFont(pixelFont);
        g2d.drawString("ORDERS", startX, startY + 10);

        java.util.List<Order> orders = engine.getOrders().getActiveOrders();
        int y = startY + 30;
        int cardW = 120;
        int cardH = 65;
        int gap = 15;

        for (Order o : orders) {
            drawOrderCard(g2d, startX, y, cardW, cardH, o);
            y += (cardH + gap);
        }
    }

    private void drawOrderCard(Graphics2D g, int x, int y, int w, int h, Order o) {
        g.setColor(new Color(255, 241, 232)); g.fillRect(x, y, w, h);
        g.setColor(Color.BLACK); g.setStroke(new BasicStroke(3)); g.drawRect(x, y, w, h);
        g.setColor(new Color(200, 200, 200)); g.fillOval(x + w/2 - 5, y - 5, 10, 10);
        g.setColor(Color.BLACK); g.drawOval(x + w/2 - 5, y - 5, 10, 10);

        try {
            String spriteName = o.getRecipe().getName().toLowerCase();
            BufferedImage icon = SpriteLibrary.getInstance().getSprite(spriteName);
            if (icon != null) g.drawImage(icon, x + w - 35, y + 10, 30, 30, null);
        } catch (Exception e) {}

        g.setColor(Color.BLACK); g.setFont(pixelFontSmall.deriveFont(7f));
        String name = o.getRecipe().getName().toUpperCase().replace("PASTA ", "");
        if (name.length() > 9) name = name.substring(0, 9);
        g.drawString(name, x + 8, y + 25);

        int maxTime = 90; int timeLeft = o.getTimeLeft();
        int maxBarWidth = w - 16; int currentBarWidth = (int) ((double) timeLeft / maxTime * maxBarWidth);
        Color barColor = (timeLeft > 30) ? new Color(0, 228, 54) : (timeLeft > 15) ? new Color(255, 163, 0) : new Color(255, 0, 77);

        g.setColor(new Color(100, 100, 100)); g.fillRect(x + 8, y + 45, maxBarWidth, 8);
        g.setColor(barColor); g.fillRect(x + 8, y + 45, currentBarWidth, 8);
        g.setColor(Color.BLACK); g.setStroke(new BasicStroke(2)); g.drawRect(x + 8, y + 45, maxBarWidth, 8);
    }

    private Font loadPixelFont(String path, float size) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) return new Font("Monospaced", Font.BOLD, (int)size);
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (FontFormatException | IOException e) { return new Font("Monospaced", Font.BOLD, (int)size); }
    }

    private void showModelessDialog(String title, String content) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title);
        dialog.setModal(true);
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false); textArea.setMargin(new Insets(15,15,15,15));
        textArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        textArea.setBackground(new Color(240, 240, 240));
        dialog.add(new JScrollPane(textArea));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String getRecipeContent() { return "=== RECIPES ===\n\n[Pasta Marinara]\nPasta + Tomato\n\n[Pasta Bolognese]\nPasta + Meat\n\n[Seafood Pasta]\nPasta + Shrimp + Fish"; }
    private String getHelpContent() { return "=== CONTROLS ===\n\nP1: W,A,S,D (Move), V (Act), B (Grab), F (Throw)\nP2: Arrows, K (Act), L (Grab), ; (Throw)\nSingle: Tab (Switch)"; }


    private void drawWorld(Graphics2D g2d) {
        WorldMap map = engine.getWorld();
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.getTile(new Position(x, y));
                int px = x * TILE_SIZE; int py = y * TILE_SIZE;
                g2d.setColor(new Color(139, 69, 19)); g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g2d.setColor(new Color(160, 82, 45)); g2d.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                if (!tile.isWalkable()) {
                    if (tile instanceof StationTile stTile) drawStation(g2d, px, py, stTile.getStation());
                    else {
                        BufferedImage wall = sprites.getSprite("wall");
                        if (wall != null) g2d.drawImage(wall, px, py, TILE_SIZE, TILE_SIZE, null);
                        else { g2d.setColor(Color.DARK_GRAY); g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE); }
                    }
                }
                if (tile instanceof WalkableTile wt && wt.getItem() != null) {
                    int itemSize = 40; int offset = (TILE_SIZE - itemSize) / 2;
                    drawItem(g2d, px + offset, py + offset, wt.getItem(), itemSize);
                }
            }
        }
    }

    private void drawStation(Graphics2D g2d, int x, int y, Station station) {
        String name = station.getName().toLowerCase();
        BufferedImage img = getStationSprite(name, station);
        if (img != null) g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        else { g2d.setColor(Color.LIGHT_GRAY); g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE); }
        if (!(station instanceof stations.PlateStorage)) {
            items.core.Item storedItem = station.peek();
            if (storedItem != null) {
                int itemSize = (int)(TILE_SIZE * 0.6);
                int offsetX = (TILE_SIZE - itemSize) / 2;
                int offsetY = (TILE_SIZE - itemSize) / 2;
                if (station instanceof stations.CuttingStation) { offsetY -= 8; offsetX -= 5; }
                drawItem(g2d, x + offsetX, y + offsetY, storedItem, itemSize);
                if (storedItem instanceof items.core.CookingDevice device) notificationQueue.add(new NotificationRequest(x, y, device));
            }
        }
    }

    private BufferedImage getStationSprite(String name, Station station) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        if (station instanceof stations.LuckyStation) return sprites.getSprite("lucky_station");
        if (station instanceof IngredientStorage) {
            if (name.contains("pasta")) return sprites.getSprite("crate_pasta");
            if (name.contains("meat")) return sprites.getSprite("crate_meat");
            if (name.contains("tomato")) return sprites.getSprite("crate_tomato");
            if (name.contains("shrimp")) return sprites.getSprite("crate_shrimp");
            if (name.contains("fish")) return sprites.getSprite("crate_fish");
            return sprites.getSprite("ingredient storage");
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
        spriteName += switch (item.getState()) { case COOKED -> "_cooked"; case BURNED -> "_burned"; case CHOPPED -> "_chopped"; default -> ""; };
        boolean isCooking = false;
        if (item instanceof CookingDevice dev) {
            isCooking = dev.isCooking();
            if (dev.getClass().getSimpleName().toLowerCase().contains("pot")) spriteName = isCooking ? "pot_cooking" : "boiling pot";
            else spriteName = isCooking ? "pan_cooking" : "frying pan";
        } else if (item instanceof Plate p) spriteName = p.isClean() ? "plate" : "plate_dirty";
        BufferedImage img = sprites.getSprite(spriteName);
        if (img != null) {
            g2d.drawImage(img, x, y, size, size, null);
            if (item instanceof Plate || item instanceof CookingDevice) drawContainerContents(g2d, x, y, item, size);
            if (isCooking) drawCookingIndicator(g2d, x, y, size);
        }
    }

    private void drawCookingIndicator(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(Color.WHITE); g2d.fillRect(x, y - 5, size, 5);
        g2d.setColor(Color.RED); g2d.fillRect(x, y - 5, (int)(size * (System.currentTimeMillis() % 1000) / 1000.0), 7);
    }

    private void drawContainerContents(Graphics2D g2d, int x, int y, Item container, int parentSize) {
        List<Preparable> contents = (container instanceof Plate) ? ((Plate)container).getContents() : ((CookingDevice)container).getContents();
        if (contents != null && !contents.isEmpty()) {
            int offsetX = 0; int offsetY = 0; int i = 0;
            int ingSize = (int)(parentSize * 0.45);
            for (Preparable p : contents) {
                if (p instanceof Item item) {
                    String ingName = item.getName().toLowerCase();
                    ingName += switch (item.getState()) { case COOKED -> "_cooked"; case BURNED -> "_burned"; case CHOPPED -> "_chopped"; default -> ""; };
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

    private void drawChefs(Graphics2D g2d) {
        SpriteLibrary sprites = SpriteLibrary.getInstance();
        List<Chef> chefs = engine.getChefs();
        for (int i = 0; i < chefs.size(); i++) {
            Chef c = chefs.get(i);
            int px = (int) (c.getExactX() * TILE_SIZE); int py = (int) (c.getExactY() * TILE_SIZE);
            int step = (int)(System.currentTimeMillis() / 200) % 2;
            BufferedImage chefImg = sprites.getChefSprite(i, c.getDirection().name(), c.getHeldItem() != null, c.isBusy(), step);
            if (chefImg != null) g2d.drawImage(chefImg, px, py, TILE_SIZE, TILE_SIZE, null);
            else { g2d.setColor(Color.BLUE); g2d.fillOval(px, py, TILE_SIZE, TILE_SIZE); }
            g2d.setColor(new Color(255, 255, 255, 50));
            int dirX = px + TILE_SIZE / 2 + (c.getDirection().dx * (TILE_SIZE/3)) - 5;
            int dirY = py + TILE_SIZE / 2 + (c.getDirection().dy * (TILE_SIZE/3)) - 5;
            g2d.fillOval(dirX, dirY, 10, 10);
            if (c.getHeldItem() != null) {
                int itemSize = (int)(TILE_SIZE * 0.5);
                drawItem(g2d, px + (TILE_SIZE - itemSize) / 2, py - (itemSize / 2), c.getHeldItem(), itemSize);
            }
            if (c.getActionProgress() > 0) {
                int bw = TILE_SIZE - 20; int bh = 8;
                g2d.setColor(Color.BLACK); g2d.fillRect(px+10, py-10, bw, bh);
                g2d.setColor(Color.GREEN); g2d.fillRect(px+11, py-9, (int)((bw-2)*c.getActionProgress()), bh-2);
            }
        }
    }

    private void drawProjectiles(Graphics2D g2d) {
        List<GameEngine.Projectile> projectiles = engine.getProjectiles();
        for (GameEngine.Projectile p : projectiles) {
            int x = (int) (p.getX() * TILE_SIZE); int y = (int) (p.getY() * TILE_SIZE);
            int size = (int) (TILE_SIZE * 0.5);
            g2d.setColor(new Color(0, 0, 0, 100)); g2d.fillOval(x + 10, y + 40, size, size / 3);
            drawItem(g2d, x + (TILE_SIZE - size) / 2, y + (TILE_SIZE - size) / 2 - 10, p.getItem(), size);
        }
    }

    private void drawAllNotifications(Graphics2D g2d) {
        for (NotificationRequest req : notificationQueue) {
            if (req.device.getContents().isEmpty()) continue;
            items.core.Item firstItem = (items.core.Item) req.device.getContents().get(0);
            if (firstItem.getState() == items.core.ItemState.COOKED || firstItem.getState() == items.core.ItemState.BURNED) {
                BufferedImage cloud = SpriteLibrary.getInstance().getSprite("cloud");
                if (cloud != null) {
                    int cs = (int)(TILE_SIZE * 0.9);
                    g2d.drawImage(cloud, req.x + TILE_SIZE/2, req.y - TILE_SIZE/2, cs, cs, null);
                    String sn = firstItem.getName().toLowerCase() + (firstItem.getState() == items.core.ItemState.COOKED ? "_cooked" : "_burned");
                    BufferedImage im = SpriteLibrary.getInstance().getSprite(sn);
                    if (im != null) g2d.drawImage(im, req.x + TILE_SIZE/2 + cs/4, req.y - TILE_SIZE/2 + cs/4, cs/2, cs/2, null);
                }
            }
        }
    }

    @Override public void update() { repaint(); }
}