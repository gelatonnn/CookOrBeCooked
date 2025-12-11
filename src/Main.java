import controller.GameController;
import factory.ItemRegistryInit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameConfig;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import utils.Position;
import view.gui.*;

public class Main {
    private static JFrame window;
    private static JPanel mainContainer;
    private static CardLayout cardLayout;
    private static JPanel gameContainerPanel;
    private static GameEngine engine;

    // PROGRESS TRACKER (Level 1 terbuka default)
    private static int unlockedStage = 1;
    private static boolean isCurrentGameMultiplayer = false;

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
        window.setMinimumSize(new Dimension(800,600));
        window.setResizable(true);
        window.setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        window.add(mainContainer);
    }

    private static void showHomeScreen() {
        AssetManager.getInstance().playBGM("bgm_menu");
        HomePanel homePanel = new HomePanel(
                () -> showStageSelect(false), // Singleplayer
                () -> showStageSelect(true)   // Multiplayer
        );
        mainContainer.add(homePanel, "HOME_SCREEN");
        cardLayout.show(mainContainer, "HOME_SCREEN");
        window.setVisible(true);
    }

    // -- PANEL STAGE SELECT --
    private static void showStageSelect(boolean isMultiplayer) {
        isCurrentGameMultiplayer = isMultiplayer;
        StageSelectPanel stagePanel = new StageSelectPanel(
                unlockedStage,
                (config) -> initAndStartGame(config), // onStageSelected
                () -> cardLayout.show(mainContainer, "HOME_SCREEN") // onBack
        );
        mainContainer.add(stagePanel, "STAGE_SELECT");
        cardLayout.show(mainContainer, "STAGE_SELECT");
    }

    // Start Game dengan Config
    private static void initAndStartGame(GameConfig config) {
        AssetManager.getInstance().playBGM("bgm_game");

        if (engine != null) return;

        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);

        // Buat engine dengan config stage yang dipilih
        engine = new GameEngine(world, orders, config);

        engine.setOnGameEnd(() -> {
            boolean win = engine.isWin();
            int finalScore = orders.getScore();

            // LOGIC UNLOCK LEVEL
            if (win) {
                if (config.stageName.equals("Stage 1") && unlockedStage < 2) unlockedStage = 2;
                else if (config.stageName.equals("Stage 2") && unlockedStage < 3) unlockedStage = 3;
            }

            SwingUtilities.invokeLater(() -> showGameOverScreen(finalScore, win));
        });

        // Spawn Logic
        List<Position> spawns = world.getSpawnPoints();
        int x1 = 2, y1 = 3;
        int x2 = 11, y2 = 6;
        if (spawns.size() >= 1) { x1 = spawns.get(0).x; y1 = spawns.get(0).y; }
        if (spawns.size() >= 2) { x2 = spawns.get(1).x; y2 = spawns.get(1).y; }

        String name1 = isCurrentGameMultiplayer ? "P1 (Gordon)" : "Gordon";
        String name2 = isCurrentGameMultiplayer ? "P2 (Ramsay)" : "Ramsay";

        Chef c1 = new Chef("c1", name1, x1, y1);
        Chef c2 = new Chef("c2", name2, x2, y2);

        // SELALU tambahkan kedua chef ke engine
        engine.addChef(c1);
        engine.addChef(c2);

        GameController controller = new GameController(engine, isCurrentGameMultiplayer);
        gameContainerPanel = new JPanel(new BorderLayout());
        GamePanel gamePanel = new GamePanel(engine);

        HUDPanel hudPanel = new HUDPanel(engine, () -> {
            stopGame();
            AssetManager.getInstance().playBGM("bgm_menu");
            showStageSelect(isCurrentGameMultiplayer); // Back to Stage Select
        });

        engine.addObserver(gamePanel);
        engine.addObserver(hudPanel);

        gameContainerPanel.add(hudPanel, BorderLayout.NORTH);
        gameContainerPanel.add(gamePanel, BorderLayout.CENTER);

        setupKeyListener(gameContainerPanel, controller);

        mainContainer.add(gameContainerPanel, "GAME_SCREEN");
        cardLayout.show(mainContainer, "GAME_SCREEN");
        window.pack();
        window.setLocationRelativeTo(null);
        gameContainerPanel.requestFocusInWindow();

        new Thread(engine::start).start();
    }

    private static void setupKeyListener(JPanel panel, GameController controller) {
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { controller.keyPressed(e); }
            @Override
            public void keyReleased(KeyEvent e) { controller.keyReleased(e); }
        });
    }

    private static void showGameOverScreen(int finalScore, boolean isWin) {
        AssetManager.getInstance().stopBGM();
        stopGame();
        GameOverPanel gameOverPanel = new GameOverPanel(finalScore, isWin, () -> {
            AssetManager.getInstance().playBGM("bgm_menu");
            showStageSelect(isCurrentGameMultiplayer);
        });
        mainContainer.add(gameOverPanel, "GAME_OVER_SCREEN");
        cardLayout.show(mainContainer, "GAME_OVER_SCREEN");
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