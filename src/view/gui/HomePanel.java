package view.gui;

import java.awt.*;
import javax.swing.*;

public class HomePanel extends JPanel {

    public HomePanel(Runnable onStartSingle, Runnable onStartMulti) {
        
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 5, 0); // Jarak antar elemen

        // --- TITLE ---
        JLabel title = new JLabel("NIMONSCOOKED");
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        add(title, gbc);

        JLabel subtitle = new JLabel("Map Type B: Pasta");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(Color.LIGHT_GRAY);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0); // Jarak agak jauh ke tombol
        add(subtitle, gbc);

        // --- BUTTONS ---
        gbc.insets = new Insets(10, 0, 10, 0); // Reset jarak

        // 1. Singleplayer
        gbc.gridy++;
        JButton btnSingle = createButton("SINGLE PLAYER (Switch Chef)");
        btnSingle.addActionListener(e -> {
            if (onStartSingle != null) onStartSingle.run();
        });
        add(btnSingle, gbc);

        // 2. Multiplayer
        gbc.gridy++;
        JButton btnMulti = createButton("MULTIPLAYER (Local Co-op)");
        btnMulti.setBackground(new Color(255, 140, 0)); // Warna Oranye agar beda
        btnMulti.addActionListener(e -> {
            if (onStartMulti != null) onStartMulti.run();
        });
        add(btnMulti, gbc);

        // 3. How to Play (Tetap Ada)
        gbc.gridy++;
        JButton btnHelp = createButton("HOW TO PLAY");
        btnHelp.setBackground(new Color(46, 204, 113)); // Warna Hijau
        btnHelp.addActionListener(e -> showModelessDialog("Cara Bermain", getHelpContent()));
        add(btnHelp, gbc);

        // 4. Exit (Tetap Ada)
        gbc.gridy++;
        JButton btnExit = createButton("EXIT GAME");
        btnExit.setBackground(new Color(200, 60, 60)); // Warna Merah
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Keluar dari permainan?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        add(btnExit, gbc);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(100, 149, 237));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(320, 45)); // Ukuran tombol konsisten
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- Helper Dialog (Sama seperti di HUDPanel) ---
    private void showModelessDialog(String title, String content) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, title);
        dialog.setModal(true); // Modal true agar fokus ke help dulu
        
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