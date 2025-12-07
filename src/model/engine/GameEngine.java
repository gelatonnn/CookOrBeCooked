package model.engine;

import java.util.ArrayList;
import java.util.List;
import model.chef.Chef;
import model.orders.OrderManager;
import model.world.WorldMap;
import stations.Station;
import utils.Direction;
import utils.Position;
import view.Observer;

public class GameEngine {
    private static GameEngine instance;
    private final WorldMap world;
    private final OrderManager orders;
    private final GameClock clock;
    private final List<Chef> chefs;
    private final List<Observer> observers = new ArrayList<>();

    private boolean isRunning = false;
    private boolean finished = false;
    private int failedStreak = 0;
    private final int maxFailedStreak = 5;

    // Constructor public agar bisa di-new di Main (sesuai kode terakhir kita)
    public GameEngine(WorldMap world, OrderManager orders, int stageSeconds) {
        this.world = world;
        this.orders = orders;
        this.clock = new GameClock(stageSeconds);
        this.chefs = new ArrayList<>();
        instance = this; // Simpan instance (opsional)
    }

    public static GameEngine getInstance() {
        return instance;
    }

    public void addChef(Chef chef) {
        chefs.add(chef);
    }

    public List<Chef> getChefs() {
        return new ArrayList<>(chefs);
    }

    // --- GAME LOOP ---
    public void start() {
        isRunning = true;
        
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0; // 60 FPS Render
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        
        long lastTimerCheck = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while (delta >= 1) {
                notifyObservers(); // Update GUI
                delta--;
            }
            
            // Logic Timer: 1 Detik Realtime
            if (System.currentTimeMillis() - lastTimerCheck >= 1000) {
                lastTimerCheck += 1000;
                tick();
            }
            
            try { Thread.sleep(2); } catch (InterruptedException e) {}
        }
    }

    public void stop() {
        isRunning = false;
        finished = true;
    }

    public void tick() {
        clock.tick();
        orders.tick();

        if (clock.isOver()) {
            System.out.println("TIME'S UP!");
            // stop(); // Uncomment jika ingin game benar-benar berhenti
        }
    }

    // --- CHEF ACTIONS ---

    public void moveChef(Chef chef, Direction dir) {
        if (chef.isBusy()) return;

        Position currentPos = chef.getPos();
        Position newPos = currentPos.move(dir);

        if (!world.inBounds(newPos)) return;
        if (!world.isWalkable(newPos)) return;

        // Cek tabrakan dengan chef lain
        for (Chef other : chefs) {
            if (other != chef && other.getPos().equals(newPos)) return;
        }

        chef.setPos(newPos.x, newPos.y);
        chef.setDirection(dir);
    }

    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st == null) return;
        chef.tryPickFrom(st);
    }

    public void placeAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        Station st = world.getStationAt(p);
        if (st == null) return;

        // --- UPDATE PENTING: Serving saat tombol Place (O) ditekan ---
        if (st instanceof stations.ServingStation) {
            processServing(chef);
            return; // Jangan lakukan place biasa (storing)
        }
        // -------------------------------------------------------------

        chef.tryPlaceTo(st);
    }

    public void interactAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        
        Station st = world.getStationAt(p);
        if (st == null) return;

        // Fallback: Jika user menekan Interact (E) di Serving Station, tetap layani
        if (st instanceof stations.ServingStation) {
            processServing(chef);
            return;
        }

        chef.tryInteract(st);
    }

    public void throwItem(Chef chef) {
        if (chef.isBusy()) return;
        chef.throwItem(world.getWallMask());
    }

   // Di dalam src/model/engine/GameEngine.java

    private void processServing(Chef chef) {
        if (chef.getHeldItem() == null) return;

        // Cek apakah item tersebut adalah Dish (Masakan Jadi)
        if (chef.getHeldItem() instanceof items.dish.DishBase dish) {
            
            // Ambil tipe masakan dari resep
            model.recipes.DishType type = dish.getRecipe().getType();
            
            // Kirim ke OrderManager
            boolean success = orders.submitDish(type);

            if (success) {
                System.out.println("✅ ORDER COMPLETED! +Points");
                chef.setHeldItem(null); // Piring diambil pelanggan
                
                // --- FIX: RESET STATE AGAR TIDAK STUCK ---
                chef.changeState(new model.chef.states.IdleState());
                // -----------------------------------------
                
            } else {
                System.out.println("❌ WRONG ORDER! Penalty");
                // Jika salah, item tetap di tangan (tidak di-set null), 
                // jadi Chef tetap di CarryingState (ini benar).
            }
        } else {
            System.out.println("⚠️ Item ini bukan masakan jadi (Dish)!");
        }
    }

    // --- OBSERVER PATTERN ---
    public void addObserver(Observer o) {
        observers.add(o);
    }

    private void notifyObservers() {
        for (Observer o : observers) o.update();
    }

    // --- GETTERS ---
    public WorldMap getWorld() { return world; }
    public GameClock getClock() { return clock; }
    public OrderManager getOrders() { return orders; }
    public boolean isFinished() { return finished; }
}