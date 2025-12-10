package view.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.engine.GameEngine;
import model.orders.Order;
import view.Observer;

public class HUDPanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int INFO_OFFSET_X = 240; 

    public HUDPanel(GameEngine engine, Runnable onExitClicked) {
        this.engine = engine;
        int mapWidth = engine.getWorld().getWidth() * 60;

        this.setPreferredSize(new Dimension(mapWidth, 80));
        this.setBackground(new Color(40, 40, 40));
        this.setLayout(null);

        // Tombol Recipe
        JButton btnRecipe = createSmallButton("Recipe");
        btnRecipe.setBounds(10, 10, 100, 30); 
        btnRecipe.addActionListener(e -> showModelessDialog("Recipe Book", getRecipeContent()));
        this.add(btnRecipe);

        // Tombol Help
        JButton btnHelp = createSmallButton("Help");
        btnHelp.setBounds(120, 10, 100, 30);
        btnHelp.addActionListener(e -> showModelessDialog("How to Play", getHelpContent()));
        this.add(btnHelp);

        // Tombol Exit
        JButton btnExit = createSmallButton("Exit");
        btnExit.setBounds(10, 45, 210, 25); 
        btnExit.setBackground(new Color(200, 60, 60)); 
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to quit to Main Menu?", 
                "Exit Game", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (onExitClicked != null) onExitClicked.run();
            }
        });
        this.add(btnExit);
    }

    private void showModelessDialog(String title, String content) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, title);
        dialog.setModal(false); 
        dialog.setFocusableWindowState(false); 

        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        dialog.add(new JScrollPane(textArea));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String getRecipeContent() {
        return """
            === RECIPE BOOK ===
            
            🍝 Pasta Marinara
               = Pasta + Tomato
            
            🍝 Pasta Bolognese
               = Pasta + Meat
            
            🍝 Pasta Frutti di Mare
               = Pasta + Shrimp + Fish
            """;
    }

    private String getHelpContent() {
        return """
            === CARA BERMAIN NIMONSCOOKED ===
            
            [MODE SINGLE PLAYER]
            W, A, S, D  : Gerak Chef
            E           : Interaksi (Potong/Cuci)
            F           : Ambil / Taruh (Otomatis)
            T           : Lempar
            C / TAB     : Ganti Chef
            
            [MODE MULTIPLAYER]
            Player 1 (Kiri):
               Gerak    : W, A, S, D
               Interaksi: V
               Ambil/Taruh: B
               Lempar   : F
            
            Player 2 (Kanan):
               Gerak    : Panah (Arrow Keys)
               Interaksi: K
               Ambil/Taruh: L
               Lempar   : ; (Titik Koma)
            
            [TUJUAN]
            Masak pesanan sesuai resep dan sajikan 
            sebelum waktu habis!
            """;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(100, 149, 237));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFocusable(false); 
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTimer(g2d);
        drawScore(g2d);
        drawOrders(g2d);
    }

    private void drawTimer(Graphics2D g) {
        int time = engine.getClock().getTimeRemaining();
        if (time <= 30) g.setColor(new Color(255, 80, 80));
        else g.setColor(Color.WHITE);

        int xPos = INFO_OFFSET_X;
        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String text = String.format("%02d:%02d", time / 60, time % 60);
        g.drawString(text, xPos, 50);
        
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("TIME LEFT", xPos + 2, 20);
    }

    private void drawScore(Graphics2D g) {
        int score = engine.getOrders().getScore();
        int xPos = INFO_OFFSET_X + 110;

        g.setColor(new Color(255, 215, 0)); 
        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String text = String.valueOf(score);
        g.drawString(text, xPos, 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("SCORE", xPos + 2, 20);
    }

    // --- FIX BAGIAN INI ---
    private void drawOrders(Graphics2D g) {
        List<Order> orders = engine.getOrders().getActiveOrders();
        int startX = getWidth() - 20;
        int y = 10;

        int cardWidth = 110;
        int cardHeight = 60;

        for (Order o : orders) {
            startX -= (cardWidth + 10);
            
            // 1. Gambar Kotak Kartu
            g.setColor(new Color(240, 240, 240));
            g.fillRoundRect(startX, y, cardWidth, cardHeight, 15, 15);
            
            // 2. Icon Makanan (Jika ada)
            int iconSize = 24; // Perkecil icon jadi 24px (sebelumnya 30)
            int iconX = startX + cardWidth - iconSize - 5; // Posisi Icon di Kanan

            try {
                String spriteName = o.getRecipe().getName().toLowerCase();
                Image icon = SpriteLibrary.getInstance().getSprite(spriteName);
                if (icon != null) {
                    g.drawImage(icon, iconX, y + 5, iconSize, iconSize, null);
                }
            } catch (Exception e) {
            }

            // 3. Teks Nama Makanan
            g.setColor(Color.BLACK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            String name = o.getRecipe().getName().toUpperCase();
            
            if (name.length() > 9) {
                name = name.substring(0, 9) + "..";
            }
            
            g.drawString(name, startX + 8, y + 22);
            

            // --- 4. BAR WAKTU DINAMIS ---
            int maxTime = 90; // Sesuai setting OrderManager di Main.java (90 detik)
            int timeLeft = o.getTimeLeft();
            
            // Hitung lebar bar agar pas di dalam kartu (padding 10px kiri kanan)
            int maxBarWidth = cardWidth - 20; 
            int currentBarWidth = (int) ((double) timeLeft / maxTime * maxBarWidth);
            
            // Warna berubah sesuai urgensi
            if (timeLeft > 30) g.setColor(new Color(46, 204, 113)); // Hijau
            else if (timeLeft > 15) g.setColor(new Color(241, 196, 15)); // Kuning
            else g.setColor(new Color(231, 76, 60)); // Merah

            // Gambar Bar Background (Abu-abu tipis)
            g.setColor(new Color(200, 200, 200));
            g.fillRect(startX + 10, y + 40, maxBarWidth, 8);

            // Gambar Bar Waktu (Berwarna)
            if (timeLeft > 30) g.setColor(new Color(46, 204, 113));
            else if (timeLeft > 15) g.setColor(new Color(241, 196, 15));
            else g.setColor(new Color(231, 76, 60));
            
            if (currentBarWidth < 0) currentBarWidth = 0;
            g.fillRect(startX + 10, y + 40, currentBarWidth, 8);
        }
    }

    @Override
    public void update() {
        repaint();
    }
}