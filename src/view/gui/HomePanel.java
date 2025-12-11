package view.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class HomePanel extends JPanel {
    private final BufferedImage backgroundImage;

    public HomePanel(Runnable onStartSingle, Runnable onStartMulti) {
        
        this.backgroundImage = AssetManager.getInstance().getMenuBackground();
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createRigidArea(new Dimension(0, 185))); 

        addButton("SINGLE PLAYER (Switch Chef)", new Color(100, 149, 237), onStartSingle);
        add(Box.createRigidArea(new Dimension(0, 0))); 
        
        addButton("MULTIPLAYER (Local Co-op)", new Color(255, 140, 0), onStartMulti);
        add(Box.createRigidArea(new Dimension(0, 0))); 
        
        addButton("HOW TO PLAY", new Color(46, 204, 113), 
            () -> showModelessDialog("Cara Bermain", getHelpContent()));
        add(Box.createRigidArea(new Dimension(0, 0))); 
        
        addButton("EXIT GAME", new Color(200, 60, 60), () -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Keluar dari permainan?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });

        add(Box.createVerticalGlue());
    }

    private void addButton(String text, Color baseColor, Runnable action) {
        JButton btn = createStyledButton(text, baseColor);
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });
        
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false); 
        wrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); 
        wrapper.add(btn);
        
        add(wrapper);
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(320, 50));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(new Color(30, 30, 30));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void showModelessDialog(String title, String content) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, title);
        dialog.setModal(true);
        
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        dialog.add(new JScrollPane(textArea));
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
}