package view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import model.engine.GameEngine;
import model.orders.Order;
import view.Observer;

public class HUDPanel extends JPanel implements Observer {
    private final GameEngine engine;
    private final int INFO_OFFSET_X = 240;
    private Font pixelFont;
    private Font pixelFontSmall;

    private JButton btnExit;

    public HUDPanel(GameEngine engine, Runnable onExitClicked) {
        this.engine = engine;

        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 20f);
        this.pixelFontSmall = loadPixelFont("/resources/fonts/PressStart2P.ttf", 15f);

        this.setBackground(new Color(30, 30, 30));
        this.setLayout(null); 

        // 1. Recipe
        JButton btnRecipe = createPixelButton("RECIPE", new Color(41, 173, 255));
        btnRecipe.setBounds(10, 15, 110, 40);
        btnRecipe.addActionListener(e -> {
            showModelessDialog("RECIPE BOOK", getRecipeContent());
            this.getParent().requestFocusInWindow();
        });
        this.add(btnRecipe);

        // 2. Help
        JButton btnHelp = createPixelButton("HELP", new Color(0, 228, 54));
        btnHelp.setBounds(130, 15, 100, 40);
        btnHelp.addActionListener(e -> {
            showModelessDialog("HOW TO PLAY", getHelpContent());
            this.getParent().requestFocusInWindow();
        });
        this.add(btnHelp);

        // 3. Exit
        btnExit = createPixelButton("EXIT", new Color(255, 0, 77));
        btnExit.setBounds(800, 15, 100, 40);
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Quit to Main Menu?", "Exit Game", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (onExitClicked != null) onExitClicked.run();
            } else {
                this.getParent().requestFocusInWindow();
            }
        });
        this.add(btnExit);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (btnExit != null) {
            btnExit.setBounds(getWidth() - 120, 15, 100, 40);
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

    private JButton createPixelButton(String text, Color baseColor) {
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

                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRect(stroke, stroke, w - stroke*2, 3);
                g2.fillRect(stroke, stroke, 3, h - stroke*2);

                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 3, w - stroke*2, 3);
                g2.fillRect(w - stroke - 3, stroke, 3, h - stroke*2);

                g2.setColor(Color.WHITE);
                g2.setFont(pixelFontSmall);
                FontMetrics fm = g2.getFontMetrics();

                g2.setColor(Color.BLACK);
                g2.drawString(getText(), (w - fm.stringWidth(getText()))/2 + 2, (h - fm.getHeight())/2 + fm.getAscent() + 2);

                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (w - fm.stringWidth(getText()))/2, (h - fm.getHeight())/2 + fm.getAscent());

                g2.dispose();
            }
        };
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);
        return btn;
    }

    private void showModelessDialog(String title, String content) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, title);
        dialog.setModal(true);

        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(15, 15, 15, 15));
        textArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        textArea.setBackground(new Color(240, 240, 240));

        dialog.add(new JScrollPane(textArea));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String getRecipeContent() {
        return """
            === ITALIAN RECIPES ===
            
            [PASTA MARINARA]
               Pasta + Tomato
            
            [PASTA BOLOGNESE]
               Pasta + Meat
            
            [SEAFOOD PASTA]
               Pasta + Shrimp + Fish
            """;
    }

    private String getHelpContent() {
        return """
            === CONTROLS ===
            
            [PLAYER 1]
            Move    : W, A, S, D
            Action  : V
            Grab    : B
            Throw   : F
            
            [PLAYER 2]
            Move    : Arrows
            Action  : K
            Grab    : L
            Throw   : ;
            
            [SINGLE PLAYER]
            Switch  : C / TAB
            """;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(0, getHeight(), getWidth(), getHeight());

        drawTimer(g2d);
        drawScore(g2d);
        drawOrders(g2d);
    }

    private void drawTimer(Graphics2D g) {
        int time = engine.getClock().getTimeRemaining();
        if (time <= 30) g.setColor(new Color(255, 0, 77));
        else g.setColor(Color.WHITE);

        int xPos = INFO_OFFSET_X;
        g.setFont(pixelFontSmall);
        g.drawString("TIME", xPos, 20);
        g.setFont(pixelFont);
        String text = String.format("%02d:%02d", time / 60, time % 60);
        g.drawString(text, xPos, 35);
    }

    private void drawScore(Graphics2D g) {
        int score = engine.getOrders().getScore();
        int xPos = INFO_OFFSET_X;
        g.setColor(new Color(255, 204, 170));
        g.setFont(pixelFontSmall);
        g.drawString("SCORE", xPos, 55);
        g.setColor(new Color(255, 236, 39));
        g.setFont(pixelFont);
        g.drawString(String.valueOf(score), xPos, 70);
    }

    private void drawOrders(Graphics2D g) {
        List<Order> orders = engine.getOrders().getActiveOrders();

        int startX = getWidth() - 130;
        int y = 10;
        int cardWidth = 115;
        int cardHeight = 60;
        int gap = 5;

        for (Order o : orders) {
            startX -= (cardWidth + gap);
            if (startX < (INFO_OFFSET_X + 80)) {
                break;
            }

            g.setColor(new Color(255, 241, 232));
            g.fillRect(startX, y, cardWidth, cardHeight);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(3));
            g.drawRect(startX, y, cardWidth, cardHeight);

            int iconSize = 24;
            int iconX = startX + cardWidth - iconSize - 5;

            try {
                String spriteName = o.getRecipe().getName().toLowerCase();
                Image icon = SpriteLibrary.getInstance().getSprite(spriteName);
                if (icon != null) g.drawImage(icon, iconX, y + 8, iconSize, iconSize, null);
            } catch (Exception e) {}

            g.setColor(Color.BLACK);
            g.setFont(pixelFontSmall.deriveFont(15f));

            String name = o.getRecipe().getName().toUpperCase().replace("PASTA ", "");
            if (name.length() > 9) name = name.substring(0, 9);
            g.drawString(name, startX + 5, y + 25);

            int maxTime = 90;
            int timeLeft = o.getTimeLeft();
            int maxBarWidth = cardWidth - 10;
            int currentBarWidth = (int) ((double) timeLeft / maxTime * maxBarWidth);

            Color barColor;
            if (timeLeft > 30) barColor = new Color(0, 228, 54);
            else if (timeLeft > 15) barColor = new Color(255, 163, 0);
            else barColor = new Color(255, 0, 77);

            g.setColor(new Color(100, 100, 100));
            g.fillRect(startX + 5, y + 38, maxBarWidth, 10);

            g.setColor(barColor);
            if (currentBarWidth < 0) currentBarWidth = 0;
            g.fillRect(startX + 5, y + 38, currentBarWidth, 10);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(startX + 5, y + 38, maxBarWidth, 10);
        }
    }

    @Override
    public void update() {
        repaint();
    }
}