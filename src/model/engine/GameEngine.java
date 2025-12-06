package model.engine;

import model.world.WorldMap;
import model.chef.Chef;
import stations.Station;
import model.orders.OrderManager;
import utils.*;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private final WorldMap world;
    private final OrderManager orders;
    private final GameClock clock;
    private final List<Chef> chefs;

    private boolean finished = false;
    private int failedStreak = 0;
    private final int maxFailedStreak = 5;

    public GameEngine(WorldMap world, OrderManager orders, int stageSeconds) {
        this.world = world;
        this.orders = orders;
        this.clock = new GameClock(stageSeconds);
        this.chefs = new ArrayList<>();
    }

    public void addChef(Chef chef) {
        chefs.add(chef);
    }

    public List<Chef> getChefs() {
        return new ArrayList<>(chefs);
    }

    public void tick() {
        clock.tick();
        orders.tick();

        if (clock.isOver()) {
            finished = true;
            System.out.println("\nTIME'S UP! Game Over!");
        }

        if (failedStreak >= maxFailedStreak) {
            finished = true;
            System.out.println("\nToo many failed orders! Game Over!");
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void moveChef(Chef chef, Direction dir) {
        if (chef.isBusy()) {
            System.out.println("Chef is busy and cannot move!");
            return;
        }

        Position currentPos = chef.getPos();
        Position newPos = currentPos.move(dir);

        if (!world.inBounds(newPos)) {
            System.out.println("Cannot move out of bounds!");
            return;
        }

        if (!world.isWalkable(newPos)) {
            System.out.println("Path is blocked!");
            return;
        }

        for (Chef other : chefs) {
            if (other != chef && other.getPos().equals(newPos)) {
                System.out.println("Another chef is in the way!");
                return;
            }
        }

        chef.setPos(newPos.x, newPos.y);
        chef.setDirection(dir);
    }

    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) {
            System.out.println("Chef is busy!");
            return;
        }

        Station st = world.getStationAt(p);
        if (st == null) {
            System.out.println("No station here!");
            return;
        }

        chef.tryPickFrom(st);
    }

    public void placeAt(Chef chef, Position p) {
        if (chef.isBusy()) {
            System.out.println("Chef is busy!");
            return;
        }

        Station st = world.getStationAt(p);
        if (st == null) {
            System.out.println("No station here!");
            return;
        }

        chef.tryPlaceTo(st);
    }

    public void interactAt(Chef chef, Position p) {
        if (chef.isBusy()) {
            System.out.println("Chef is already busy!");
            return;
        }

        Station st = world.getStationAt(p);
        if (st == null) {
            System.out.println("No station to interact with!");
            return;
        }

        chef.tryInteract(st);
    }

    public void throwItem(Chef chef) {
        if (chef.isBusy()) {
            System.out.println("Chef is busy!");
            return;
        }

        chef.throwItem(world.getWallMask());
    }

    public void stop() {
        finished = true;
    }

    public void incrementFailedStreak() {
        failedStreak++;
    }

    public void resetFailedStreak() {
        failedStreak = 0;
    }

    public int getFailedStreak() {
        return failedStreak;
    }

    public WorldMap getWorld() {
        return world;
    }

    public GameClock getClock() {
        return clock;
    }

    public OrderManager getOrders() {
        return orders;
    }
}