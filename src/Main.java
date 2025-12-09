import controller.GameController;
import factory.ItemRegistryInit;
import model.chef.Chef;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import view.gui.GamePanel;
import view.gui.HUDPanel;
import view.gui.HomePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    private static JFrame window;
    private static JPanel mainContainer;
    private static CardLayout cardLayout;
    private static JPanel gameContainerPanel;
    private static GameEngine engine;

    public static void main(String[] args) {
        ItemRegistryInit.registerAll();
        SwingUtilities.invokeLater(() -> {
            setupMainWindow();
            showHomeScreen();
        });
    }

    private static void setupMainWindow() {
        window = new JFrame("CookOrBeCooked");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setSize(800, 600);
        window.setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        window.add(mainContainer);
    }

    private static void showHomeScreen() {
        HomePanel homePanel = new HomePanel(() -> {
            initAndStartGame();
            cardLayout.show(mainContainer, "GAME_SCREEN");
            gameContainerPanel.requestFocusInWindow();
        });
        mainContainer.add(homePanel, "HOME_SCREEN");
        cardLayout.show(mainContainer, "HOME_SCREEN");
        window.setVisible(true);
    }

    private static void initAndStartGame() {
        if (engine != null) return;

        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);
        engine = new GameEngine(world, orders, 180);

        Chef c1 = new Chef("c1", "Chef 1", 2, 3);
        Chef c2 = new Chef("c2", "Chef 2", 11, 6);
        engine.addChef(c1);
        engine.addChef(c2);

        GameController controller = new GameController(engine);
        gameContainerPanel = new JPanel(new BorderLayout());
        GamePanel gamePanel = new GamePanel(engine);
        HUDPanel hudPanel = new HUDPanel(engine, () -> {
            stopGame();
            cardLayout.show(mainContainer, "HOME_SCREEN");
        });

        engine.addObserver(gamePanel);
        engine.addObserver(hudPanel);

        gameContainerPanel.add(hudPanel, BorderLayout.NORTH);
        gameContainerPanel.add(gamePanel, BorderLayout.CENTER);

        setupKeyListener(gameContainerPanel, controller);

        mainContainer.add(gameContainerPanel, "GAME_SCREEN");
        window.pack();
        window.setLocationRelativeTo(null);

        new Thread(engine::start).start();
    }

    private static void setupKeyListener(JPanel panel, GameController controller) {
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // FIX: Pass the full KeyEvent to handle Ctrl modifiers
                controller.handleInput(e);
            }
        });
    }

    private static void stopGame() {
        if (engine != null) {
            engine.stop();
            engine = null;
        }
        if (gameContainerPanel != null) {
            mainContainer.remove(gameContainerPanel);
            gameContainerPanel = null;
        }
    }
}