package controller;

import controller.commands.*;
import model.engine.GameEngine;
import model.world.WorldMap;
import model.chef.Chef;
import utils.Direction;
import view.*;

import java.util.Scanner;

public class GameController {
    private final GameEngine engine;
    private final WorldMap world;
    private final Chef[] chefs;
    private int activeChef = 0;

    private final ConsoleRenderer console;
    private final HUDRenderer hud;
    private final OrderRenderer orderView;

    private final Scanner input = new Scanner(System.in);

    public GameController(
            GameEngine engine,
            WorldMap world,
            Chef[] chefs,
            ConsoleRenderer console,
            HUDRenderer hud,
            OrderRenderer orderView
    ) {
        this.engine = engine;
        this.world = world;
        this.chefs = chefs;
        this.console = console;
        this.hud = hud;
        this.orderView = orderView;
    }

    public void gameLoop() {
        console.render();
        hud.render();
        orderView.render();

        while (!engine.isFinished()) {
            System.out.print("Command: ");
            String cmd = input.nextLine().trim().toLowerCase();

            Command action = parseCommand(cmd);
            if (action == null) continue;
            action.execute();

            engine.tick();

            console.render();
            hud.setActiveChefIndex(activeChef);
            hud.render();
            orderView.render();
        }

        System.out.println("\n=== GAME OVER ===");
        System.out.println("Final Score: " + engine.getOrders().getScore());
        System.out.println("Time Remaining: " + engine.getClock().getTimeRemaining() + "s");
    }

    private Command parseCommand(String s) {
        Chef chef = chefs[activeChef];

        return switch (s) {
            case "w" -> new MoveCommand(engine, chef, Direction.UP);
            case "s" -> new MoveCommand(engine, chef, Direction.DOWN);
            case "a" -> new MoveCommand(engine, chef, Direction.LEFT);
            case "d" -> new MoveCommand(engine, chef, Direction.RIGHT);
            case "e" -> new InteractCommand(engine, chef, world);
            case "p" -> new PickCommand(engine, chef, world);
            case "o" -> new PlaceCommand(engine, chef, world);
            case "t" -> new ThrowCommand(engine, chef, world);
            case "c" -> {
                activeChef = (activeChef+1)%chefs.length;
                System.out.println("Switched to Chef " + (activeChef+1));
                yield null;
            }
            case "q" -> new QuitCommand(engine);

            default -> {
                System.out.println("Unknown command.");
                yield null;
            }
        };
    }
}