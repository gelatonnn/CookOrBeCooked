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
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer; // Import Timer Swing

public class HomePanel extends JPanel {
    private final BufferedImage backgroundImage;
    private Font pixelFont;

    // --- ANIMATION VARIABLES ---
    private Timer animationTimer;
    private float animationTime = 0f; // Waktu berjalan untuk sinus

    public HomePanel(Runnable onStartSingle, Runnable onStartMulti) {
        this.backgroundImage = AssetManager.getInstance().getMenuBackground();
        // Load font pixel
        this.pixelFont = loadPixelFont("/resources/fonts/PressStart2P.ttf", 22.5f);

        // 1. SETUP TIMER ANIMASI (60 FPS)
        // Timer ini akan memanggil repaint() setiap 16ms
        animationTimer = new Timer(16, e -> {
            animationTime += 0.05f; // Kecepatan animasi
            repaint(); // Gambar ulang panel beserta tombol-tombolnya
        });
        animationTimer.start();

        // 2. GUNAKAN GRIDBAGLAYOUT
        setLayout(new GridBagLayout());

        // 3. BUAT CONTAINER
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // 4. ATUR JARAK DARI ATAS (Spacer)
        container.add(Box.createRigidArea(new Dimension(0, 120)));

        // 5. TAMBAHKAN TOMBOL (Dengan Index untuk variasi animasi)

        // SINGLE PLAYER (Index 0)
        addButton(container, "SINGLE PLAYER", new Color(41, 173, 255), onStartSingle, 0);
        container.add(Box.createRigidArea(new Dimension(0, 15)));

        // MULTIPLAYER (Index 1)
        addButton(container, "MULTIPLAYER", new Color(255, 163, 0), onStartMulti, 1);
        container.add(Box.createRigidArea(new Dimension(0, 15)));

        // HOW TO PLAY (Index 2)
        addButton(container, "HOW TO PLAY", new Color(0, 228, 54),
                () -> showModelessDialog("Cara Bermain", getHelpContent()), 2);
        container.add(Box.createRigidArea(new Dimension(0, 15)));

        // EXIT GAME (Index 3)
        addButton(container, "EXIT GAME", new Color(255, 0, 77), () -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Keluar dari permainan?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        }, 3);

        // 6. MASUKKAN CONTAINER KE PANEL UTAMA
        add(container);
    }

    // Perlu mematikan timer jika panel tidak digunakan (opsional tapi good practice)
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    // Update method: Menambahkan parameter 'int index'
    private void addButton(JPanel parent, String text, Color baseColor, Runnable action, int index) {
        JButton btn = createStyledButton(text, baseColor, index);
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(btn);
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

    // Update method: Menambahkan logika animasi di paintComponent
    private JButton createStyledButton(String text, Color baseColor, int index) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

                // --- LOGIKA ANIMASI MELAYANG ---
                // Amplitude: Seberapa jauh naik turunnya (4 pixel)
                // Speed: Diatur oleh animationTime
                // Phase: (index * 0.5) membuat tombol tidak bergerak serentak (efek gelombang)
                double offsetY = Math.sin(animationTime + (index * 0.5)) * 4.0;

                // Geser koordinat gambar ke atas/bawah
                g2.translate(0, offsetY);

                // Logika Warna saat ditekan
                Color color = baseColor;
                if (getModel().isPressed()) {
                    color = baseColor.darker();
                    g2.translate(2, 2); // Efek tekan fisik
                } else if (getModel().isRollover()) {
                    color = baseColor.brighter();
                }

                int w = getWidth();
                int h = getHeight();

                // Gambar Kotak Dasar
                g2.setColor(color);
                g2.fillRect(0, 0, w, h);

                // Efek Bevel (3D Highlight & Shadow ala Retro)
                int stroke = 4;

                // Border Luar Hitam
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(stroke/2, stroke/2, w - stroke, h - stroke);

                // Highlight (Atas & Kiri - Warna Putih Transparan)
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRect(stroke, stroke, w - stroke*2, 4);
                g2.fillRect(stroke, stroke, 4, h - stroke*2);

                // Shadow (Bawah & Kanan - Warna Hitam Transparan)
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(stroke, h - stroke - 4, w - stroke*2, 4);
                g2.fillRect(w - stroke - 4, stroke, 4, h - stroke*2);

                // Teks Putih
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();

                // Text Shadow
                g2.setColor(Color.BLACK);
                g2.drawString(getText(), x + 2, y + 2);

                // Main Text
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(pixelFont);
        btn.setPreferredSize(new Dimension(400,75));
        btn.setMaximumSize(new Dimension(400,75));
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