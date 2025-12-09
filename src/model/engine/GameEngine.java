package model.engine;

import java.util.ArrayList;
import java.util.List;
import model.chef.Chef;
import model.orders.OrderManager;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.WalkableTile;
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
    private Runnable onGameEnd;

    private boolean isRunning = false;
    private boolean finished = false;

    public GameEngine(WorldMap world, OrderManager orders, int stageSeconds) {
        this.world = world;
        this.orders = orders;
        this.clock = new GameClock(stageSeconds);
        this.chefs = new ArrayList<>();
        instance = this;
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

    public void setOnGameEnd(Runnable onGameEnd) {
        this.onGameEnd = onGameEnd;
    }

    // --- GAME LOOP ---
    public void start() {
        isRunning = true;

        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        long lastTimerCheck = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                notifyObservers();
                delta--;
            }

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

        // LOGIKA GAME OVER OTOMATIS
        if (clock.isOver() && isRunning) {
            System.out.println("TIME'S UP! Game Ending...");
            stop(); // Matikan loop engine
            
            // Panggil callback jika ada
            if (onGameEnd != null) {
                onGameEnd.run();
            }
        }
    }

    // --- CHEF ACTIONS ---

    public void moveChef(Chef chef, Direction dir) {
        if (chef.isBusy()) return;

        // FIX (Retained): Update Direction FIRST so chef faces walls if blocked
        chef.setDirection(dir);

        Position currentPos = chef.getPos();
        Position newPos = currentPos.move(dir);

        if (!world.inBounds(newPos)) return;
        if (!world.isWalkable(newPos)) return;

        for (Chef other : chefs) {
            if (other != chef && other.getPos().equals(newPos)) return;
        }

        chef.setPos(newPos.x, newPos.y);
    }

    // NEW: Dash Mechanic
    public void dashChef(Chef chef) {
        if (chef.isBusy()) return;

        if (!chef.canDash()) {
            System.out.println("Dash is on cooldown!");
            return;
        }

        System.out.println("Chef triggered Dash!");
        Direction dir = chef.getDirection();

        // Dash distance: 3 tiles
        for (int i = 0; i < 3; i++) {
            // Re-use moveChef to ensure collision logic is respected
            moveChef(chef, dir);
        }

        chef.registerDash();
    }

    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        // Priority 1: Pick from Station
        Station st = world.getStationAt(p);
        if (st != null) {
            chef.tryPickFrom(st);
            return;
        }

        // FIX (Retained): Pick from Floor (WalkableTile)
        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt) {
            if (wt.getItem() != null && chef.getHeldItem() == null) {
                chef.setHeldItem(wt.pick());
                chef.changeState(new model.chef.states.CarryingState());
                System.out.println("Picked up " + chef.getHeldItem().getName() + " from floor.");
            }
        }
    }

    public void placeAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        Station st = world.getStationAt(p);
        if (st == null) return;

        if (st instanceof stations.ServingStation) {
            processServing(chef, st);
            return;
        }

        chef.tryPlaceTo(st);
    }

    public void interactAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        Station st = world.getStationAt(p);
        if (st == null) return;

        if (st instanceof stations.ServingStation) {
            processServing(chef, st);
            return;
        }

        chef.tryInteract(st);
    }

    public void throwItem(Chef chef) {
        if (chef.isBusy() || chef.getHeldItem() == null) return;

        // FIX (Retained): Throw item onto the map
        Position p = chef.getPos();
        Direction d = chef.getDirection();

        for (int i = 0; i < 3; i++) {
            Position next = p.move(d);
            if (!world.inBounds(next) || !world.isWalkable(next)) {
                break;
            }
            p = next;
        }

        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt) {
            if (wt.getItem() == null) {
                wt.setItem(chef.getHeldItem());
            } else {
                System.out.println("Floor occupied, item lost!");
            }
        }

        chef.throwItem(world.getWallMask());
    }

    // Ubah parameter method ini
    private void processServing(Chef chef, Station station) { 
        if (chef.getHeldItem() == null) return;

        // ... logic cek dish ...
        if (chef.getHeldItem() instanceof items.dish.DishBase dish) {
             // ... logic submit order ...
            boolean success = orders.submitDish(dish.getRecipe().getType());

            if (success) {
                System.out.println("✅ ORDER COMPLETED!");
                chef.setHeldItem(null); // Piring di tangan hilang
                
                // --- LOGIC BARU: MUNCULKAN PIRING KOTOR ---
                // Jika Serving Station kosong, munculkan Dirty Plate di situ
                if (station.peek() == null) {
                    // Kita perlu 'force' place karena base station mungkin menolak dirty plate
                    // Jadi kita akses storedItem secara manual atau buat method forcePlace
                    // Tapi karena ServingStation extends BaseStation, kita bisa pakai place() 
                    // ASALKAN ServingStation.canPlace() mengizinkan DirtyPlate (atau kita override).
                    
                    // Cara paling aman: Langsung set item (tapi butuh akses protected/setter)
                    // Atau: Kita buat ServingStation menerima DirtyPlate
                    
                    // Mari kita asumsikan ServingStation punya method untuk terima hasil return
                    if (station instanceof stations.ServingStation ss) {
                        ss.receiveDirtyPlate();
                    }
                }
                // ------------------------------------------

                chef.changeState(new model.chef.states.IdleState());
            } else {
                System.out.println("❌ WRONG ORDER!");
            }
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