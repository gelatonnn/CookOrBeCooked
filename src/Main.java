package main;

import controller.GameController;
import factory.ItemRegistryInit;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import model.chef.Chef;
import view.*;

public class Main {
    public static void main(String[] args) {
        // Initialize item factory
        ItemRegistryInit.registerAll();

        // 1. World
        WorldMap world = new WorldMap();

        // 2. Chefs (spawn at V positions)
        Chef chef1 = new Chef("chef1", "Gordon", 2, 3);
        Chef chef2 = new Chef("chef2", "Ramsay", 11, 6);
        Chef[] chefs = { chef1, chef2 };

        // 3. Orders
        OrderManager orders = new OrderManager(false);

        // 4. Engine
        GameEngine engine = new GameEngine(world, orders, 180);
        engine.addChef(chef1);
        engine.addChef(chef2);

        // 5. View
        ConsoleRenderer console = new ConsoleRenderer(world, chefs);
        HUDRenderer hud = new HUDRenderer(chefs);
        OrderRenderer orderView = new OrderRenderer(orders);

        // 6. Controller
        GameController controller = new GameController(engine, world, chefs,
                console, hud, orderView);

        System.out.println("=== NIMONSCOOKED ===");
        System.out.println("Controls:");
        System.out.println("  WASD - Move active chef");
        System.out.println("  E - Interact with station");
        System.out.println("  P - Pick item from station");
        System.out.println("  O - Place item to station");
        System.out.println("  T - Throw item");
        System.out.println("  C - Switch chef");
        System.out.println("  Q - Quit game");
        System.out.println("====================\n");

        controller.gameLoop();
    }
}