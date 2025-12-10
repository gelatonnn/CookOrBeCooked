package view.gui;

import java.awt.*;
import model.engine.EffectManager.EffectType;

public class SpinOverlay {
    private boolean active = false;
    private float scrollY = 0;
    private float speed = 0;
    private final int BOX_SIZE = 120; // Tinggi per item
    
    private final EffectType[] reel = EffectType.values();
    
    private enum State { ACCELERATE, SPINNING, DECELERATE, SNAPPING, SHOW_RESULT, FINISHED }
    private State state = State.FINISHED;
    private EffectType target;
    private Runnable onFinish;

    private long spinStartTime;
    private long resultStartTime;
    private final long SPIN_DURATION_FAST = 3000; 
    private final long RESULT_DURATION = 2500; 

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
                    // Cek jarak ke target untuk mulai snapping
                    int targetIndex = getTargetIndex();
                    float targetY = targetIndex * BOX_SIZE;
                    float currentPosInReel = scrollY % (reel.length * BOX_SIZE);
                    
                    float dist = targetY - currentPosInReel;
                    if (dist < 0) dist += (reel.length * BOX_SIZE); // Wrap logic

                    // Mulai snapping jika sudah dekat dan pelan
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
                // Logika wrap-around agar mencari jalan terdekat
                if (diff < -totalHeight / 2f) diff += totalHeight;
                if (diff > totalHeight / 2f) diff -= totalHeight;
                
                // Gerakan halus ke titik target
                scrollY += diff * 0.15f; 
                
                // Jika sudah sangat dekat (kurang dari 1 pixel), paksa berhenti
                if (Math.abs(diff) < 1.0f) {
                    scrollY = targetY; // Kunci posisi visual
                    
                    state = State.SHOW_RESULT;
                    resultStartTime = System.currentTimeMillis();
                    
                    AssetManager.getInstance().stopBGM(); 
                    AssetManager.getInstance().playSound("win");
                }
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

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        int cx = screenWidth / 2;
        int cy = screenHeight / 2;

        if (state != State.SHOW_RESULT) {
            drawSlotMachine(g2d, cx, cy);
        } else {
            drawResultCard(g2d, cx, cy);
        }
    }

    private void drawSlotMachine(Graphics2D g2d, int cx, int cy) {
        int w = 220;
        int h = 220;

        // Frame
        g2d.setColor(new Color(218, 165, 32));
        g2d.fillRoundRect(cx - w/2 - 10, cy - h/2 - 10, w + 20, h + 20, 25, 25);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(cx - w/2, cy - h/2, w, h);
        
        // Clip Area (Agar gambar tidak keluar kotak)
        Shape oldClip = g2d.getClip();
        g2d.setClip(cx - w/2, cy - h/2, w, h);

        int totalReelHeight = reel.length * BOX_SIZE;
        float renderY = scrollY % totalReelHeight;

        // --- FIX VISUAL LOOP ---
        // Render dari index minus agar yang dari atas terlihat masuk
        for (int i = -reel.length; i < reel.length * 2; i++) {
            // Handle index negatif agar tetap valid (0..length-1)
            int index = (i % reel.length + reel.length) % reel.length;
            EffectType type = reel[index];
            
            // FIX POSISI: Hapus offset -BOX_SIZE/2f yang bikin meleset
            // Rumus: Titik Tengah + (Urutan * Ukuran) - Posisi Scroll Saat Ini
            float itemY = cy + (i * BOX_SIZE) - renderY;
            
            // Skip jika jauh diluar layar (Optimization)
            if (itemY < cy - h || itemY > cy + h) continue;

            drawEffectIcon(g2d, type, cx, (int)itemY, 90);
        }

        g2d.setClip(oldClip);

        // Garis Penunjuk (Merah)
        g2d.setColor(new Color(220, 20, 60));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(cx - w/2, cy, cx + w/2, cy); // Garis pas di tengah
        
        // Teks Header
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String txt = "LUCKY SPIN!";
        g2d.drawString(txt, cx - g2d.getFontMetrics().stringWidth(txt)/2, cy - h/2 - 30);
    }

    private void drawResultCard(Graphics2D g2d, int cx, int cy) {
        int cardW = 550; // Lebar kartu diperbesar
        int cardH = 320;

        // Background Putih
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(cx - cardW/2, cy - cardH/2, cardW, cardH, 30, 30);
        
        // Border Emas
        g2d.setColor(new Color(255, 215, 0)); 
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRoundRect(cx - cardW/2, cy - cardH/2, cardW, cardH, 30, 30);

        // Header
        g2d.setColor(new Color(40, 40, 40));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
        String title = "Kamu Mendapatkan:";
        g2d.drawString(title, cx - g2d.getFontMetrics().stringWidth(title)/2, cy - 100);

        // Icon Besar di Tengah
        drawEffectIcon(g2d, target, cx, cy - 30, 100);

        // Nama Efek
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
        String name = target.name().replace("_", " ");
        g2d.drawString(name, cx - g2d.getFontMetrics().stringWidth(name)/2, cy + 50);

        // Deskripsi Detail
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        String desc = getEffectDescription(target);
        
        // Text Wrapping sederhana (center align)
        FontMetrics fm = g2d.getFontMetrics();
        int descY = cy + 80;
        // Jika teks kepanjangan, split manual (opsional, disini asumsi muat satu baris panjang)
        g2d.drawString(desc, cx - fm.stringWidth(desc)/2, descY);
    }

    private void drawEffectIcon(Graphics2D g, EffectType type, int x, int y, int size) {
        Color c = switch(type) {
            case FLASH -> new Color(255, 223, 0);   
            case DRUNK -> new Color(138, 43, 226);  
            case DOUBLE_MONEY -> new Color(0, 200, 83); 
            case HELLS_KITCHEN -> new Color(213, 0, 0); 
            case MAGIC_SPONGE -> new Color(0, 176, 255); 
        };
        
        // Kotak Warna
        g.setColor(c);
        g.fillRoundRect(x - size/2, y - size/2, size, size, 15, 15);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x - size/2, y - size/2, size, size, 15, 15);
        
        // Simbol Unicode
        g.setColor(Color.BLACK);
        g.setFont(new Font("Segoe UI Emoji", Font.BOLD, size/2));
        String symbol = switch(type) {
            case FLASH -> "⚡";
            case DRUNK -> "🥴";
            case DOUBLE_MONEY -> "$";
            case HELLS_KITCHEN -> "🔥";
            case MAGIC_SPONGE -> "✨";
        };
        FontMetrics fm = g.getFontMetrics();
        g.drawString(symbol, x - fm.stringWidth(symbol)/2, y + fm.getAscent()/2 - 5);
    }

    private String getEffectDescription(EffectType type) {
        return switch(type) {
            case FLASH -> "Kecepatan lari meningkat & Dash tanpa cooldown! (15s)";
            case DRUNK -> "Oops! Kontrol arah chef menjadi terbalik. (10s)";
            case DOUBLE_MONEY -> "Cuan Time! Setiap pesanan bernilai 2x lipat. (20s)";
            case HELLS_KITCHEN -> "Bencana! Satu masakan yang sedang dimasak langsung GOSONG.";
            case MAGIC_SPONGE -> "Cling! Semua piring kotor di peta menjadi bersih seketika.";
        };
    }
}