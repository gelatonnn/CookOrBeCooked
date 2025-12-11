package view.gui;

import java.awt.*;
import javax.swing.*;

public class GameOverPanel extends JPanel {
    public GameOverPanel(int finalScore, boolean isWin, Runnable onBackToMenu) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Hijau jika menang, Merah jika kalah
        this.setBackground(isWin ? new Color(20, 50, 20) : new Color(50, 20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 1. Title
        JLabel titleLabel = new JLabel(isWin ? "STAGE CLEARED!" : "GAME OVER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 54));
        titleLabel.setForeground(isWin ? new Color(100, 255, 100) : new Color(255, 80, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Score
        JLabel scoreLabel = new JLabel("Final Score: " + finalScore);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Button
        JButton btnMenu = new JButton("Back to Stage Select");
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMenu.setBackground(new Color(100, 149, 237));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setFocusPainted(false);
        btnMenu.addActionListener(e -> onBackToMenu.run());

        this.add(Box.createVerticalGlue());
        this.add(titleLabel);
        this.add(Box.createRigidArea(new Dimension(0, 20)));
        this.add(scoreLabel);
        this.add(Box.createRigidArea(new Dimension(0, 50)));
        this.add(btnMenu);
        this.add(Box.createVerticalGlue());
    }
}