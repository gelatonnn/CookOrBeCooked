package model.engine;

import items.core.Item;
import items.core.ItemState;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import model.chef.Chef;
import model.orders.OrderManager;
import model.world.Tile;
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

    // ENTITIES
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private final List<FloorItem> floorItems = new CopyOnWriteArrayList<>();

    private Runnable onGameEnd;
    private boolean isRunning = false;
    private boolean finished = false;

    private static final double MOVEMENT_SPEED = 0.075; // Adjusted for tick rate
    private static final double THROW_MAX_DIST = 4;
    private static final double THROW_SPEED = 0.2;

    public GameEngine(WorldMap world, OrderManager orders, int stageSeconds) {
        this.world = world;
        this.orders = orders;
        this.clock = new GameClock(stageSeconds);
        this.chefs = new ArrayList<>();
        instance = this;
    }

    public static GameEngine getInstance() { return instance; }

    public void addChef(Chef chef) { chefs.add(chef); }
    public List<Chef> getChefs() { return new ArrayList<>(chefs); }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<FloorItem> getFloorItems() { return floorItems; }

    public void setOnGameEnd(Runnable onGameEnd) { this.onGameEnd = onGameEnd; }

    public void start() {
        isRunning = true;
        long lastTime = System.nanoTime();
        double amountOfTicks = 60;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long lastTimerCheck = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                updatePhysics();
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

    public void stop() { isRunning = false; finished = true; }

    public void tick() {
        clock.tick();
        orders.tick();
        if (clock.isOver() && isRunning) {
            stop();
            if (onGameEnd != null) onGameEnd.run();
        }
    }

    // --- PHYSICS LOOP ---

    // Input state buffer
    private final boolean[] chefMoving = new boolean[2];

    public void updateChefMovement(Chef chef, Direction dir, boolean isMoving) {
        if (dir != null) chef.setDirection(dir); // Always update facing instantly

        // Basic logic assuming 2 chefs max for array indexing
        int idx = chefs.indexOf(chef);
        if (idx >= 0 && idx < 2) chefMoving[idx] = isMoving;
    }

    private void updatePhysics() {
        // 1. Move Chefs
        for (int i = 0; i < chefs.size(); i++) {
            Chef c = chefs.get(i);
            if (chefMoving[i] && !c.isBusy()) {
                moveChef(c, c.getDirection());
            }
        }

        // 2. Update Projectiles
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.update()) {
                projectiles.remove(p);
            }
        }
    }

    public void moveChef(Chef chef, Direction dir) {
        double speed = MOVEMENT_SPEED;
        if (EffectManager.getInstance().isFlash()) speed *= 2.0;

        double dx = dir.getXComponent() * speed;
        double dy = dir.getYComponent() * speed;

        double nextX = chef.getExactX() + dx;
        double nextY = chef.getExactY() + dy;

        // Sliding collision
        if (isValidPosition(nextX, chef.getExactY())) chef.setExactPos(nextX, chef.getExactY());
        if (isValidPosition(chef.getExactX(), nextY)) chef.setExactPos(chef.getExactX(), nextY);
    }

    private boolean isValidPosition(double topLx, double topLy) {
        double padding = 0.25;
        double right = topLx + 1.0 - padding;
        double bottom = topLy + 1.0 - padding;
        return isWalkablePixel(topLx + padding, topLy + padding) &&
                isWalkablePixel(right, topLy + padding) &&
                isWalkablePixel(topLx + padding, bottom) &&
                isWalkablePixel(right, bottom);
    }

    private boolean isWalkablePixel(double x, double y) {
        Position p = new Position((int)Math.floor(x), (int)Math.floor(y));
        if (!world.inBounds(p)) return false;
        Tile t = world.getTile(p);
        return t.isWalkable();
    }

    // --- THROW MECHANIC (IMPROVED) ---

    public void throwItem(Chef chef) {
        if (chef.isBusy() || chef.getHeldItem() == null) return;
        Item item = chef.getHeldItem();

        if (item.getState() == ItemState.COOKED || item.getState() == ItemState.BURNED) {
            System.out.println("❌ Cannot throw cooked food!");
            return;
        }

        view.gui.AssetManager.getInstance().playSound("throw");
        System.out.println("🚀 Throw initiated by " + chef.getName());

        double startX = chef.getExactX() + 0.5;
        double startY = chef.getExactY() + 0.5;

        Direction dir = chef.getDirection();
        double vecX = dir.getXComponent();
        double vecY = dir.getYComponent();

        // Raycast for Wall Collision
        double dist = 0;
        double step = 0.1;
        double finalDist = 0;
        boolean hitWall = false;

        while (dist <= THROW_MAX_DIST) {
            double checkX = startX + vecX * dist;
            double checkY = startY + vecY * dist;

            if (!isWalkablePixel(checkX, checkY)) {
                hitWall = true;
                finalDist = Math.max(0, dist - 0.5); // Stop slightly before wall
                break;
            }
            finalDist = dist;
            dist += step;
        }

        double targetX = startX + vecX * finalDist;
        double targetY = startY + vecY * finalDist;

        // Offset to center items visually
        projectiles.add(new Projectile(chef, item, startX - 0.5, startY - 0.5, targetX - 0.5, targetY - 0.5));

        chef.setHeldItem(null);
        chef.changeState(new model.chef.states.IdleState());
    }

    // --- PROJECTILE & FLOOR ITEM CLASSES ---

    public class FloorItem {
        public Item item;
        public double x, y; // Top-Left
        public FloorItem(Item item, double x, double y) {
            this.item = item;
            this.x = x;
            this.y = y;
        }
        public boolean intersects(double ox, double oy, double size) {
            return x < ox + size && x + 0.6 > ox && y < oy + size && y + 0.6 > oy;
        }
    }

    public class Projectile {
        Chef thrower;
        Item item;
        double x, y, startX, startY;
        double targetX, targetY;
        double totalDist;
        double traveled;
        double arcHeight = 1.0;

        public Projectile(Chef thrower, Item item, double sx, double sy, double tx, double ty) {
            this.thrower = thrower;
            this.item = item;
            this.startX = sx; this.startY = sy;
            this.x = sx; this.y = sy;
            this.targetX = tx; this.targetY = ty;
            this.traveled = 0;
            this.totalDist = Math.sqrt(Math.pow(tx - sx, 2) + Math.pow(ty - sy, 2));
        }

        public Item getItem() { return item; }
        public double getRenderX() { return x; }
        public double getRenderY() {
            // Parabolic arc logic
            if (totalDist == 0) return y;
            double progress = traveled / totalDist;
            double heightOffset = Math.sin(progress * Math.PI) * arcHeight;
            return y - heightOffset;
        }

        public boolean update() {
            double move = THROW_SPEED;
            if (traveled + move > totalDist) move = totalDist - traveled;

            traveled += move;
            double t = traveled / totalDist;
            if (totalDist == 0) t = 1;

            // Linear Interpolation
            double nextX = startX + (targetX - startX) * t;
            double nextY = startY + (targetY - startY) * t;

            // 1. Check Collision with Chefs (Catch)
            double centerX = nextX + 0.5;
            double centerY = nextY + 0.5;
            for (Chef c : chefs) {
                if (c != thrower && !c.hasItem()) {
                    double cX = c.getExactX() + 0.5;
                    double cY = c.getExactY() + 0.5;
                    if (Math.hypot(cX - centerX, cY - centerY) < 0.6) {
                        c.setHeldItem(item);
                        view.gui.AssetManager.getInstance().playSound("pickup");
                        System.out.println("🙌 " + c.getName() + " CAUGHT " + item.getName());
                        return true;
                    }
                }
            }

            // 2. Check Collision with Stations (Interact/Place)
            Position gridPos = new Position((int)Math.round(centerX), (int)Math.round(centerY));
            Station st = world.getStationAt(gridPos);
            if (st != null) {
                // Check if we hit the bounding box of the station
                if (Math.abs(gridPos.x - nextX) < 0.6 && Math.abs(gridPos.y - nextY) < 0.6) {
                    if (st.place(item)) {
                        System.out.println("🎯 Thrown item landed on " + st.getName());
                        return true;
                    } else {
                        // Scatter if full
                        System.out.println("↩️ Bounced off station");
                        scatter(nextX, nextY);
                        return true;
                    }
                }
            }

            x = nextX;
            y = nextY;

            // 3. Reached Target
            if (Math.abs(traveled - totalDist) < 0.01) {
                placeOrScatter(x, y);
                return true;
            }
            return false;
        }

        private void placeOrScatter(double lx, double ly) {
            // Check bounding box overlap with existing floor items
            for (FloorItem fi : floorItems) {
                if (fi.intersects(lx, ly, 0.4)) {
                    System.out.println("💥 Item collision! Scattering...");
                    scatter(lx, ly);
                    return;
                }
            }
            // Valid placement
            floorItems.add(new FloorItem(item, lx, ly));
            System.out.println("👇 Item landed at " + String.format("%.2f, %.2f", lx, ly));
        }

        private void scatter(double ox, double oy) {
            // Simple scatter logic: try random nearby offsets
            double[] offsets = {0.5, -0.5, 0.3, -0.3};
            for (double off : offsets) {
                double nx = ox + off;
                double ny = oy + off;
                if (isValidPosition(nx, ny)) {
                    floorItems.add(new FloorItem(item, nx, ny));
                    return;
                }
            }
            // If strictly stuck, just place at feet (clipping enabled to prevent disappearing)
            floorItems.add(new FloorItem(item, ox, oy));
        }
    }

    // --- INTERACTION LOGIC ---

    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) return;

        // 1. Station Pick
        Station st = world.getStationAt(p);
        if (st != null) {
            chef.tryPickFrom(st);
            return;
        }

        // 2. Pixel-based Floor Pick
        // Check circle radius around chef's facing point
        double px = chef.getExactX() + 0.5 + chef.getDirection().getXComponent() * 0.8;
        double py = chef.getExactY() + 0.5 + chef.getDirection().getYComponent() * 0.8;

        FloorItem closest = null;
        double minDst = 1.0;

        for (FloorItem fi : floorItems) {
            double dist = Math.hypot((fi.x + 0.5) - px, (fi.y + 0.5) - py);
            if (dist < minDst) {
                minDst = dist;
                closest = fi;
            }
        }

        if (closest != null && chef.getHeldItem() == null) {
            chef.setHeldItem(closest.item);
            floorItems.remove(closest);
            chef.changeState(new model.chef.states.CarryingState());
            view.gui.AssetManager.getInstance().playSound("pickup");
        }
    }

    public void placeAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st != null) {
            if (st instanceof stations.ServingStation) {
                processServing(chef, st);
                return;
            }
            chef.tryPlaceTo(st);
            return;
        }

        // Place on floor (Pixel based)
        if (chef.getHeldItem() != null) {
            double tx = chef.getExactX() + chef.getDirection().getXComponent();
            double ty = chef.getExactY() + chef.getDirection().getYComponent();

            if (isWalkablePixel(tx, ty)) {
                // Check overlap
                boolean free = true;
                for (FloorItem fi : floorItems) {
                    if (Math.hypot(fi.x - tx, fi.y - ty) < 0.5) free = false;
                }

                if (free) {
                    floorItems.add(new FloorItem(chef.getHeldItem(), tx, ty));
                    chef.setHeldItem(null);
                    chef.changeState(new model.chef.states.IdleState());
                    view.gui.AssetManager.getInstance().playSound("place");
                } else {
                    System.out.println("❌ Floor occupied!");
                }
            }
        }
    }

    // Helpers
    public void interactAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st != null) chef.tryInteract(st);
    }

    private void processServing(Chef chef, Station station) {
        if (chef.getHeldItem() instanceof items.dish.DishBase dish) {
            boolean success = orders.submitDish(dish.getRecipe().getType());
            chef.setHeldItem(null);
            chef.changeState(new model.chef.states.IdleState());
            if (success) view.gui.AssetManager.getInstance().playSound("serve");
            else view.gui.AssetManager.getInstance().playSound("trash");
            if (station instanceof stations.ServingStation ss) {
                if (ss.peek() == null) ss.receiveDirtyPlate();
            }
        }
    }

    public void dashChef(Chef chef) {
        // Dash implementation...
        if (chef.isBusy() || !chef.canDash()) return;
        view.gui.AssetManager.getInstance().playSound("dash");
        Direction dir = chef.getDirection();
        for (int i = 0; i < 15; i++) moveChef(chef, dir);
        chef.registerDash();
    }

    public void addObserver(Observer o) { observers.add(o); }
    private void notifyObservers() { for (Observer o : observers) o.update(); }
    public WorldMap getWorld() { return world; }
    public GameClock getClock() { return clock; }
    public OrderManager getOrders() { return orders; }
}