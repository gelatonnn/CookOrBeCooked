package view.gui;

import model.engine.GameEngine;
import model.orders.Order;
import view.Observer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HUDPanel extends JPanel implements Observer {
    private final GameEngine engine;

    public HUDPanel(GameEngine engine) {
        this.engine = engine;
        // Tinggi HUD 80 pixel, lebar mengikuti window
        this.setPreferredSize(new Dimension(800, 80)); 
        this.setBackground(new Color(40, 40, 40)); // Abu-abu gelap
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Agar teks dan bentuk terlihat halus (tidak bergerigi)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTimer(g2d);
        drawScore(g2d);
        drawOrders(g2d);
    }

    private void drawTimer(Graphics2D g) {
        int time = engine.getClock().getTimeRemaining();
        
        // Warna berubah jadi Merah jika waktu < 30 detik (Panic Mode!)
        if (time <= 30) g.setColor(new Color(255, 80, 80));
        else g.setColor(Color.WHITE);

        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String text = String.format("%02d:%02d", time / 60, time % 60);
        
        // Gambar di pojok kiri
        g.drawString(text, 30, 50);
        
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("TIME LEFT", 32, 20);
    }

    private void drawScore(Graphics2D g) {
        int score = engine.getOrders().getScore();
        
        g.setColor(new Color(255, 215, 0)); // Warna Emas
        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String text = String.valueOf(score);
        
        // Gambar agak di kiri setelah Timer
        g.drawString(text, 180, 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("SCORE", 182, 20);
    }

    private void drawOrders(Graphics2D g) {
        List<Order> orders = engine.getOrders().getActiveOrders();
        
        // Mulai menggambar dari kanan layar ke kiri
        int startX = getWidth() - 20;
        int y = 10;
        int cardWidth = 140;
        int cardHeight = 60;

        for (Order o : orders) {
            startX -= (cardWidth + 10); // Geser ke kiri untuk kartu berikutnya
            
            // 1. Kartu Background (Putih)
            g.setColor(new Color(240, 240, 240));
            g.fillRoundRect(startX, y, cardWidth, cardHeight, 15, 15);
            
            // 2. Nama Pesanan
            g.setColor(Color.BLACK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String name = o.getRecipe().getName();
            
            // Potong teks jika terlalu panjang
            if (name.length() > 14) name = name.substring(0, 14) + "..";
            g.drawString(name, startX + 10, y + 25);
            
            // 3. Icon Dish (Visualisasi kecil)
            // Mengambil sprite dish dari library
            try {
                // Asumsi nama resep cocok dengan nama sprite (lowercase)
                String spriteName = o.getRecipe().getName().toLowerCase();
                // Jika sprite belum ada, akan default ke kotak magenta (aman)
                Image icon = SpriteLibrary.getInstance().getSprite(spriteName);
                if (icon != null) {
                    g.drawImage(icon, startX + cardWidth - 40, y + 20, 32, 32, null);
                }
            } catch (Exception e) {
                // Ignore sprite error
            }

            // 4. Timer Bar untuk Order (Opsional, garis hijau di bawah)
            g.setColor(new Color(50, 200, 50));
            g.fillRect(startX + 10, y + 45, cardWidth - 50, 6);
        }
    }

    @Override
    public void update() {
        repaint(); // Gambar ulang saat ada notifikasi dari Engine
    }
}