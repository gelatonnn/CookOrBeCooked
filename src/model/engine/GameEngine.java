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

public class GameEngine {
    private static GameEngine instance;
    private final WorldMap world;
    private final OrderManager orders;
    private final GameClock clock;
    private final List<Chef> chefs;
    private final List<Observer> observers = new ArrayList<>();

    // PROJECTILE SYSTEM
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();

    private Runnable onGameEnd;
    private boolean isRunning = false;
    private boolean finished = false;

    // MOVEMENT SETTINGS
    // Kecepatan gerak per tick (pixel-based)
    private static final double MOVEMENT_SPEED = 0.75;

    // THROW SETTINGS
    private static final double THROW_DISTANCE = 3.5;
    private static final double THROW_SPEED = 0.25;

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

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void setOnGameEnd(Runnable onGameEnd) {
        this.onGameEnd = onGameEnd;
    }

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
                updatePhysics(); // Update fisika setiap tick (60 FPS)
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

        if (clock.isOver() && isRunning) {
            System.out.println("TIME'S UP! Game Ending...");
            stop();
            if (onGameEnd != null) {
                onGameEnd.run();
            }
        }
    }

    // --- PHYSICS & MOVEMENT ---

    private void updatePhysics() {
        // Update Projectiles (Flying Items)
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.update()) {
                projectiles.remove(p);
            }
        }
    }

    public void moveChef(Chef chef, Direction dir) {
        if (chef.isBusy()) return;

        chef.setDirection(dir);

        double dx = 0, dy = 0;
        switch (dir) {
            case UP -> dy = -MOVEMENT_SPEED;
            case DOWN -> dy = MOVEMENT_SPEED;
            case LEFT -> dx = -MOVEMENT_SPEED;
            case RIGHT -> dx = MOVEMENT_SPEED;
            case UP_LEFT -> { dx = -MOVEMENT_SPEED * 0.707; dy = -MOVEMENT_SPEED * 0.707; }
            case UP_RIGHT -> { dx = MOVEMENT_SPEED * 0.707; dy = -MOVEMENT_SPEED * 0.707; }
            case DOWN_LEFT -> { dx = -MOVEMENT_SPEED * 0.707; dy = MOVEMENT_SPEED * 0.707; }
            case DOWN_RIGHT -> { dx = MOVEMENT_SPEED * 0.707; dy = MOVEMENT_SPEED * 0.707; }
        }

        // Prediksi posisi baru
        double nextX = chef.getExactX() + dx;
        double nextY = chef.getExactY() + dy;

        // Gerakan Independent Sumbu X (Sliding)
        if (isValidPosition(nextX, chef.getExactY())) {
            chef.setExactPos(nextX, chef.getExactY());
        }

        // Gerakan Independent Sumbu Y (Sliding)
        // Kita gunakan getExactX() yang baru (jika tadi berhasil gerak X)
        if (isValidPosition(chef.getExactX(), nextY)) {
            chef.setExactPos(chef.getExactX(), nextY);
        }
    }

    // PERBAIKAN UTAMA: Collision Detection
    private boolean isValidPosition(double topLx, double topLy) {
        // Anggap ukuran Chef = 1.0 (seukuran Tile)
        // Kita buat "Collision Box" sedikit lebih kecil dari ukuran tile (padding)
        // Agar chef bisa lewat pintu sempit atau tidak nyangkut di pojok tembok
        double padding = 0.25;

        // Titik-titik collision box (Relative terhadap Top-Left)
        double left = topLx + padding;
        double right = topLx + 1.0 - padding;
        double top = topLy + padding;
        double bottom = topLy + 1.0 - padding;

        // Cek keempat sudut collision box
        return isWalkablePixel(left, top) &&
                isWalkablePixel(right, top) &&
                isWalkablePixel(left, bottom) &&
                isWalkablePixel(right, bottom);
    }

    private boolean isWalkablePixel(double x, double y) {
        int tileX = (int) Math.floor(x);
        int tileY = (int) Math.floor(y);

        Position p = new Position(tileX, tileY);

        if (!world.inBounds(p)) return false;

        Tile t = world.getTile(p);
        // Stasiun dianggap tembok (tidak bisa ditembus)
        return t.isWalkable();
    }

    public void dashChef(Chef chef) {
        if (chef.isBusy() || !chef.canDash()) return;

        view.gui.AssetManager.getInstance().playSound("dash");

        Direction dir = chef.getDirection();
        // Dash bergerak 15x kecepatan normal secara instan tapi tetap cek collision per step
        for (int i = 0; i < 15; i++) {
            moveChef(chef, dir);
        }
        chef.registerDash();
    }

    // --- THROW MECHANIC ---

    public void throwItem(Chef chef) {
        if (chef.isBusy() || chef.getHeldItem() == null) return;

        Item item = chef.getHeldItem();

        // Rule: Hanya bisa melempar bahan mentah/uncooked
        if (item.getState() == ItemState.COOKED || item.getState() == ItemState.BURNED) {
            System.out.println("❌ Cannot throw cooked food!");
            return;
        }

        view.gui.AssetManager.getInstance().playSound("throw");

        // Posisi awal lemparan (Center of Chef)
        double startX = chef.getExactX() + 0.5;
        double startY = chef.getExactY() + 0.5;
        double dirX = 0, dirY = 0;

        switch (chef.getDirection()) {
            case UP -> dirY = -1;
            case DOWN -> dirY = 1;
            case LEFT -> dirX = -1;
            case RIGHT -> dirX = 1;
            default -> { dirY = 1; }
        }

        // Raycast untuk memvalidasi lintasan
        double checkStep = 0.2;
        double finalDist = 0;
        boolean hitWall = false;

        for (double d = 0; d <= THROW_DISTANCE; d += checkStep) {
            double tx = startX + dirX * d;
            double ty = startY + dirY * d;

            // Cek apakah titik ini tembok
            if (!isWalkablePixel(tx, ty)) {
                hitWall = true;
                // Mundur sedikit dari tembok agar item tidak masuk ke dalam tembok
                finalDist = Math.max(0, d - 0.7);
                break;
            }
            finalDist = d;
        }

        double targetX = startX + dirX * finalDist;
        double targetY = startY + dirY * finalDist;

        // Offset target kembali ke Top-Left untuk rendering projectile
        projectiles.add(new Projectile(chef, item, startX - 0.5, startY - 0.5, targetX - 0.5, targetY - 0.5));

        chef.setHeldItem(null);
        chef.changeState(new model.chef.states.IdleState());
        System.out.println(chef.getName() + " threw " + item.getName() + "!");
    }

    // --- PROJECTILE CLASS ---

    public class Projectile {
        Chef thrower;
        Item item;
        double x, y; // Top-Left coordinate
        double targetX, targetY;
        double totalDist;
        double traveled;

        public Projectile(Chef thrower, Item item, double sx, double sy, double tx, double ty) {
            this.thrower = thrower;
            this.item = item;
            this.x = sx;
            this.y = sy;
            this.targetX = tx;
            this.targetY = ty;
            this.traveled = 0;
            this.totalDist = Math.sqrt(Math.pow(tx - sx, 2) + Math.pow(ty - sy, 2));
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public Item getItem() { return item; }

        public boolean update() {
            if (totalDist == 0) return true;

            double move = THROW_SPEED;
            if (traveled + move > totalDist) move = totalDist - traveled;

            double ratio = move / totalDist; // Simplifikasi linear
            // Interpolasi posisi
            double dx = targetX - (x + (targetX - x) * (traveled / totalDist)); // Delta sisa

            // Update posisi manual berdasarkan vektor arah
            double vecX = (targetX - x) / (totalDist - traveled) * move;
            // Koreksi matematika sederhana: LERP
            double nextRatio = (traveled + move) / totalDist;
            double nextX = x + (targetX - x) * (move / (totalDist - traveled));
            double nextY = y + (targetY - y) * (move / (totalDist - traveled));

            x = nextX;
            y = nextY;
            traveled += move;

            // Cek Collision dengan Chef lain (CATCH)
            double centerX = x + 0.5;
            double centerY = y + 0.5;

            for (Chef c : chefs) {
                if (c != thrower && !c.hasItem()) {
                    double cCenterX = c.getExactX() + 0.5;
                    double cCenterY = c.getExactY() + 0.5;

                    double dist = Math.sqrt(Math.pow(cCenterX - centerX, 2) + Math.pow(cCenterY - centerY, 2));
                    if (dist < 0.7) { // Radius tangkap
                        c.setHeldItem(item);
                        view.gui.AssetManager.getInstance().playSound("pickup");
                        System.out.println(c.getName() + " CAUGHT " + item.getName() + "!");
                        return true; // Hapus projectile
                    }
                }
            }

            // Sampai tujuan
            if (Math.abs(traveled - totalDist) < 0.05) {
                dropItem();
                return true;
            }
            return false;
        }

        private void dropItem() {
            // Drop di titik tengah projectile
            int tileX = (int) Math.floor(x + 0.5);
            int tileY = (int) Math.floor(y + 0.5);
            Position gridPos = new Position(tileX, tileY);

            Tile t = world.getTile(gridPos);
            if (t instanceof WalkableTile wt) {
                if (wt.getItem() == null) {
                    wt.setItem(item);
                } else {
                    System.out.println("Floor occupied, item lost!");
                }
            }
        }
    }

    // ... Bagian Interaksi tetap sama (gunakan int x,y dari chef) ...
    public void pickAt(Chef chef, Position p) {
        if (chef.isBusy()) return;
        Station st = world.getStationAt(p);
        if (st != null) {
            chef.tryPickFrom(st);
            return;
        }
        Tile t = world.getTile(p);
        if (t instanceof WalkableTile wt) {
            if (wt.getItem() != null && chef.getHeldItem() == null) {
                chef.setHeldItem(wt.pick());
                chef.changeState(new model.chef.states.CarryingState());
                view.gui.AssetManager.getInstance().playSound("pickup");
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
        if (st == null) {
            if (st instanceof stations.ServingStation) {
                processServing(chef, st);
                return;
            }
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
                System.out.println("✅ ORDER COMPLETED!");
                view.gui.AssetManager.getInstance().playSound("serve");
            } else {
                System.out.println("❌ WRONG ORDER! Dish discarded.");
                view.gui.AssetManager.getInstance().playSound("trash");
            }
            if (station instanceof stations.ServingStation ss) {
                if (ss.peek() == null) ss.receiveDirtyPlate();
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