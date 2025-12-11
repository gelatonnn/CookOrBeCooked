package view.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.engine.GameConfig;

public class StageSelectPanel extends JPanel {
    private BufferedImage backgroundImage;
    private Font pixelFont;
    private Font pixelFontSmall;

    public StageSelectPanel(int unlockedLevel, Consumer<GameConfig> onStageSelected, Runnable onBack) {
        // 1. Load Font & Background
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 10f);
        this.pixelFontSmall = loadPixelFont("/resources/fonts/PressStart2P.ttf", 7f); // Ukuran kecil untuk deskripsi
        loadBackground();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Jarak dari atas (sesuaikan dengan desain background Anda)
        add(Box.createRigidArea(new Dimension(0, 190)));

        // --- STAGE 1: EASY (Warna Biru Retro) ---
        add(createStageButton("STAGE 1: EASY", "TARGET: 3 ORDERS",
                new Color(41, 173, 255),
                true,
                () -> onStageSelected.accept(new GameConfig("Stage 1", 240, 5, 3, 0, false))));

        add(Box.createRigidArea(new Dimension(0, 10))); // Spasi antar tombol

        // --- STAGE 2: MEDIUM (Warna Oranye Retro) ---
        boolean isS2Unlocked = unlockedLevel >= 2;
        add(createStageButton("STAGE 2: MEDIUM", "TARGET: 4 ORDERS",
                new Color(255, 163, 0),
                isS2Unlocked,
                () -> onStageSelected.accept(new GameConfig("Stage 2", 270, 5, 4, 0, false))));

        add(Box.createRigidArea(new Dimension(0, 10)));

        // --- STAGE 3: SURVIVAL (Warna Hijau Retro) ---
        boolean isS3Unlocked = unlockedLevel >= 3;
        add(createStageButton("STAGE 3: HARD", "SURVIVE 5 MINS",
                new Color(0, 228, 54),
                isS3Unlocked,
                () -> onStageSelected.accept(new GameConfig("Stage 3", 300, 3, 0, 500, true))));

        add(Box.createRigidArea(new Dimension(0, 10)));

        // --- BACK BUTTON (Warna Merah Retro) ---
        JButton btnBack = createSimpleButton("BACK TO MENU", new Color(255, 0, 77));
        btnBack.addActionListener(e -> onBack.run());

        // Wrapper agar tombol back rapi di tengah
        JPanel backWrapper = new JPanel();
        backWrapper.setOpaque(false);
        backWrapper.add(btnBack);
        add(backWrapper);

        add(Box.createVerticalGlue());
    }

    // Load Font Helper
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

    private void loadBackground() {
        try {
            java.net.URL url = getClass().getResource("/resources/SelectStageBackground.png");
            if (url == null) url = getClass().getResource("/SelectStageBackground.png");

            if (url != null) {
                this.backgroundImage = ImageIO.read(url);
            } else {
                // System.err.println("Warning: SelectStageBackground.png not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(new Color(30, 30, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // --- TOMBOL STAGE GAYA PIXEL ART ---
    private JButton createStageButton(String title, String desc, Color baseColor, boolean unlocked, Runnable action) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                // Matikan Antialiasing
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

                Color color = unlocked ? baseColor : new Color(100, 100, 100); // Abu-abu jika terkunci

                // Efek Tekan
                if (unlocked && getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2);
                } else if (unlocked && getModel().isRollover()) {
                    color = baseColor.brighter();
                }

                int w = getWidth();
                int h = getHeight();
                int stroke = 4;

                // 1. Background Kotak
                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                // 2. Border Hitam Tebal
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                // 3. Efek Bevel 3D
                // Highlight Putih (Atas & Kiri)
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRect(stroke, stroke, w - stroke*2, 4);
                g2.fillRect(stroke, stroke, 4, h - stroke*2);

                // Shadow Hitam (Bawah & Kanan)
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 4, w - stroke*2, 4);
                g2.fillRect(w - stroke - 4, stroke, 4, h - stroke*2);

                // 4. Teks
                g2.setColor(Color.WHITE);

                // Judul (Font Besar)
                g2.setFont(pixelFont);
                FontMetrics fmTitle = g2.getFontMetrics();
                int xTitle = (w - fmTitle.stringWidth(title)) / 2;
                int yTitle = (h / 2) - 8;

                // Shadow Teks Hitam
                g2.setColor(Color.BLACK);
                g2.drawString(title, xTitle + 2, yTitle + 2);
                g2.setColor(Color.WHITE);
                g2.drawString(title, xTitle, yTitle);

                // Deskripsi (Font Kecil)
                g2.setFont(pixelFontSmall);
                FontMetrics fmDesc = g2.getFontMetrics();
                String drawDesc = unlocked ? desc : "(LOCKED)";
                int xDesc = (w - fmDesc.stringWidth(drawDesc)) / 2;
                int yDesc = (h / 2) + 12;

                g2.setColor(new Color(220, 220, 220)); // Putih agak gelap untuk deskripsi
                g2.drawString(drawDesc, xDesc, yDesc);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(320, 60)); // Ukuran konsisten
        btn.setMaximumSize(new Dimension(320, 60));
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

    // --- TOMBOL SIMPLE (BACK) ---
    private JButton createSimpleButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

                Color color = baseColor;
                if (getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2);
                } else if (getModel().isRollover()) {
                    color = baseColor.brighter();
                }

                int w = getWidth();
                int h = getHeight();
                int stroke = 3;

                // Background
                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                // Border
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                // Highlight/Shadow
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRect(stroke, stroke, w - stroke*2, 3);
                g2.fillRect(stroke, stroke, 3, h - stroke*2);

                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 3, w - stroke*2, 3);
                g2.fillRect(w - stroke - 3, stroke, 3, h - stroke*2);

                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(pixelFont);
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(Color.BLACK);
                g2.drawString(getText(), x+2, y+2);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}