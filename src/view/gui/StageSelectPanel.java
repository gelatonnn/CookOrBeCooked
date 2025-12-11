package view.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.engine.GameConfig;

public class StageSelectPanel extends JPanel {
    private BufferedImage backgroundImage;

    public StageSelectPanel(int unlockedLevel, Consumer<GameConfig> onStageSelected, Runnable onBack) {
        // 1. Setup Layout & Background
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        loadBackground(); 

        add(Box.createRigidArea(new Dimension(0, 190))); 

        // --- STAGE 1: EASY ---
        add(createStageButton("Stage 1: Easy", "Target: 3 Orders | 4 Mins", 
            new Color(100, 149, 237),
            true,
            () -> onStageSelected.accept(new GameConfig("Stage 1", 240, 5, 3, 0, false))));
        
        add(Box.createRigidArea(new Dimension(0, 15))); // Spasi antar tombol

        // --- STAGE 2: MEDIUM ---
        boolean isS2Unlocked = unlockedLevel >= 2;
        add(createStageButton("Stage 2: Medium", "Target: 4 Orders | 4.5 Mins", 
            new Color(255, 140, 0), // Oranye
            isS2Unlocked,
            () -> onStageSelected.accept(new GameConfig("Stage 2", 270, 5, 4, 0, false))));

        add(Box.createRigidArea(new Dimension(0, 15)));

        // --- STAGE 3: SURVIVAL ---
        boolean isS3Unlocked = unlockedLevel >= 3;
        add(createStageButton("Stage 3: Survival", "Survive 5 Mins | Min Score: 500", 
            new Color(46, 204, 113), // Hijau
            isS3Unlocked,
            () -> onStageSelected.accept(new GameConfig("Stage 3", 300, 3, 0, 500, true))));

        add(Box.createRigidArea(new Dimension(0, 15)));

        // --- BACK BUTTON ---
        JButton btnBack = createSimpleButton("Back to Menu", new Color(200, 60, 60));
        btnBack.addActionListener(e -> onBack.run());
        
        // Wrapper agar tombol back rapi di tengah
        JPanel backWrapper = new JPanel();
        backWrapper.setOpaque(false);
        backWrapper.add(btnBack);
        add(backWrapper);

        add(Box.createVerticalGlue());
    }

    private void loadBackground() {
        try {
            // Mencoba load dari classpath (folder resources)
            java.net.URL url = getClass().getResource("/resources/SelectStageBackground.png");
            // Fallback jika file ada di root source
            if (url == null) url = getClass().getResource("/SelectStageBackground.png");
            
            if (url != null) {
                this.backgroundImage = ImageIO.read(url);
            } else {
                System.err.println("Warning: SelectStageBackground.png not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Gambar memenuhi panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback warna jika gambar gagal load
            g.setColor(new Color(30, 30, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Method Membuat Tombol Stage (Custom Style mirip HomePanel)
    private JButton createStageButton(String title, String desc, Color baseColor, boolean unlocked, Runnable action) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Tentukan warna berdasarkan status (Locked/Unlocked/Hover)
                if (!unlocked) {
                    g2.setColor(Color.GRAY);
                } else if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }

                // Gambar Background Tombol (Rounded)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Gambar Border Putih
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);

                // Gambar Teks (Manual, agar bisa beda ukuran font)
                g2.setColor(Color.WHITE);
                
                // 1. Gambar Judul (Besar)
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fmTitle = g2.getFontMetrics();
                int xTitle = (getWidth() - fmTitle.stringWidth(title)) / 2;
                int yTitle = (getHeight() / 2) - 5; // Sedikit ke atas
                g2.drawString(title, xTitle, yTitle);

                // 2. Gambar Deskripsi (Kecil)
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fmDesc = g2.getFontMetrics();
                String drawDesc = unlocked ? desc : "(LOCKED)";
                int xDesc = (getWidth() - fmDesc.stringWidth(drawDesc)) / 2;
                int yDesc = (getHeight() / 2) + 15; // Sedikit ke bawah
                g2.drawString(drawDesc, xDesc, yDesc);

                g2.dispose();
            }
        };

        // Konfigurasi Dasar Tombol
        btn.setPreferredSize(new Dimension(350, 70));
        btn.setMaximumSize(new Dimension(350, 70));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (unlocked) {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> action.run());
        } else {
            btn.setEnabled(false);
        }

        return btn;
    }

    // Helper untuk tombol biasa (seperti tombol Back)
    private JButton createSimpleButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) g2.setColor(color.darker());
                else if (getModel().isRollover()) g2.setColor(color.brighter());
                else g2.setColor(color);

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}