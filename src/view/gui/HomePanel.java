package view.gui;

import java.awt.*;
import javax.swing.*;

public class HomePanel extends JPanel {
    // Runnable adalah "tugas" yang akan dijalankan saat tombol Play ditekan
    private final Runnable onPlayClicked;

    public HomePanel(Runnable onPlayClicked) {
        this.onPlayClicked = onPlayClicked;

        // Setup Tampilan Dasar
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Layout vertikal
        this.setBackground(new Color(30, 30, 30)); // Latar belakang gelap
        // Beri jarak (padding) di sekeliling panel
        this.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 1. Judul Game
        JLabel titleLabel = new JLabel("Nimonscooked");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 64));
        titleLabel.setForeground(new Color(255, 215, 0)); // Warna Emas
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Tombol Play
        JButton playButton = createStyledButton("PLAY GAME");
        playButton.addActionListener(e -> startGame());

        // 3. Tombol Exit
        JButton exitButton = createStyledButton("EXIT");
        exitButton.addActionListener(e -> System.exit(0)); // Keluar aplikasi

        // Menambahkan komponen ke panel dengan jarak (rigid area)
        this.add(Box.createVerticalGlue()); // Dorong ke tengah vertikal
        this.add(titleLabel);
        this.add(Box.createRigidArea(new Dimension(0, 50))); // Jarak 50px
        this.add(playButton);
        this.add(Box.createRigidArea(new Dimension(0, 20))); // Jarak 20px
        this.add(exitButton);
        this.add(Box.createVerticalGlue()); // Dorong ke tengah vertikal
    }

    private void startGame() {
        System.out.println("Starting Game...");
        // Jalankan tugas yang diberikan oleh Main.java (yaitu: ganti layar & mulai engine)
        if (onPlayClicked != null) {
            onPlayClicked.run();
        }
    }

    // Helper method untuk membuat tombol yang terlihat lebih bagus
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 24));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 60)); // Ukuran tetap
        button.setBackground(new Color(70, 130, 180)); // Biru Steel
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false); // Hilangkan kotak fokus saat diklik
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Ubah kursor jadi tangan

        // Efek Hover Sederhana
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237)); // Lebih terang saat hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180)); // Kembali normal
            }
        });
        return button;
    }
}