package model.engine;

import items.core.CookingDevice;
import items.core.Item;
import items.utensils.DirtyPlate;
import items.utensils.Plate;
import model.chef.Chef;
import model.world.Tile;
import model.world.WorldMap;
import model.world.tiles.StationTile;
import model.world.tiles.WalkableTile;
import stations.Station;
import utils.TimerUtils;
import java.util.Random;

public class EffectManager {
    private static EffectManager instance;

    public enum EffectType {
        FLASH, DRUNK, DOUBLE_MONEY, HELLS_KITCHEN, MAGIC_SPONGE
    }

    private boolean isFlashActive = false;
    private boolean isDrunkActive = false;
    private boolean isDoubleMoneyActive = false;

    // Variabel Waktu
    private long effectEndTime = 0;

    // Token Sesi
    private int sessionToken = 0;

    private Runnable onSpinStart;
    private EffectType pendingEffect;

    private EffectManager() {}

    public static EffectManager getInstance() {
        if (instance == null) instance = new EffectManager();
        return instance;
    }

    public void setOnSpinStart(Runnable onSpinStart) {
        this.onSpinStart = onSpinStart;
    }

    // --- LOGIKA SPIN ---
    public void startSpin() {
        int rng = new Random().nextInt(EffectType.values().length);
        pendingEffect = EffectType.values()[rng];

        System.out.println("🎰 Gacha Result Determined: " + pendingEffect);

        if (onSpinStart != null) onSpinStart.run();
    }

    // [PERBAIKAN ERROR] Menambahkan method ini agar LuckyStation.java tidak error
    public void startGachaSequence() {
        startSpin();
    }

    public void applyPendingEffect(GameEngine engine) {
        if (pendingEffect == null) return;

        int currentToken = sessionToken;

        switch (pendingEffect) {
            case FLASH -> activateFlash(currentToken);
            case DRUNK -> activateDrunk(currentToken);
            case DOUBLE_MONEY -> activateDoubleMoney(currentToken);
            case HELLS_KITCHEN -> triggerHellsKitchen(engine);
            case MAGIC_SPONGE -> triggerMagicSponge(engine);
        }

        pendingEffect = null;
    }

    public EffectType getPendingEffect() { return pendingEffect; }

    // --- RESET ---
    public void resetEffects() {
        isFlashActive = false;
        isDrunkActive = false;
        isDoubleMoneyActive = false;
        effectEndTime = 0;
        pendingEffect = null;
        sessionToken++;
        System.out.println("✨ Effect Manager Reset! (Session: " + sessionToken + ")");
    }

    // ==========================================
    // LOGIKA EFEK
    // ==========================================

    private void activateFlash(int token) {
        if (isFlashActive) return;
        isFlashActive = true;
        effectEndTime = System.currentTimeMillis() + 15000;

        TimerUtils.schedule(() -> {
            if (sessionToken != token) return;
            isFlashActive = false;
            System.out.println("⚡ Effect Ended: The Flash");
        }, 15000);
    }

    private void activateDrunk(int token) {
        if (isDrunkActive) return;
        isDrunkActive = true;
        effectEndTime = System.currentTimeMillis() + 10000;

        TimerUtils.schedule(() -> {
            if (sessionToken != token) return;
            isDrunkActive = false;
            System.out.println("🥴 Effect Ended: Drunk Chef");
        }, 10000);
    }

    private void activateDoubleMoney(int token) {
        if (isDoubleMoneyActive) return;
        isDoubleMoneyActive = true;
        effectEndTime = System.currentTimeMillis() + 20000;

        TimerUtils.schedule(() -> {
            if (sessionToken != token) return;
            isDoubleMoneyActive = false;
            System.out.println("💰 Effect Ended: Double Money");
        }, 20000);
    }

    private void triggerHellsKitchen(GameEngine engine) {
        System.out.println("🔥 EFFECT: HELL'S KITCHEN!");
        WorldMap map = engine.getWorld();
        boolean burned = false;

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile t = map.getTile(new utils.Position(x, y));
                if (t instanceof StationTile st) {
                    Item item = st.getStation().peek();
                    if (item instanceof CookingDevice dev && dev.isCooking()) {
                        dev.forceBurn();
                        burned = true;
                        break;
                    }
                }
            }
            if (burned) break;
        }
    }

    private void triggerMagicSponge(GameEngine engine) {
        System.out.println("✨ EFFECT: MAGIC SPONGE!");
        WorldMap map = engine.getWorld();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile t = map.getTile(new utils.Position(x, y));

                if (t instanceof StationTile st) {
                    Station station = st.getStation();
                    if (station.peek() instanceof DirtyPlate) {
                        station.pick();
                        station.place(new Plate());
                    }
                }

                if (t instanceof WalkableTile wt && wt.getItem() instanceof DirtyPlate) {
                    wt.pick();
                    wt.setItem(new Plate());
                }
            }
        }

        for (Chef c : engine.getChefs()) {
            if (c.getHeldItem() instanceof DirtyPlate) {
                c.setHeldItem(new Plate());
            }
        }
    }

    // --- GETTERS ---
    public boolean isFlash() { return isFlashActive; }
    public boolean isDrunk() { return isDrunkActive; }
    public boolean isDoubleMoney() { return isDoubleMoneyActive; }

    public long getTimeRemaining() {
        return Math.max(0, effectEndTime - System.currentTimeMillis());
    }
}