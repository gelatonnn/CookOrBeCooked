package view.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import javax.swing.*;

public class HomePanel extends JPanel {
    private final BufferedImage backgroundImage;
    private Font pixelFont;

    public HomePanel(Runnable onStartSingle, Runnable onStartMulti) {
        // Load Background & Font
        this.backgroundImage = AssetManager.getInstance().getMenuBackground();
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 10f);

        // --- UBAH LAYOUT KE GRIDBAGLAYOUT (Agar posisi Center) ---
        setLayout(new GridBagLayout());

        // Buat Panel Container untuk menampung tombol-tombol
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setOpaque(false); // Transparan agar background terlihat

        // --- TAMBAHKAN TOMBOL KE CONTAINER ---

        // Jarak dari "Logo" (asumsi ada di background atas)
        // Kita beri sedikit ruang di atas tombol agar tidak terlalu nempel ke tengah atas
        buttonContainer.add(Box.createRigidArea(new Dimension(0, 50)));

        // 1. SINGLE PLAYER
        addButton(buttonContainer, "SINGLE PLAYER", new Color(41, 173, 255), onStartSingle);
        buttonContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. MULTIPLAYER
        addButton(buttonContainer, "MULTIPLAYER", new Color(255, 163, 0), onStartMulti);
        buttonContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // 3. HOW TO PLAY
        addButton(buttonContainer, "HOW TO PLAY", new Color(0, 228, 54),
                () -> showModelessDialog("Cara Bermain", getHelpContent()));
        buttonContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // 4. EXIT GAME
        addButton(buttonContainer, "EXIT GAME", new Color(255, 0, 77), () -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Keluar dari permainan?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });

        // --- TAMBAHKAN CONTAINER KE PANEL UTAMA (CENTER) ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Pastikan di tengah layar
        add(buttonContainer, gbc);
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

    // Helper untuk menambah tombol ke container spesifik
    private void addButton(JPanel parent, String text, Color baseColor, Runnable action) {
        JButton btn = createStyledButton(text, baseColor);
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });

        // Wrapper agar tombol tidak stretch di BoxLayout
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(btn);
        // Set alignment X agar wrapper di tengah container BoxLayout
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        parent.add(wrapper);
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                // Matikan Antialiasing untuk gaya Pixel Art
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

                Color color = baseColor;
                if (getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2); // Efek tekan
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

                // 3. Highlight & Shadow (Efek 3D)
                g2.setColor(new Color(255, 255, 255, 100)); // Putih Transparan
                g2.fillRect(stroke, stroke, w - stroke*2, 4); // Atas
                g2.fillRect(stroke, stroke, 4, h - stroke*2); // Kiri

                g2.setColor(new Color(0, 0, 0, 50)); // Hitam Transparan
                g2.fillRect(stroke, h - stroke - 4, w - stroke*2, 4); // Bawah
                g2.fillRect(w - stroke - 4, stroke, 4, h - stroke*2); // Kanan

                // 4. Teks
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();

                // Shadow Teks
                g2.setColor(Color.BLACK);
                g2.drawString(getText(), x + 2, y + 2);

                // Teks Utama
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(pixelFont);
        btn.setPreferredSize(new Dimension(320, 55));
        btn.setMaximumSize(new Dimension(320, 55)); // Agar tidak stretch
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
            // Gambar background memenuhi layar (Full Screen)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(new Color(30, 30, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
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