package model.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import items.core.Item;
import items.core.ItemState;
import model.chef.Chef;
import model.orders.OrderManager;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.WalkableTile;
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

    // Config disimpan untuk pengecekan kondisi menang
    private final GameConfig config;

    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private Runnable onGameEnd;
    private boolean isRunning = false;
    private boolean finished = false;
    private boolean isWin = false; // Status akhir

    private static final double MOVEMENT_SPEED = 3.0;
    private static final double DASH_SPEED = 10.0;
    private static final double DASH_TOTAL_DIST = 120.0;
    private static final double THROW_DISTANCE = 3.5;
    private static final double THROW_SPEED = 0.25;

    // UPDATE CONSTRUCTOR: Menerima GameConfig
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
        double amountOfTicks = config.fps; // Pakai FPS dari config
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

        // --- WIN/LOSS LOGIC ---

        // 1. Cek Batas Kesalahan (Berlaku Semua Mode)
        if (orders.getFailedCount() >= config.maxFailedStreak) {
            System.out.println("❌ GAME OVER: Too many failed orders!");
            finishGame(false);
            return;
        }

        if (config.isSurvival) {
            // Mode SURVIVAL: Menang jika waktu habis DAN skor cukup
            if (clock.isOver()) {
                if (orders.getScore() >= config.minScore) {
                    System.out.println("🎉 SURVIVAL SUCCESS!");
                    finishGame(true);
                } else {
                    System.out.println("❌ SURVIVAL FAILED: Low Score");
                    finishGame(false);
                }
            }
        } else {
            // Mode NORMAL: Menang jika target order tercapai
            if (orders.getCompletedCount() >= config.targetOrders) {
                System.out.println("🎉 STAGE CLEARED!");
                finishGame(true);
                return;
            }
            // Kalah jika waktu habis tapi target belum tercapai
            if (clock.isOver()) {
                System.out.println("❌ TIME'S UP! Target not reached.");
                finishGame(false);
            }
        }
    }

    private void finishGame(boolean win) {
        this.isWin = win;
        stop();
        if (onGameEnd != null) onGameEnd.run();
    }

    // --- PHYSICS & MOVEMENT (Tidak ada perubahan logika, hanya copy paste) ---
    private void updatePhysics() {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.update()) projectiles.remove(p);
        }
        for (Chef chef : chefs) updateChefPosition(chef);
    }

    private void updateChefPosition(Chef chef) {
        if (chef.isBusy()) return;
        if (chef.isDashing()) {
            double moveDist = DASH_SPEED;
            moveChef(chef, chef.getDashDirection(), moveDist);
            chef.updateDash(moveDist);
            return;
        }
        Direction inputDir = chef.getMoveInput();
        if (inputDir != null) {
            double speed = MOVEMENT_SPEED;
            if (EffectManager.getInstance().isFlash()) speed *= 1.5;
            chef.setDirection(inputDir);
            moveChef(chef, inputDir, speed);
        }
    }

    public void moveChef(Chef chef, Direction dir, double speed) {
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
        double nextX = chef.getExactX() + dx / 60.0;
        if (isValidPosition(nextX, chef.getExactY())) chef.setExactPos(nextX, chef.getExactY());
        else if (chef.isDashing()) chef.stopDash();

        double nextY = chef.getExactY() + dy / 60.0;
        if (isValidPosition(chef.getExactX(), nextY)) chef.setExactPos(chef.getExactX(), nextY);
        else if (chef.isDashing()) chef.stopDash();
    }

    // Overload untuk panggilan dari MoveCommand (Default Speed)
    public void moveChef(Chef chef, Direction dir) {
        moveChef(chef, dir, MOVEMENT_SPEED);
    }

    private boolean isValidPosition(double topLx, double topLy) {
        double padding = 0.25;
        double left = topLx + padding;
        double right = topLx + 1.0 - padding;
        double top = topLy + padding;
        double bottom = topLy + 1.0 - padding;
        return isWalkablePixel(left, top) && isWalkablePixel(right, top) &&
                isWalkablePixel(left, bottom) && isWalkablePixel(right, bottom);
    }

    private boolean isWalkablePixel(double x, double y) {
        int tileX = (int) Math.floor(x);
        int tileY = (int) Math.floor(y);
        Position p = new Position(tileX, tileY);
        if (!world.inBounds(p)) return false;
        return world.getTile(p).isWalkable();
    }

    public void dashChef(Chef chef) {
        if (chef.isBusy() || !chef.canDash()) return;
        Direction dashDir = chef.getMoveInput();
        if (dashDir == null) dashDir = chef.getDirection();
        AssetManager.getInstance().playSound("dash");
        chef.startDash(dashDir, DASH_TOTAL_DIST);
    }

    public void throwItem(Chef chef) {
        if (chef.isBusy() || chef.getHeldItem() == null) return;
        Item item = chef.getHeldItem();
        if (item.getState() == ItemState.COOKED || item.getState() == ItemState.BURNED) {
            System.out.println("❌ Cannot throw cooked food!");
            return;
        }
        AssetManager.getInstance().playSound("throw");
        double startX = chef.getExactX() + 0.5;
        double startY = chef.getExactY() + 0.5;
        double dirX = 0, dirY = 0;
        switch (chef.getDirection()) {
            case UP -> dirY = -1;
            case DOWN -> dirY = 1;
            case LEFT -> dirX = -1;
            case RIGHT -> dirX = 1;
            default -> dirY = 1;
        }
        double finalDist = 0;
        for (double d = 0; d <= THROW_DISTANCE; d += 0.2) {
            double tx = startX + dirX * d;
            double ty = startY + dirY * d;
            if (!isWalkablePixel(tx, ty)) {
                finalDist = Math.max(0, d - 0.7);
                break;
            }
            finalDist = d;
        }
        double targetX = startX + dirX * finalDist;
        double targetY = startY + dirY * finalDist;
        projectiles.add(new Projectile(chef, item, startX - 0.5, startY - 0.5, targetX - 0.5, targetY - 0.5));
        chef.setHeldItem(null);
        chef.changeState(new model.chef.states.IdleState());
    }

    public class Projectile {
        Chef thrower;
        Item item;
        double x, y, targetX, targetY, totalDist, traveled;
        public Projectile(Chef thrower, Item item, double sx, double sy, double tx, double ty) {
            this.thrower = thrower; this.item = item;
            this.x = sx; this.y = sy; this.targetX = tx; this.targetY = ty;
            this.totalDist = Math.sqrt(Math.pow(tx - sx, 2) + Math.pow(ty - sy, 2));
        }
        public boolean update() {
            if (totalDist == 0) return true;
            double move = THROW_SPEED;
            if (traveled + move > totalDist) move = totalDist - traveled;
            x += (targetX - x) / (totalDist - traveled) * move;
            y += (targetY - y) / (totalDist - traveled) * move;
            traveled += move;

            double centerX = x + 0.5;
            double centerY = y + 0.5;
            for (Chef c : chefs) {
                if (c != thrower && !c.hasItem()) {
                    double dist = Math.sqrt(Math.pow(c.getExactX() + 0.5 - centerX, 2) + Math.pow(c.getExactY() + 0.5 - centerY, 2));
                    if (dist < 0.7) {
                        c.setHeldItem(item);
                        AssetManager.getInstance().playSound("pickup");
                        return true;
                    }
                }
            }
            if (Math.abs(traveled - totalDist) < 0.05) {
                dropItem();
                return true;
            }
            return false;
        }
        private void dropItem() {
            Position gridPos = new Position((int) Math.floor(x + 0.5), (int) Math.floor(y + 0.5));
            Tile t = world.getTile(gridPos);
            if (t instanceof WalkableTile wt && wt.getItem() == null) wt.setItem(item);
        }
        public double getX() { return x; }
        public double getY() { return y; }
        public Item getItem() { return item; }
    }

    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st != null) { chef.tryPickFrom(st); return; }
        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt && wt.getItem() != null && chef.getHeldItem() == null) {
            chef.setHeldItem(wt.pick());
            chef.changeState(new model.chef.states.CarryingState());
            AssetManager.getInstance().playSound("pickup");
        }
    }

    public void placeAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st == null) return;
        if (st instanceof stations.ServingStation) { processServing(chef, st); return; }
        chef.tryPlaceTo(st);
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
            if (success) AssetManager.getInstance().playSound("serve");
            else AssetManager.getInstance().playSound("trash");
            if (station instanceof stations.ServingStation ss && ss.peek() == null) ss.receiveDirtyPlate();
        }
    }

    public void addObserver(Observer o) { observers.add(o); }
    private void notifyObservers() { for (Observer o : observers) o.update(); }
    public WorldMap getWorld() { return world; }
    public GameClock getClock() { return clock; }
    public OrderManager getOrders() { return orders; }
    public boolean isFinished() { return finished; }
}