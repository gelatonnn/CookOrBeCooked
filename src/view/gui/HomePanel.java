package view.gui;

import java.awt.*;
import javax.swing.*;

public class HomePanel extends JPanel {
    private final Runnable onPlayClicked;

    public HomePanel(Runnable onPlayClicked) {
        this.onPlayClicked = onPlayClicked;

        //Tampilan Dasar
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
        this.setBackground(new Color(30, 30, 30)); 
        this.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 0. Judul
        JLabel titleLabel = new JLabel("NimonsCooked");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(Color.ORANGE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 1. Tombol Play 
        JButton playButton = createStyledButton("PLAY GAME");
        playButton.addActionListener(e -> startGame());

        // 2. Tombol How To Play 
        JButton helpButton = createStyledButton("HOW TO PLAY");
        helpButton.addActionListener(e -> showHowToPlay());

        // 3. Tombol Exit 
        JButton exitButton = createStyledButton("EXIT");
        exitButton.addActionListener(e -> System.exit(0));

        this.add(Box.createVerticalGlue()); 
        this.add(titleLabel);
        this.add(Box.createRigidArea(new Dimension(0, 50))); 
        this.add(playButton);
        this.add(Box.createRigidArea(new Dimension(0, 20))); 
        this.add(helpButton);
        this.add(Box.createRigidArea(new Dimension(0, 20))); 
        this.add(exitButton);
        this.add(Box.createVerticalGlue()); 
        
    }

    private void startGame() {
        System.out.println("Starting Game...");
        if (onPlayClicked != null) {
            onPlayClicked.run();
        }
    }

    private void showHowToPlay() {
        String instructionText = """
            === CARA BERMAIN NIMONSCOOKED ===
            
            [KONTROL]
            W, A, S, D  : Bergerak
            E           : Interaksi (Ambil/Taruh/Proses)
            CTRL + WASD : Dash (Lari Cepat)
            C           : Ganti Chef
            O           : Place Item Down
            P           : Pick Up Item
            T           : Throw Item
            
            [TUJUAN]
            1. Ambil bahan dari kotak (Crate).
            2. Potong bahan di talenan (Cutting Board).
            3. Masak di kompor (Stove) sampai matang.
            4. Letakkan di piring (Plate).
            5. Antar ke loket penyajian (Serving Window).
            
            Hati-hati! Jangan sampai masakan gosong!
            """;

        JTextArea textArea = new JTextArea(instructionText);
        textArea.setEditable(false);
        textArea.setOpaque(false); 
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));

        JOptionPane.showMessageDialog(this, 
            new JScrollPane(textArea) { 
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(400, 300); 
                }
            },
            "How To Play", 
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Helper method untuk membuat tombol yang terlihat lebih bagus
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 24));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 60));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false); 
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //Hover Effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237)); 
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180)); 
            }
        });
        return button;
    }
}