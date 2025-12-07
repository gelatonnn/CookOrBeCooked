package model.engine;

import java.util.ArrayList;
import java.util.List;
import model.chef.Chef;
import model.chef.states.IdleState;
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

        if (clock.isOver()) {
            System.out.println("TIME'S UP!");
        }
    }

    // --- CHEF ACTIONS ---

    public void moveChef(Chef chef, Direction dir) {
        if (chef.isBusy()) return;

        // FIX 1: Update Direction FIRST.
        // Even if movement is blocked, the chef should face the requested direction.
        chef.setDirection(dir);

        Position currentPos = chef.getPos();
        Position newPos = currentPos.move(dir);

        if (!world.inBounds(newPos)) return;
        if (!world.isWalkable(newPos)) return;

        // Collision check with other chefs
        for (Chef other : chefs) {
            if (other != chef && other.getPos().equals(newPos)) return;
        }

        chef.setPos(newPos.x, newPos.y);
    }

    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        // Priority 1: Pick from Station
        Station st = world.getStationAt(p);
        if (st != null) {
            chef.tryPickFrom(st);
            return;
        }

        // FIX 2b: Pick from Floor (WalkableTile)
        // If no station, check if there is an item on the floor
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
            processServing(chef);
            return;
        }

        chef.tryPlaceTo(st);
    }

    public void interactAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        Station st = world.getStationAt(p);
        if (st == null) return;

        if (st instanceof stations.ServingStation) {
            processServing(chef);
            return;
        }

        chef.tryInteract(st);
    }

    public void throwItem(Chef chef) {
        if (chef.isBusy() || chef.getHeldItem() == null) return;

        // FIX 2a: Logic to throw item onto the map
        Position p = chef.getPos();
        Direction d = chef.getDirection();

        // Throw distance: up to 3 tiles
        for (int i = 0; i < 3; i++) {
            Position next = p.move(d);

            // Stop if out of bounds or hitting a wall/station (not walkable)
            if (!world.inBounds(next) || !world.isWalkable(next)) {
                break;
            }
            p = next;
        }

        // Place item on the final valid WalkableTile
        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt) {
            // Only place if floor is empty
            if (wt.getItem() == null) {
                wt.setItem(chef.getHeldItem());
            } else {
                System.out.println("Floor occupied, item lost!");
            }
        }

        // Chef logic: Clear hands and reset state
        chef.throwItem(world.getWallMask());
    }

    private void processServing(Chef chef) {
        if (chef.getHeldItem() == null) return;

        if (chef.getHeldItem() instanceof items.dish.DishBase dish) {
            model.recipes.DishType type = dish.getRecipe().getType();
            boolean success = orders.submitDish(type);

            if (success) {
                System.out.println("✅ ORDER COMPLETED! +Points");
                chef.setHeldItem(null);
                chef.changeState(new IdleState());
            } else {
                System.out.println("❌ WRONG ORDER! Penalty");
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