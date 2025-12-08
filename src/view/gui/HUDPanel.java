package view.gui;

import model.engine.GameEngine;
import model.orders.Order;
import view.Observer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
            CONTROLS:
            [W, A, S, D] : Move Chef
            [P] : Pick Up Item
            [E] : Interact / Use Station
            [O] : Place Item Down
            [T] : Throw Item
            [TAB] : Switch Chef
            
            TIPS:
            - Masak Pasta di Boiling Pot (Panci).
            - Masak Daging/Ikan di Frying Pan (Wajan).
            - Jangan lupa angkat sebelum GOSONG!
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

    private void drawOrders(Graphics2D g) {
        List<Order> orders = engine.getOrders().getActiveOrders();
        int startX = getWidth() - 20;
        int y = 10;
        int cardWidth = 140;
        int cardHeight = 60;

        for (Order o : orders) {
            startX -= (cardWidth + 10);
            g.setColor(new Color(240, 240, 240));
            g.fillRoundRect(startX, y, cardWidth, cardHeight, 15, 15);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String name = o.getRecipe().getName();
            if (name.length() > 14) name = name.substring(0, 14) + "..";
            g.drawString(name, startX + 10, y + 25);
            try {
                String spriteName = o.getRecipe().getName().toLowerCase();
                Image icon = SpriteLibrary.getInstance().getSprite(spriteName);
                if (icon != null) g.drawImage(icon, startX + cardWidth - 40, y + 20, 32, 32, null);
            } catch (Exception e) {}
            g.setColor(new Color(50, 200, 50));
            g.fillRect(startX + 10, y + 45, cardWidth - 50, 6);
        }
    }

    @Override
    public void update() {
        repaint();
    }
}