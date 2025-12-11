package view.gui;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
import model.engine.GameConfig;

public class StageSelectPanel extends JPanel {
    public StageSelectPanel(int unlockedLevel, Consumer<GameConfig> onStageSelected, Runnable onBack) {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("SELECT STAGE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        add(title, gbc);

        // --- STAGE 1: EASY (4 Menit, Target 3 Order) ---
        gbc.gridy++;
        add(createButton("Stage 1: Easy", "Target: 3 Orders | 4 Mins", true,
                new GameConfig("Stage 1", 240, 5, 3, 0, false), onStageSelected), gbc);

        // --- STAGE 2: MEDIUM (4.5 Menit, Target 4 Order) ---
        gbc.gridy++;
        add(createButton("Stage 2: Medium", "Target: 4 Orders | 4.5 Mins", unlockedLevel >= 2,
                new GameConfig("Stage 2", 270, 5, 4, 0, false), onStageSelected), gbc);

        // --- STAGE 3: SURVIVAL (5 Menit, Bertahan Hidup) ---
        gbc.gridy++;
        add(createButton("Stage 3: Survival", "Survive 5 Mins | Min Score: 500", unlockedLevel >= 3,
                new GameConfig("Stage 3", 300, 3, 0, 500, true), onStageSelected), gbc);

        // Back Button
        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        JButton btnBack = new JButton("Back to Menu");
        btnBack.setPreferredSize(new Dimension(200, 40));
        btnBack.setBackground(new Color(200, 60, 60));
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> onBack.run());
        add(btnBack, gbc);
    }

    private JButton createButton(String title, String desc, boolean unlocked, GameConfig config, Consumer<GameConfig> action) {
        JButton btn = new JButton("<html><center>" + title + "<br/><small>" + desc + "</small></center></html>");
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setFocusPainted(false);
        if (unlocked) {
            btn.setBackground(new Color(100, 149, 237));
            btn.addActionListener(e -> action.accept(config));
        } else {
            btn.setBackground(Color.GRAY);
            btn.setEnabled(false);
            btn.setText("<html><center>" + title + " (LOCKED)</center></html>");
        }
        return btn;
    }
}