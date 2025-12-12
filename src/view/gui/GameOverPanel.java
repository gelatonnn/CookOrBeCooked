package view.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GameOverPanel extends JPanel {
    private BufferedImage backgroundImage;
    private Font pixelFont;
    
    public GameOverPanel(int finalScore, boolean isWin, Runnable onBackToMenu) {
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 30f);
        
        String bgPath = isWin ? "/resources/StageClearBackground.png" : "/resources/GameOverBackground.png";
        loadBackground(bgPath);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 3. PENGATURAN POSISI (PADDING ATAS)
        this.add(Box.createRigidArea(new Dimension(0, 260))); 

        //SCORE LABEL
        JLabel scoreLabel = new JLabel("FINAL SCORE: " + finalScore) {
             @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                
                String text = getText();
                FontMetrics fm = g2.getFontMetrics(pixelFont);
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = fm.getAscent();

                g2.setFont(pixelFont);
                g2.setColor(Color.BLACK);
                g2.drawString(text, x + 3, y + 3); 
                // Teks Putih
                g2.setColor(Color.WHITE);
                g2.drawString(text, x, y);
            }
        };
        scoreLabel.setFont(pixelFont);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setPreferredSize(new Dimension(800, 50));
        scoreLabel.setMaximumSize(new Dimension(800, 50));
        this.add(scoreLabel);

        this.add(Box.createRigidArea(new Dimension(0, 30)));

        // TOMBOL
        Color btnColor = isWin ? new Color(0, 228, 54) : new Color(255, 0, 77);
        String btnText = "BACK TO MENU"; 

        JButton btnBack = createStyledButton(btnText, btnColor);
        btnBack.addActionListener(e -> onBackToMenu.run());
        
        JPanel btnWrapper = new JPanel();
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnBack);
        this.add(btnWrapper);

        this.add(Box.createVerticalGlue());
    }

    //Helper Methods
    private void loadBackground(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                this.backgroundImage = ImageIO.read(is);
            } else {
                System.err.println("Background image not found at: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    //Button Style
    private JButton createStyledButton(String text, Color baseColor) {
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
                int stroke = 4;

                // 1. Background
                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                // 2. Border Hitam Tebal
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                // 3. Highlight & Shadow 
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRect(stroke, stroke, w - stroke*2, 4);
                g2.fillRect(stroke, stroke, 4, h - stroke*2);

                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 4, w - stroke*2, 4);
                g2.fillRect(w - stroke - 4, stroke, 4, h - stroke*2);

                // 4. Text Shadow & Text
                g2.setFont(pixelFont);
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(Color.BLACK);
                g2.drawString(getText(), x + 2, y + 2);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(320, 60));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(new Color(20, 20, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}