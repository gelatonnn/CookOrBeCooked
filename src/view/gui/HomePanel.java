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

        this.backgroundImage = AssetManager.getInstance().getMenuBackground();

        // Load font pixel (pastikan file ada)
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 10f);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Jarak dari atas layar ke tombol pertama
        add(Box.createRigidArea(new Dimension(0, 185)));

        // 1. SINGLE PLAYER - Warna Biru Retro (PICO-8 Blue)
        addButton("SINGLE PLAYER", new Color(41, 173, 255), onStartSingle);

        // --- PENGATURAN JARAK ANTAR TOMBOL ---
        // Ubah angka '10' di bawah ini untuk mengatur jarak (makin besar makin jauh)
        add(Box.createRigidArea(new Dimension(0, 3)));

        // 2. MULTIPLAYER - Warna Orange Retro (PICO-8 Orange)
        addButton("MULTIPLAYER", new Color(255, 163, 0), onStartMulti);
        add(Box.createRigidArea(new Dimension(0, 3)));

        // 3. HOW TO PLAY - Warna Hijau Retro (PICO-8 Green)
        addButton("HOW TO PLAY", new Color(0, 228, 54),
                () -> showModelessDialog("Cara Bermain", getHelpContent()));
        add(Box.createRigidArea(new Dimension(0, 3)));

        // 4. EXIT GAME - Warna Merah Retro (PICO-8 Red)
        addButton("EXIT GAME", new Color(255, 0, 77), () -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Keluar dari permainan?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });

        add(Box.createVerticalGlue());
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

                // 1. MATIKAN Antialiasing (Wajib untuk Pixel Art agar tajam)
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

                // Logika Warna saat ditekan
                Color color = baseColor;
                if (getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2); // Efek tombol turun fisik
                } else if (getModel().isRollover()) {
                    color = baseColor.brighter();
                }

                int w = getWidth();
                int h = getHeight();

                // 2. Gambar Kotak Dasar
                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                // 3. Efek Bevel (3D Highlight & Shadow ala Retro)
                int stroke = 4; // Ketebalan border

                // Border Luar Hitam
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                // Highlight (Atas & Kiri - Warna Putih Transparan)
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRect(stroke, stroke, w - stroke*2, 4); // Strip Atas
                g2.fillRect(stroke, stroke, 4, h - stroke*2); // Strip Kiri

                // Shadow (Bawah & Kanan - Warna Hitam Transparan)
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 4, w - stroke*2, 4); // Strip Bawah
                g2.fillRect(w - stroke - 4, stroke, 4, h - stroke*2); // Strip Kanan

                // 4. Teks Putih
                g2.setColor(Color.WHITE); // <-- KEMBALI KE PUTIH
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();

                // Bayangan Teks Hitam (Drop Shadow) agar lebih terbaca
                g2.setColor(Color.BLACK);
                g2.drawString(getText(), x + 2, y + 2);

                // Teks Utama Putih
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(pixelFont);
        btn.setPreferredSize(new Dimension(320, 55));
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