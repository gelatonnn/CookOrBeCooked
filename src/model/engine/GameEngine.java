package model.engine;

import java.util.ArrayList;
import java.util.List;
import model.chef.Chef;
import model.orders.OrderManager;
import model.world.WorldMap;
import stations.Station;
import utils.*;
import view.Observer;

public class GameEngine {
    private final WorldMap world;
    private final OrderManager orders;
    private final GameClock clock;
    private final List<Chef> chefs;

    private boolean finished = false;
    private int failedStreak = 0;
    private final int maxFailedStreak = 5;
    private final List<Observer> observers = new ArrayList<>();
    private boolean isRunning = false;
    

    public GameEngine(WorldMap world, OrderManager orders, int stageSeconds) {
        this.world = world;
        this.orders = orders;
        this.clock = new GameClock(stageSeconds);
        this.chefs = new ArrayList<>();
    }
    public void addObserver(Observer o) {
        observers.add(o);
    }

    private void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
    public void start() {
        isRunning = true;
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0; // Kecepatan game (Tick per detik) - Bisa dinaikkan
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        // Timer untuk GameClock (Detik Game)
        long lastTimerCheck = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            // Logic & Render Loop (60x per detik)
            while (delta >= 1) {
                // tick() untuk update logic ringan (jika ada)
                // notifyObservers() agar GUI menggambar posisi terbaru (animasi)
                notifyObservers(); 
                delta--;
            }
            
            // --- FIX TIMER: Hanya kurangi waktu setiap 1 Detik (1000ms) ---
            if (System.currentTimeMillis() - lastTimerCheck >= 1000) {
                lastTimerCheck += 1000;
                clock.tick();      // Kurangi waktu game 1 detik
                orders.tick();     // Update expired orders
                
                // Cek kondisi kalah/menang di sini agar tidak spam
                if (clock.isOver()) {
                    System.out.println("TIME'S UP!");
                    // isRunning = false; // Uncomment jika ingin stop loop
                }
            }
            
            // Sleep biar CPU adem
            try { Thread.sleep(2); } catch (InterruptedException e) {}
        }
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