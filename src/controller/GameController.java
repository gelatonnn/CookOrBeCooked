package controller;

import controller.commands.*;
import model.engine.GameEngine;
import model.world.WorldMap;
import model.chef.Chef;
import utils.Direction;
import utils.TimerUtils;
import view.*;

import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;

public class GameController {
    private final GameEngine engine;
    private final WorldMap world;
    private final Chef[] chefs;
    private int activeChef = 0;

    private final ConsoleRenderer console;
    private final HUDRenderer hud;
    private final OrderRenderer orderView;

    private final Scanner input = new Scanner(System.in);
    private ScheduledFuture<?> gameTickTask;

    private long lastDashTime = 0;
    private final long DASH_COOLDOWN_MS = 5000;
    private final int DASH_DISTANCE = 3;

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
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║     GAME STARTING...              ║");
        System.out.println("╚════════════════════════════════════╝\n");

        // Start game tick thread (1 tick per second)
        gameTickTask = TimerUtils.repeat(() -> {
            engine.tick();
        }, 1000);

        console.render();
        hud.render();
        orderView.render();

        while (!engine.isFinished()) {
            System.out.print("\nCommand: ");
            String cmd = input.nextLine().trim().toLowerCase();

            Command action = parseCommand(cmd);
            if (action != null) {
                action.execute();
            }

            console.render();
            hud.setActiveChefIndex(activeChef);
            hud.render();
            orderView.render();
        }

        // Stop game tick
        if (gameTickTask != null) {
            gameTickTask.cancel(true);
        }

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║          GAME OVER                ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("Final Score: " + engine.getOrders().getScore());
        System.out.println("Time Remaining: " + engine.getClock().getTimeRemaining() + "s");
    }

    private Command parseCommand(String s) {
        Chef chef = chefs[activeChef];

        // Dash commands (SHIFT + direction)
        if (s.startsWith("shift+") || s.startsWith("dash+")) {
            String dir = s.substring(s.indexOf('+')+1);
            return handleDash(chef, dir);
        }

        return switch (s) {
            // 4-direction movement
            case "w" -> new MoveCommand(engine, chef, Direction.UP);
            case "s" -> new MoveCommand(engine, chef, Direction.DOWN);
            case "a" -> new MoveCommand(engine, chef, Direction.LEFT);
            case "d" -> new MoveCommand(engine, chef, Direction.RIGHT);

            // 8-direction (diagonal)
            case "wa", "aw" -> new MoveCommand(engine, chef, Direction.UP_LEFT);
            case "wd", "dw" -> new MoveCommand(engine, chef, Direction.UP_RIGHT);
            case "sa", "as" -> new MoveCommand(engine, chef, Direction.DOWN_LEFT);
            case "sd", "ds" -> new MoveCommand(engine, chef, Direction.DOWN_RIGHT);

            case "e", "v" -> new InteractCommand(engine, chef, world);
            case "p" -> new PickCommand(engine, chef, world);
            case "o" -> new PlaceCommand(engine, chef, world);
            case "t" -> new ThrowCommand(engine, chef, world, DASH_DISTANCE);
            case "c", "b" -> {
                activeChef = (activeChef + 1) % chefs.length;
                System.out.println("→ Switched to Chef " + (activeChef + 1) + " [" + chefs[activeChef].getName() + "]");
                yield null;
            }
            case "q" -> new QuitCommand(engine);

            default -> {
                System.out.println("❌ Unknown command: " + s);
                yield null;
            }
        };
    }

    private Command handleDash(Chef chef, String dirStr) {
        long now = System.currentTimeMillis();
        if (now - lastDashTime < DASH_COOLDOWN_MS) {
            long remaining = (DASH_COOLDOWN_MS - (now - lastDashTime)) / 1000;
            System.out.println("⚠ Dash on cooldown! Wait " + remaining + "s");
            return null;
        }

        Direction dir = switch(dirStr) {
            case "w" -> Direction.UP;
            case "s" -> Direction.DOWN;
            case "a" -> Direction.LEFT;
            case "d" -> Direction.RIGHT;
            default -> null;
        };

        if (dir == null) {
            System.out.println("❌ Invalid dash direction!");
            return null;
        }

        lastDashTime = now;
        System.out.println("💨 DASH activated! Moving " + DASH_DISTANCE + " tiles");
        return new DashCommand(engine, chef, dir, DASH_DISTANCE);
    }
}