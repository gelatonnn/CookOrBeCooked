package view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.IOException;
import java.io.InputStream;

import model.engine.EffectManager.EffectType;

public class SpinOverlay {
    private boolean active = false;
    private float scrollY = 0;
    private float speed = 0;
    private final int BOX_SIZE = 120; 

    private final EffectType[] reel = EffectType.values();

    private enum State { ACCELERATE, SPINNING, DECELERATE, SNAPPING, SHOW_RESULT, FINISHED }
    private State state = State.FINISHED;
    private EffectType target;
    private Runnable onFinish;

    private long spinStartTime;
    private long resultStartTime;
    private final long SPIN_DURATION_FAST = 3000;
    private final long RESULT_DURATION = 2500;

    private Font pixelFont;
    private Font pixelFontLarge;
    private Font pixelFontSmall;

    public SpinOverlay() {
        // Load Fonts di Constructor
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 12f);
        this.pixelFontLarge = loadPixelFont("/resources/fonts/PressStart2P.ttf", 20f);
        this.pixelFontSmall = loadPixelFont("/resources/fonts/PressStart2P.ttf", 8f);
    }

    private Font loadPixelFont(String path, float size) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) return new Font("Monospaced", Font.BOLD, (int)size);
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (FontFormatException | IOException e) {
            return new Font("Monospaced", Font.BOLD, (int)size);
        }
    }

    public void start(EffectType target, Runnable onFinish) {
        this.active = true;
        this.target = target;
        this.onFinish = onFinish;
        this.scrollY = 0;
        this.speed = 0;
        this.state = State.ACCELERATE;
        this.spinStartTime = System.currentTimeMillis();

        AssetManager.getInstance().playSound("spin");
    }

    public void update() {
        if (!active) return;

        long elapsed = System.currentTimeMillis() - spinStartTime;

        switch (state) {
            case ACCELERATE -> {
                speed += 2.0f;
                if (speed >= 50.0f) state = State.SPINNING;
            }
            case SPINNING -> {
                if (elapsed > SPIN_DURATION_FAST) {
                    state = State.DECELERATE;
                }
            }
            case DECELERATE -> {
                speed *= 0.96f;
                if (speed < 20.0f) {
                    int targetIndex = getTargetIndex();
                    float targetY = targetIndex * BOX_SIZE;
                    float currentPosInReel = scrollY % (reel.length * BOX_SIZE);

                    float dist = targetY - currentPosInReel;
                    if (dist < 0) dist += (reel.length * BOX_SIZE);

                    if (dist < 150 && speed < 15.0f) {
                        state = State.SNAPPING;
                    }
                }
            }
            case SNAPPING -> {
                int totalHeight = reel.length * BOX_SIZE;
                float currentMod = scrollY % totalHeight;
                int targetIndex = getTargetIndex();
                float targetY = targetIndex * BOX_SIZE;

                float diff = targetY - currentMod;
                if (diff < -totalHeight / 2f) diff += totalHeight;
                if (diff > totalHeight / 2f) diff -= totalHeight;

                scrollY += diff * 0.15f;

                if (Math.abs(diff) < 1.0f) {
                    scrollY = targetY;
                    state = State.SHOW_RESULT;
                    resultStartTime = System.currentTimeMillis();
                    AssetManager.getInstance().stopBGM();
                    AssetManager.getInstance().playSound("win");
                }
                AssetManager.getInstance().playSound("bgm_game");
            }
            case SHOW_RESULT -> {
                if (System.currentTimeMillis() - resultStartTime > RESULT_DURATION) {
                    state = State.FINISHED;
                    active = false;
                    if (onFinish != null) onFinish.run();
                }
            }
        }

        if (state != State.SNAPPING && state != State.SHOW_RESULT) {
            scrollY += speed;
        }
    }

    private int getTargetIndex() {
        for (int i = 0; i < reel.length; i++) {
            if (reel[i] == target) return i;
        }
        return 0;
    }

    public void draw(Graphics2D g2d, int screenWidth, int screenHeight) {
        if (!active) return;

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int cx = screenWidth / 2;
        int cy = screenHeight / 2;

        if (state != State.SHOW_RESULT) {
            drawSlotMachine(g2d, cx, cy);
        } else {
            drawResultCard(g2d, cx, cy);
        }
    }

    private void drawSlotMachine(Graphics2D g2d, int cx, int cy) {
        int w = 240;
        int h = 240;
        int border = 10;

        // 1. Frame Luar (Emas Retro)
        g2d.setColor(new Color(255, 163, 0)); 
        g2d.fillRect(cx - w/2 - border, cy - h/2 - border, w + border*2, h + border*2);

        // Border Hitam Tebal
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(cx - w/2 - border, cy - h/2 - border, w + border*2, h + border*2);

        // Highlight/Shadow Frame
        g2d.setColor(new Color(255, 236, 39)); 
        g2d.fillRect(cx - w/2 - border, cy - h/2 - border, w + border*2, 4);
        g2d.setColor(new Color(171, 82, 54)); 
        g2d.fillRect(cx - w/2 - border, cy + h/2 + border - 4, w + border*2, 4);

        // 2. Jendela Slot (Putih)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(cx - w/2, cy - h/2, w, h);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(cx - w/2, cy - h/2, w, h);

        // --- GAMBAR REEL ---
        Shape oldClip = g2d.getClip();
        g2d.setClip(cx - w/2, cy - h/2, w, h);

        int totalReelHeight = reel.length * BOX_SIZE;
        float renderY = scrollY % totalReelHeight;

        for (int i = -reel.length; i < reel.length * 2; i++) {
            int index = (i % reel.length + reel.length) % reel.length;
            EffectType type = reel[index];
            float itemY = cy + (i * BOX_SIZE) - renderY;

            if (itemY < cy - h || itemY > cy + h) continue;

            drawEffectIcon(g2d, type, cx, (int)itemY, 90);
        }

        g2d.setClip(oldClip);

        // 3. Garis Penunjuk (Segitiga Merah Pixel)
        g2d.setColor(new Color(255, 0, 77)); 

        // Kiri
        int[] xL = {cx - w/2 - 20, cx - w/2, cx - w/2 - 20};
        int[] yL = {cy - 10, cy, cy + 10};
        g2d.fillPolygon(xL, yL, 3);

        // Kanan
        int[] xR = {cx + w/2 + 20, cx + w/2, cx + w/2 + 20};
        int[] yR = {cy - 10, cy, cy + 10};
        g2d.fillPolygon(xR, yR, 3);

        // Garis Tengah Transparan
        g2d.setColor(new Color(255, 0, 77, 100));
        g2d.drawLine(cx - w/2, cy, cx + w/2, cy);

        // 4. Teks Judul
        g2d.setColor(new Color(255, 204, 170)); 
        g2d.setFont(pixelFontLarge);
        String txt = "LUCKY SPIN!";
        g2d.drawString(txt, cx - g2d.getFontMetrics().stringWidth(txt)/2, cy - h/2 - 40);
    }

    private void drawResultCard(Graphics2D g2d, int cx, int cy) {
        int cardW = 600;
        int cardH = 350;
        int border = 6;

        // 1. Kartu Background 
        g2d.setColor(Color.WHITE);
        g2d.fillRect(cx - cardW/2, cy - cardH/2, cardW, cardH);

        // 2. Border Emas Tebal
        g2d.setColor(new Color(255, 163, 0));
        g2d.setStroke(new BasicStroke(border));
        g2d.drawRect(cx - cardW/2, cy - cardH/2, cardW, cardH);

        // 3. Border Luar Hitam
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(cx - cardW/2 - border/2, cy - cardH/2 - border/2, cardW + border, cardH + border);

        // 4. Header "YOU GOT:"
        g2d.setColor(new Color(41, 173, 255)); 
        g2d.setFont(pixelFontLarge);
        String title = "YOU GOT:";
        g2d.drawString(title, cx - g2d.getFontMetrics().stringWidth(title)/2, cy - 100);

        // 5. Icon Besar
        drawEffectIcon(g2d, target, cx, cy - 20, 100);

        // 6. Nama Efek
        g2d.setColor(Color.BLACK);
        g2d.setFont(pixelFont);
        String name = target.name().replace("_", " ");
        g2d.drawString(name, cx - g2d.getFontMetrics().stringWidth(name)/2, cy + 60);

        // 7. Deskripsi 
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(pixelFontSmall);
        String desc = getEffectDescription(target);
        g2d.drawString(desc, cx - g2d.getFontMetrics().stringWidth(desc)/2, cy + 90);
    }

    private void drawEffectIcon(Graphics2D g, EffectType type, int x, int y, int size) {
        Color c = switch(type) {
            case FLASH -> new Color(255, 236, 39);   
            case DRUNK -> new Color(131, 118, 156);  
            case DOUBLE_MONEY -> new Color(0, 228, 54); 
            case HELLS_KITCHEN -> new Color(255, 0, 77);
            case MAGIC_SPONGE -> new Color(41, 173, 255);
        };

        // Kotak Warna Dasar
        g.setColor(c);
        g.fillRect(x - size/2, y - size/2, size, size);

        // Border Hitam
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));
        g.drawRect(x - size/2, y - size/2, size, size);

        // Efek Bevel (Highlight & Shadow)
        g.setColor(new Color(255, 255, 255, 100));
        g.fillRect(x - size/2 + 4, y - size/2 + 4, size - 8, 4); 

        // Simbol Unicode
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, size/2));

        String symbol = switch(type) {
            case FLASH -> "⚡";
            case DRUNK -> "😵";
            case DOUBLE_MONEY -> "$";
            case HELLS_KITCHEN -> "🔥";
            case MAGIC_SPONGE -> "✨";
        };

        FontMetrics fm = g.getFontMetrics();
        g.drawString(symbol, x - fm.stringWidth(symbol)/2, y + fm.getAscent()/2 - 5);
    }

    private String getEffectDescription(EffectType type) {
        return switch(type) {
            case FLASH -> "SPEED UP & NO DASH COOLDOWN! (15s)";
            case DRUNK -> "OOPS! CONTROLS ARE INVERTED. (10s)";
            case DOUBLE_MONEY -> "DOUBLE SCORE FOR EVERY ORDER! (20s)";
            case HELLS_KITCHEN -> "OH NO! ONE DISH IS INSTANTLY BURNED.";
            case MAGIC_SPONGE -> "MAGIC! ALL DIRTY PLATES ARE CLEANED.";
        };
    }
}