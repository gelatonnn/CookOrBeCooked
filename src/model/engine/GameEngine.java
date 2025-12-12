package model.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;
import items.utensils.DirtyPlate;
import model.chef.Chef;
import model.orders.OrderManager;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.WalkableTile;
import model.world.tiles.WalkableTile.DroppedItem;
import stations.Station;
import utils.Direction;
import utils.Position;
import view.Observer;
import view.gui.AssetManager;

public class GameEngine {
    private static GameEngine instance;
    private final WorldMap world;
    private final OrderManager orders;
    private final GameClock clock;
    private final List<Chef> chefs;
    private final List<Observer> observers = new ArrayList<>();

    private final GameConfig config;
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    
    private final List<DelayedTask> delayedTasks = new CopyOnWriteArrayList<>();
    private record DelayedTask(long executeTime, Runnable action) {}

    private Runnable onGameEnd;
    private boolean isRunning = false;
    private boolean finished = false;
    private boolean isWin = false;

    private static final double MOVEMENT_SPEED = 3.0;
    private static final double DASH_SPEED = 10.0;
    private static final double DASH_TOTAL_DIST = 120.0;

    private static final double CHEF_SIZE = 0.6;
    private static final double ITEM_SIZE = 0.4;
    private static final double THROW_DISTANCE = 4.0;
    private static final double THROW_SPEED_PPS = 8.0;
    private static final double THROW_ARC_HEIGHT = 0.6;
    private static final double BOUNCE_DURATION = 0.3;
    private static final double BOUNCE_DISTANCE = 0.5;

    public GameEngine(WorldMap world, OrderManager orders, GameConfig config) {
        this.world = world;
        this.orders = orders;
        this.config = config;
        this.clock = new GameClock(config.stageTimeSeconds);
        this.chefs = new ArrayList<>();
        instance = this;
    }

    public static GameEngine getInstance() { return instance; }

    public void addChef(Chef chef) { chefs.add(chef); }
    public List<Chef> getChefs() { return new ArrayList<>(chefs); }
    public List<Projectile> getProjectiles() { return projectiles; }
    public void setOnGameEnd(Runnable onGameEnd) { this.onGameEnd = onGameEnd; }
    public boolean isWin() { return isWin; }
    public GameConfig getConfig() { return config; }

    public void start() {
        isRunning = true;
        long lastTime = System.nanoTime();
        double amountOfTicks = config.fps;
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

    public void stop() {
        isRunning = false;
        finished = true;
    }

    public void tick() {
        clock.tick();
        orders.tick();
        if (orders.getFailedCount() >= config.maxFailedStreak) { finishGame(false); return; }
        if (config.isSurvival) {
            if (clock.isOver()) finishGame(orders.getScore() >= config.minScore);
        } else {
            if (orders.getCompletedCount() >= config.targetOrders) { finishGame(true); return; }
            if (clock.isOver()) finishGame(false);
        }
    }

    private void finishGame(boolean win) {
        this.isWin = win;
        stop();
        if (onGameEnd != null) onGameEnd.run();
    }

    private void updatePhysics() {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.update()) projectiles.remove(p);
        }
        
        for (Chef chef : chefs) updateChefPosition(chef);
        
