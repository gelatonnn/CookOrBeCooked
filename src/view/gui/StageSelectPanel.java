package view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import model.engine.GameConfig;

public class StageSelectPanel extends JPanel {
    private BufferedImage backgroundImage;
    private Font pixelFont;
    private Font pixelFontSmall;

    public StageSelectPanel(int unlockedLevel, Consumer<GameConfig> onStageSelected, Runnable onBack) {
        // 1. Load Font & Background
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 17f);
        this.pixelFontSmall = loadPixelFont("/resources/fonts/PressStart2P.ttf", 13f); // Ukuran kecil untuk deskripsi
        loadBackground();

        // --- UBAH LAYOUT KE GRIDBAGLAYOUT (Agar Posisi Center) ---
        setLayout(new GridBagLayout());

        // Container vertikal untuk menampung tombol
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false); // Transparan

        // Spacer Atas (Agar tidak menutupi logo di background jika ada)
        container.add(Box.createRigidArea(new Dimension(0, 80)));

        // --- STAGE 1: EASY ---
        JButton btnStage1 = createStageButton("STAGE 1: EASY", "TARGET: 3 ORDERS",
                new Color(41, 173, 255),
                true,
                () -> onStageSelected.accept(new GameConfig("Stage 1", 240, 3, 3, 0, false)));
        container.add(btnStage1);

        container.add(Box.createRigidArea(new Dimension(0, 15))); // Jarak antar tombol

        // --- STAGE 2: MEDIUM ---
        boolean isS2Unlocked = unlockedLevel >= 2;
        JButton btnStage2 = createStageButton("STAGE 2: MEDIUM", "TARGET: 4 ORDERS",
                new Color(255, 163, 0),
                isS2Unlocked,
                () -> onStageSelected.accept(new GameConfig("Stage 2", 270, 3, 4, 0, false)));
        container.add(btnStage2);

        container.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- STAGE 3: SURVIVAL ---
        boolean isS3Unlocked = unlockedLevel >= 3;
        JButton btnStage3 = createStageButton("STAGE 3: HARD", "SURVIVE 5 MINS",
                new Color(0, 228, 54),
                isS3Unlocked,
                () -> onStageSelected.accept(new GameConfig("Stage 3", 300, 3, 0, 500, true)));
        container.add(btnStage3);

        container.add(Box.createRigidArea(new Dimension(0, 30))); // Jarak ke tombol Back

        // --- BACK BUTTON ---
        JButton btnBack = createSimpleButton("BACK TO MENU", new Color(255, 0, 77));
        btnBack.addActionListener(e -> onBack.run());

        // Wrapper agar tombol back rapi di tengah container
        JPanel backWrapper = new JPanel();
        backWrapper.setOpaque(false);
        backWrapper.add(btnBack);
        // Pastikan wrapper align center di dalam container
        backWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(backWrapper);

        // Tambahkan container ke Panel Utama (Center)
        add(container);
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

    private void loadBackground() {
        try {
            java.net.URL url = getClass().getResource("/resources/SelectStageBackground.png");
            if (url == null) url = getClass().getResource("/SelectStageBackground.png");

            if (url != null) {
                this.backgroundImage = ImageIO.read(url);
            } else {
                // System.err.println("SelectStageBackground.png not found.");
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

    private JButton createStageButton(String title, String desc, Color baseColor, boolean unlocked, Runnable action) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

                Color color = unlocked ? baseColor : new Color(100, 100, 100);

                if (unlocked && getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2);
                } else if (unlocked && getModel().isRollover()) {
                    color = baseColor.brighter();
                }

                int w = getWidth();
                int h = getHeight();
                int stroke = 4;

                // Background
                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                // Border Hitam
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                // Highlight
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRect(stroke, stroke, w - stroke*2, 4);
                g2.fillRect(stroke, stroke, 4, h - stroke*2);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 4, w - stroke*2, 4);
                g2.fillRect(w - stroke - 4, stroke, 4, h - stroke*2);

                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(pixelFont);
                FontMetrics fmTitle = g2.getFontMetrics();
                int xTitle = (w - fmTitle.stringWidth(title)) / 2;
                int yTitle = (h / 2);

                g2.setColor(Color.BLACK);
                g2.drawString(title, xTitle + 2, yTitle + 2);
                g2.setColor(Color.WHITE);
                g2.drawString(title, xTitle, yTitle);

                g2.setFont(pixelFontSmall);
                FontMetrics fmDesc = g2.getFontMetrics();
                String drawDesc = unlocked ? desc : "(LOCKED)";
                int xDesc = (w - fmDesc.stringWidth(drawDesc)) / 2;
                int yDesc = (h / 2) + 15;

                g2.setColor(new Color(220, 220, 220)); 
                g2.drawString(drawDesc, xDesc, yDesc);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(400,75)); 
        btn.setMaximumSize(new Dimension(400,75));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); // Agar di tengah container

        if (unlocked) {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> action.run());
        } else {
            btn.setEnabled(false);
        }

        return btn;
    }

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

                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRect(stroke, stroke, w - stroke*2, 3);
                g2.fillRect(stroke, stroke, 3, h - stroke*2);

                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 3, w - stroke*2, 3);
                g2.fillRect(w - stroke - 3, stroke, 3, h - stroke*2);

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
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}