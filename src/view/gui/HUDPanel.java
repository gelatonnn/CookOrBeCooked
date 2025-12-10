package view.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.engine.GameEngine;
import model.orders.Order;
import view.Observer;

public class HUDPanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int INFO_OFFSET_X = 360; 

    public HUDPanel(GameEngine engine, Runnable onExitClicked) {
        this.engine = engine;
        this.setPreferredSize(new Dimension(800, 80));
        this.setBackground(new Color(40, 40, 40));
        this.setLayout(null); 

        // --- 1. Tombol Recipe ---
        JButton btnRecipe = createSmallButton("Recipe");
        btnRecipe.setBounds(10, 10, 100, 30); 
        btnRecipe.addActionListener(e -> showModelessDialog("Recipe Book", getRecipeContent()));
        this.add(btnRecipe);

        // --- 2. Tombol How To Play ---
        JButton btnHelp = createSmallButton("Help");
        btnHelp.setBounds(120, 10, 100, 30);
        btnHelp.addActionListener(e -> showModelessDialog("How to Play", getHelpContent()));
        this.add(btnHelp);

        // --- 3. Tombol Exit ---
        JButton btnExit = createSmallButton("Exit");
        btnExit.setBounds(10, 45, 210, 25); 
        btnExit.setBackground(new Color(200, 60, 60)); 
        btnExit.addActionListener(e -> {
            // Khusus Exit tetap pakai Modal (Blocking) karena mau keluar
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to quit to Main Menu?", 
                "Exit Game", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (onExitClicked != null) onExitClicked.run();
            }
        });
        this.add(btnExit);
    }

    // --- Helper untuk membuat JDialog Non-Modal (Game Tetap Jalan) ---
    private void showModelessDialog(String title, String content) {
        // Cari Window utama (JFrame) sebagai parent
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, title);
        
        // PENTING 1: Set Modal ke FALSE agar game tidak berhenti
        dialog.setModal(false); 
        
        // PENTING 2: Agar dialog tidak mencuri fokus keyboard (WASD tetap jalan di game)
        dialog.setFocusableWindowState(false); 

        // Isi konten teks
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        dialog.add(new JScrollPane(textArea));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this); // Muncul di tengah
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
            
            [KONTROL]
            W, A, S, D  : Bergerak
            E           : Interaksi (Ambil/Taruh/Proses)
            CTRL + WASD : Dash (Lari Cepat)
            C           : Ganti Chef
            O           : Place Item Down
            P           : Pick Up Item
            T           : Throw Item
            
            [TUJUAN]
            1. Ambil bahan dari kotak (Crate).
            2. Potong bahan di talenan (Cutting Board).
            3. Masak di kompor (Stove) sampai matang.
            4. Letakkan di piring (Plate).
            5. Antar ke loket penyajian (Serving Window).
            
            Hati-hati! Jangan sampai masakan gosong!
            """;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(100, 149, 237));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        
        // PENTING 3: Tombol tidak boleh fokus, agar WASD langsung jalan 
        // tanpa harus klik peta lagi setelah klik tombol.
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
        int xPos = INFO_OFFSET_X + 150;

        g.setColor(new Color(255, 215, 0)); 
        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String text = String.valueOf(score);
        g.drawString(text, xPos, 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("SCORE", xPos + 2, 20);
    }

    // Update method ini di HUDPanel.java
    private void drawOrders(Graphics2D g) {
        List<Order> orders = engine.getOrders().getActiveOrders();
        
        int cardWidth = 130;
        int cardHeight = 65;
        int spacing = 10;
        
        // Mulai menggambar dari pojok kanan atas
        int startX = getWidth() - 20; 
        int y = 10;

        for (Order o : orders) {
            // Geser posisi X ke kiri untuk setiap order baru
            startX -= (cardWidth + spacing);

            // 1. Gambar Background Kartu Order
            g.setColor(new Color(245, 245, 245)); // Putih gading
            g.fillRoundRect(startX, y, cardWidth, cardHeight, 10, 10);
            
            // Border Kartu
            g.setColor(new Color(200, 200, 200));
            g.drawRoundRect(startX, y, cardWidth, cardHeight, 10, 10);

            // 2. Gambar Nama Makanan
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            String name = o.getRecipe().getName();
            // Truncate nama panjang biar muat (misal: "Pasta Fru..")
            if (name.length() > 13) name = name.substring(0, 11) + "..";
            
            g.drawString(name, startX + 10, y + 22);

            // 3. Gambar Icon Makanan (Jika ada sprite)
            try {
                // Asumsi nama file sprite sama dengan nama resep lowercase (misal: pasta marinara)
                // Anda mungkin perlu menyesuaikan nama file di folder resource Anda
                String spriteName = o.getRecipe().getName().toLowerCase(); 
                Image icon = SpriteLibrary.getInstance().getSprite(spriteName); // Pastikan SpriteLibrary handle ini
                
                // Jika tidak ada icon khusus, bisa pakai logic fallback atau skip
                if (icon != null) {
                    g.drawImage(icon, startX + 10, y + 28, 24, 24, null);
                }
            } catch (Exception e) {
                // Ignore error image rendering
            }

            // 4. Gambar Progress Bar Waktu (Paling Penting)
            int timeLeft = o.getTimeLeft();
            int maxTime = 60; // Asumsi waktu order 60 detik (bisa diambil dari Order.java jika ada getter maxTime)
            
            // Hitung lebar bar
            int barWidth = cardWidth - 20;
            int barHeight = 8;
            int barFill = (int) ((double) timeLeft / maxTime * barWidth);
            
            // Tentukan Warna Berdasarkan Sisa Waktu
            Color barColor;
            if (timeLeft > 30) barColor = new Color(46, 204, 113);      // Hijau (Aman)
            else if (timeLeft > 15) barColor = new Color(241, 196, 15); // Kuning (Waspada)
            else barColor = new Color(231, 76, 60);                     // Merah (Bahaya)

            // Gambar Background Bar (Abu-abu)
            g.setColor(new Color(220, 220, 220));
            g.fillRoundRect(startX + 10, y + 45, barWidth, barHeight, 4, 4);
            
            // Gambar Fill Bar (Warna)
            g.setColor(barColor);
            if (barFill < 0) barFill = 0; // Cegah error negatif
            g.fillRoundRect(startX + 10, y + 45, barFill, barHeight, 4, 4);
            
            // Teks Waktu Kecil di Kanan Bar
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            // g.drawString(timeLeft + "s", startX + cardWidth - 25, y + 40); // Opsional jika ingin angka
        }
    }
    
    @Override
    public void update() {
        repaint();
    }
}