        long now = System.currentTimeMillis();
        Iterator<DelayedTask> taskIt = delayedTasks.iterator();
        while (taskIt.hasNext()) {
            DelayedTask task = taskIt.next();
            if (now >= task.executeTime) {
                task.action.run();
                delayedTasks.remove(task);
            }
        }
    }

    // --- SHARED HELPER ---
    private boolean isSpaceFree(double cx, double cy, double radius) {
        Position p = new Position((int)cx, (int)cy);
        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt) {
            for (DroppedItem di : wt.getItems()) {
                if (dist(di.x, di.y, cx, cy) < (di.item.getSize() + radius) * 0.8) return false;
            }
        }
        return true;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }

    // --- CHEF MOVEMENT ---
    public void moveChef(Chef chef, Direction dir) {
        moveChefWithCollision(chef, dir, MOVEMENT_SPEED);
    }

    private void updateChefPosition(Chef chef) {
        if (chef.isBusy()) return;

        double speed = 0;
        Direction moveDir = null;

        if (chef.isDashing()) {
            speed = DASH_SPEED;
            moveDir = chef.getDashDirection();
            chef.updateDash(speed);
        } else {
            moveDir = chef.getMoveInput();
            if (moveDir != null) {
                speed = MOVEMENT_SPEED;
                if (EffectManager.getInstance().isFlash()) speed *= 1.5;
                chef.setDirection(moveDir);
            }
        }

        if (moveDir != null && speed > 0) {
            moveChefWithCollision(chef, moveDir, speed);
        }
    }

    private void moveChefWithCollision(Chef chef, Direction dir, double speed) {
        double dx = 0, dy = 0;
        double diagonalFactor = 0.7071;

        switch (dir) {
            case UP -> dy = -speed;
            case DOWN -> dy = speed;
            case LEFT -> dx = -speed;
            case RIGHT -> dx = speed;
            case UP_LEFT -> { dx = -speed * diagonalFactor; dy = -speed * diagonalFactor; }
            case UP_RIGHT -> { dx = speed * diagonalFactor; dy = -speed * diagonalFactor; }
            case DOWN_LEFT -> { dx = -speed * diagonalFactor; dy = speed * diagonalFactor; }
            case DOWN_RIGHT -> { dx = speed * diagonalFactor; dy = speed * diagonalFactor; }
        }

        double speedPerTick = 1.0 / 60.0;

        double nextExactX = chef.getExactX() + dx * speedPerTick;
        if (!checkAABBCollision(nextExactX, chef.getExactY(), CHEF_SIZE)) {
            chef.setExactPos(nextExactX, chef.getExactY());
        } else if (chef.isDashing()) {
            chef.stopDash();
        }

        double nextExactY = chef.getExactY() + dy * speedPerTick;
        if (!checkAABBCollision(chef.getExactX(), nextExactY, CHEF_SIZE)) {
            chef.setExactPos(chef.getExactX(), nextExactY);
        } else if (chef.isDashing()) {
            chef.stopDash();
        }
    }

    private boolean checkAABBCollision(double tlx, double tly, double size) {
        
        double minX = tlx + (1.0 - size) / 2.0;
        double maxX = minX + size;
        
        double maxY = tly + 1.0; 
        double minY = maxY - size;

        int startTileX = (int) Math.floor(minX);
        int endTileX = (int) Math.floor(maxX - 0.001);
        int startTileY = (int) Math.floor(minY);
        int endTileY = (int) Math.floor(maxY - 0.001);

        for (int y = startTileY; y <= endTileY; y++) {
            for (int x = startTileX; x <= endTileX; x++) {
                Position p = new Position(x, y);
                if (!world.inBounds(p)) return true;
                if (!world.getTile(p).isWalkable()) return true;
            }
        }
        return false;
    }

    public void dashChef(Chef chef) {
        if (chef.isBusy() || !chef.canDash()) return;
        Direction dashDir = chef.getMoveInput();
        if (dashDir == null) dashDir = chef.getDirection();
        AssetManager.getInstance().playSound("dash");
        chef.startDash(dashDir, DASH_TOTAL_DIST);
    }

    // --- THROWING LOGIC ---
    public void throwItem(Chef chef) {
        if (chef.isBusy() || chef.getHeldItem() == null) return;
        Item item = chef.getHeldItem();

        if (!(item instanceof Preparable) || item.getState() == ItemState.COOKED || item.getState() == ItemState.BURNED) {
            System.out.println("❌ Can only throw uncooked ingredients!");
            return;
        }

        AssetManager.getInstance().playSound("throw");

        double startX = chef.getExactX() + 0.5;
        double startY = chef.getExactY() + 0.5;

        double[] vec = chef.getFacingVector();
        double dirX = vec[0];
        double dirY = vec[1];

        double targetX = startX + dirX * THROW_DISTANCE;
        double targetY = startY + dirY * THROW_DISTANCE;

        projectiles.add(new Projectile(chef, item, startX - 0.5, startY - 0.5, targetX - 0.5, targetY - 0.5));
        chef.setHeldItem(null);
        chef.changeState(new model.chef.states.IdleState());
    }

    // --- PROJECTILE CLASS ---
    public class Projectile {
        Chef thrower;
        Item item;
        enum State { FLYING, BOUNCING }
        State state = State.FLYING;

        double startX, startY, targetX, targetY;
        double currentX, currentY;

        long startTime;
        double duration;

        double bounceStartX, bounceStartY, bounceTargetX, bounceTargetY;
        long bounceStartTime;

        public Projectile(Chef thrower, Item item, double sx, double sy, double tx, double ty) {
            this.thrower = thrower;
            this.item = item;
            this.startX = sx; this.startY = sy;
            this.targetX = tx; this.targetY = ty;
            this.currentX = sx; this.currentY = sy;

            double dist = Math.sqrt(Math.pow(tx - sx, 2) + Math.pow(ty - sy, 2));
            this.duration = dist / THROW_SPEED_PPS;
            if (this.duration < 0.1) this.duration = 0.1;

            this.startTime = System.nanoTime();
        }

        public boolean update() {
            if (state == State.FLYING) return updateFlying();
            else if (state == State.BOUNCING) return updateBouncing();
            return true;
        }

        private boolean updateFlying() {
            long now = System.nanoTime();
            double elapsed = (now - startTime) / 1_000_000_000.0;
            double t = Math.min(1.0, elapsed / duration);

            double nextX = startX + (targetX - startX) * t;
            double nextY = startY + (targetY - startY) * t;

            double centerX = nextX + 0.5;
            double centerY = nextY + 0.5;

            // 1. Catching
            for (Chef c : chefs) {
                if (c != thrower && !c.hasItem() && !c.isDashing()) {
                    if (dist(c.getExactX() + 0.5, c.getExactY() + 0.5, centerX, centerY) < 0.6) {
                        c.setHeldItem(item);
                        AssetManager.getInstance().playSound("pickup");
                        return true;
                    }
                }
            }

            // 2. Mid-air Collision (HANYA Wall & Station, ITEM DILEWATI)
            if (checkCollisionMidAir(nextX, nextY)) {
                startBounce(currentX, currentY, nextX, nextY);
                return false;
            }

            currentX = nextX;
            currentY = nextY;

            // 3. Arrival Logic
            if (t >= 1.0) {
                if (checkLandingCollision(currentX, currentY)) {
                    startBounce(currentX, currentY, startX, startY); 
                    return false;
                }

                landItemAt(currentX, currentY);
                return true;
            }
            return false;
        }

        private boolean checkCollisionMidAir(double tlx, double tly) {
            return checkAABBCollision(tlx, tly, ITEM_SIZE);
        }

        private boolean checkLandingCollision(double tlx, double tly) {
            if (checkAABBCollision(tlx, tly, ITEM_SIZE)) return true;

            double cx = tlx + 0.5;
            double cy = tly + 0.5;
            return !isSpaceFree(cx, cy, item.getSize()); 
        }

        private void startBounce(double currX, double currY, double hitX, double hitY) {
            this.state = State.BOUNCING;
            this.bounceStartTime = System.nanoTime();
            this.bounceStartX = currX;
            this.bounceStartY = currY;

            double dx = currX - hitX;
            double dy = currY - hitY;
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len == 0) { dx = -1; len = 1; }

            double ndx = dx / len;
            double ndy = dy / len;

            this.bounceTargetX = currX + ndx * BOUNCE_DISTANCE;
            this.bounceTargetY = currY + ndy * BOUNCE_DISTANCE;

            if (checkAABBCollision(bounceTargetX, bounceTargetY, ITEM_SIZE)) {
                this.bounceTargetX = currX;
                this.bounceTargetY = currY;
            }

            AssetManager.getInstance().playSound("bump");
        }

        private boolean updateBouncing() {
            long now = System.nanoTime();
            double elapsed = (now - bounceStartTime) / 1_000_000_000.0;
            double t = Math.min(1.0, elapsed / BOUNCE_DURATION);

            currentX = bounceStartX + (bounceTargetX - bounceStartX) * t;
            currentY = bounceStartY + (bounceTargetY - bounceStartY) * t;

            if (t >= 1.0) {
                landItemAt(currentX, currentY);
                return true;
            }
            return false;
        }

        private void landItemAt(double tlx, double tly) {
            double cx = tlx + 0.5;
            double cy = tly + 0.5;

            if (checkAABBCollision(tlx, tly, ITEM_SIZE)) {
                Position p = resolveCollision(cx, cy);
                cx = p.x + 0.5;
                cy = p.y + 0.5;
            }

            Position gridPos = new Position((int)cx, (int)cy);

            Station st = world.getStationAt(gridPos);
            if (st != null) {
                if (st.peek() == null) {
                    st.place(item);
                    AssetManager.getInstance().playSound("place");
                } else {
                    scatterItem(cx, cy);
                }
                return;
            }

            Tile t = world.getTile(gridPos);
            if (t instanceof WalkableTile wt) {
                if (isSpaceFree(cx, cy, item.getSize())) {
                    wt.addItem(item, cx, cy);
                } else {
                    scatterItem(cx, cy);
                }
            }
        }

        private Position resolveCollision(double cx, double cy) {
            int tx = (int)cx;
            int ty = (int)cy;
            for(int y=ty-1; y<=ty+1; y++) {
                for(int x=tx-1; x<=tx+1; x++) {
                    Position p = new Position(x, y);
                    if(world.inBounds(p) && world.getTile(p).isWalkable()) {
                        return p;
                    }
                }
            }
            return new Position(tx, ty);
        }

        private void scatterItem(double cx, double cy) {
            double[] angles = {0, 45, 90, 135, 180, 225, 270, 315};
            double radius = 0.7;
            for (double ang : angles) {
                double rad = Math.toRadians(ang);
                double nx = cx + Math.cos(rad) * radius;
                double ny = cy + Math.sin(rad) * radius;
                if (!checkAABBCollision(nx - 0.5, ny - 0.5, ITEM_SIZE) && isSpaceFree(nx, ny, item.getSize())) {
                    Position np = new Position((int)nx, (int)ny);
                    Tile nt = world.getTile(np);
                    if (nt instanceof WalkableTile wt) {
                        wt.addItem(item, nx, ny);
                        return;
                    }
                }
            }
        }

        public double getX() { return currentX; }
        public double getY() {
            if (state == State.BOUNCING) {
                long now = System.nanoTime();
                double t = Math.min(1.0, (now - bounceStartTime) / 1_000_000_000.0 / BOUNCE_DURATION);
                double arc = Math.sin(t * Math.PI) * (THROW_ARC_HEIGHT * 0.4);
                return currentY - arc;
            }
            long now = System.nanoTime();
            double elapsed = (now - startTime) / 1_000_000_000.0;
            double t = Math.min(1.0, elapsed / duration);
            double arc = Math.sin(t * Math.PI) * THROW_ARC_HEIGHT;
            return currentY - arc;
        }
        public Item getItem() { return item; }
    }

    // --- INTERACTION LOGIC ---
    public void placeAt(Chef chef, Position ignored) {
        if (chef.isBusy()) return;

        double[] vec = chef.getFacingVector();
        double targetX = chef.getExactX() + 0.5 + vec[0] * 0.8;
        double targetY = chef.getExactY() + 0.5 + vec[1] * 0.8;
        Position p = new Position((int)targetX, (int)targetY);

        Station st = world.getStationAt(p);
        if (st != null) {
            if (st instanceof stations.ServingStation) { processServing(chef, st); return; }
            chef.tryPlaceTo(st);
            return;
        }

        System.out.println("❌ Cannot drop item on floor. Use Throw!");
    }

    public void pickAt(Chef chef, Position ignored) {
        if (chef.isBusy()) return;

        double[] vec = chef.getFacingVector();
        double pickX = chef.getExactX() + 0.5 + vec[0] * 0.6;
        double pickY = chef.getExactY() + 0.5 + vec[1] * 0.6;
        Position p = new Position((int)pickX, (int)pickY);

        Station st = world.getStationAt(p);
        if (st != null) { chef.tryPickFrom(st); return; }

        // 1. Cek Depan
        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt) {
            DroppedItem target = wt.pickNearest(pickX, pickY, 0.7);

            // 2. Jika depan kosong, Cek Kaki (radius 0.5)
            if (target == null) {
                Position pCenter = new Position(chef.getX(), chef.getY());
                Tile tCenter = world.getTile(pCenter);
                if (tCenter instanceof WalkableTile wtCenter) {
                    target = wtCenter.pickNearest(chef.getExactX() + 0.5, chef.getExactY() + 0.5, 0.5);
                }
            }

            if (target != null && chef.getHeldItem() == null) {
                Position itemPos = new Position((int)target.x, (int)target.y);
                Tile itemTile = world.getTile(itemPos);
                if (itemTile instanceof WalkableTile wtOrigin) {
                    wtOrigin.removeItem(target);
                    chef.setHeldItem(target.item);
                    chef.changeState(new model.chef.states.CarryingState());
                    AssetManager.getInstance().playSound("pickup");
                }
            }
        }
    }

    public void interactAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st == null) {
            if (st instanceof stations.ServingStation) { processServing(chef, st); return; }
        }
        if (st != null) chef.tryInteract(st);
    }

    private void processServing(Chef chef, Station station) {
        if (chef.getHeldItem() == null) return;
        if (chef.getHeldItem() instanceof items.dish.DishBase dish) {
            boolean success = orders.submitDish(dish.getRecipe().getType());
            chef.setHeldItem(null);
            chef.changeState(new model.chef.states.IdleState());
            
            if (success) {
                AssetManager.getInstance().playSound("serve");
                
                long targetTime = System.currentTimeMillis() + 10000;
                delayedTasks.add(new DelayedTask(targetTime, () -> {
                    for (int y = 0; y < world.getHeight(); y++) {
                        for (int x = 0; x < world.getWidth(); x++) {
                            Station st = world.getStationAt(new Position(x, y));
                            if (st instanceof stations.PlateStorage ps) {
                                ps.place(new DirtyPlate());
                                System.out.println("⚠️ Dirty plate reappeared at storage!");
                                return; 
                            }
                        }
                    }
                }));
                
            } else {
                AssetManager.getInstance().playSound("trash");
            }
        }
    }

    public void addObserver(Observer o) { observers.add(o); }
    private void notifyObservers() { for (Observer o : observers) o.update(); }
    public WorldMap getWorld() { return world; }
    public GameClock getClock() { return clock; }
    public OrderManager getOrders() { return orders; }
    public boolean isFinished() { return finished; }
}