package controller;

import factory.ItemRegistryInit;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import model.chef.Chef;
import view.*;

import java.util.Scanner;

public class MenuController {
    private final Scanner scanner = new Scanner(System.in);
    private boolean stageCompleted = false;

    public void showMainMenu() {
        while (true) {
            clearScreen();
            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║       NIMONSCOOKED GAME           ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println();
            System.out.println("1. Start Game");
            System.out.println("2. How to Play");
            System.out.println("3. Exit");
            System.out.println();
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showStageSelect();
                    break;
                case "2":
                    showHowToPlay();
                    break;
                case "3":
                    System.out.println("Thanks for playing!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option!");
                    pause();
            }
        }
    }

    private void showStageSelect() {
        clearScreen();
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║         STAGE SELECT              ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println();
        System.out.println("Stage 1: PASTA MAP " + (stageCompleted ? "[SUCCESS]" : ""));
        System.out.println();
        System.out.println("Map Preview:");
        System.out.println("  14x10 grid with multiple stations");
        System.out.println("  Target Score: 500 points");
        System.out.println("  Time Limit: 180 seconds");
        System.out.println();
        System.out.println("Press ENTER to start, or 'B' to go back");

        String choice = scanner.nextLine().trim().toLowerCase();
        if (!choice.equals("b")) {
            startGame();
        }
    }

    private void showHowToPlay() {
        clearScreen();
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║          HOW TO PLAY              ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println();
        System.out.println("CONTROLS:");
        System.out.println("  W/A/S/D    - Move chef (8-direction with diagonals)");
        System.out.println("  SHIFT+W/A/S/D - Dash (move 3 tiles, 5s cooldown)");
        System.out.println("  E          - Interact with station");
        System.out.println("  P          - Pick item from station");
        System.out.println("  O          - Place item to station");
        System.out.println("  T          - Throw item (3 tiles distance)");
        System.out.println("  C          - Switch between chefs");
        System.out.println("  Q          - Quit game");
        System.out.println();
        System.out.println("OBJECTIVE:");
        System.out.println("  - Prepare dishes according to orders");
        System.out.println("  - Serve dishes before time runs out");
        System.out.println("  - Avoid burning ingredients");
        System.out.println("  - Wash dirty plates before reuse");
        System.out.println();
        System.out.println("STATIONS:");
        System.out.println("  I - Ingredient Storage (get ingredients)");
        System.out.println("  C - Cutting Station (chop ingredients)");
        System.out.println("  R - Cooking Station (cook with utensils)");
        System.out.println("  A - Assembly Station (combine ingredients)");
        System.out.println("  P - Plate Storage (get clean plates)");
        System.out.println("  W - Washing Station (clean dirty plates)");
        System.out.println("  S - Serving Counter (serve dishes)");
        System.out.println("  T - Trash Station (discard items)");
        System.out.println();
        pause();
    }

    private void startGame() {
        WorldMap world = new WorldMap();

        Chef chef1 = new Chef("chef1", "Gordon", 2, 3);
        Chef chef2 = new Chef("chef2", "Ramsay", 11, 6);
        Chef[] chefs = { chef1, chef2 };

        OrderManager orders = new OrderManager(true);

        GameEngine engine = new GameEngine(world, orders, 180);
        engine.addChef(chef1);
        engine.addChef(chef2);

        ConsoleRenderer console = new ConsoleRenderer(world, chefs);
        HUDRenderer hud = new HUDRenderer(chefs);
        OrderRenderer orderView = new OrderRenderer(orders);

        GameController controller = new GameController(engine, world, chefs,
                console, hud, orderView);

        controller.gameLoop();

        showResultScreen(engine);
    }

    private void showResultScreen(GameEngine engine) {
        clearScreen();
        int score = engine.getOrders().getScore();
        int targetScore = 500;
        boolean passed = score >= targetScore;

        if (passed) {
            stageCompleted = true;
            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║       🎉 STAGE CLEARED! 🎉        ║");
            System.out.println("╚════════════════════════════════════╝");
        } else {
            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║         STAGE FAILED              ║");
            System.out.println("╚════════════════════════════════════╝");
        }

        System.out.println();
        System.out.println("RESULTS:");
        System.out.println("  Final Score: " + score + " / " + targetScore);
        System.out.println("  Status: " + (passed ? "PASS ✓" : "FAIL ✗"));
        System.out.println();
        System.out.println("Press ENTER to return to menu");
        scanner.nextLine();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
