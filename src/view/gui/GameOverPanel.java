package view.gui;

import java.awt.*;
import javax.swing.*;

public class GameOverPanel extends JPanel {
    public GameOverPanel(int finalScore, Runnable onBackToMenu) {
        // Setup Tampilan (Dark Theme)
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(new Color(30, 30, 30));
        this.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 1. Label "GAME OVER"
        JLabel titleLabel = new JLabel("TIME'S UP!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 54));
        titleLabel.setForeground(new Color(255, 80, 80)); // Merah
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Label Skor
        JLabel scoreLabel = new JLabel("Final Score: " + finalScore);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        scoreLabel.setForeground(new Color(255, 215, 0)); // Emas
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Tombol Kembali
        JButton btnMenu = new JButton("Back to Main Menu");
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMenu.setBackground(new Color(100, 149, 237));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setFocusPainted(false);
        btnMenu.addActionListener(e -> onBackToMenu.run());

        // Susun Komponen
        this.add(Box.createVerticalGlue());
        this.add(titleLabel);
        this.add(Box.createRigidArea(new Dimension(0, 20)));
        this.add(scoreLabel);
        this.add(Box.createRigidArea(new Dimension(0, 50)));
        this.add(btnMenu);
        this.add(Box.createVerticalGlue());
    }
}