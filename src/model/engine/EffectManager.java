package model.engine;

import items.core.CookingDevice;
import items.core.Item;
import items.core.ItemState;
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
    
    // Enum Jenis Efek
    public enum EffectType {
        FLASH,          // Lari Cepat
        DRUNK,          // Kontrol Terbalik
        DOUBLE_MONEY,   // Skor x2
        HELLS_KITCHEN,  // Masakan Gosong
        MAGIC_SPONGE    // Piring Bersih
    }

    // Status Efek Aktif (Buff/Debuff Duration)
    private boolean isFlashActive = false;
    private boolean isDrunkActive = false;
    private boolean isDoubleMoneyActive = false;

    // Variabel Gacha Logic
    private Runnable onSpinStart; // Callback ke GUI
    private EffectType pendingEffect; // Hasil Gacha yang menunggu animasi selesai

    private EffectManager() {}

    public static EffectManager getInstance() {
        if (instance == null) instance = new EffectManager();
        return instance;
    }

    // --- SETUP LISTENER (Dipanggil oleh GamePanel) ---
    public void setOnSpinStart(Runnable onSpinStart) {
        this.onSpinStart = onSpinStart;
    }

    // --- STEP 1: Mulai Sequence (Dipanggil oleh LuckyStation) ---
    public void startGachaSequence() {
        // 1. Tentukan hasil sekarang (RNG Backend)
        int rng = new Random().nextInt(EffectType.values().length);
        pendingEffect = EffectType.values()[rng];
        
        System.out.println("🎰 Gacha Result Determined: " + pendingEffect);

        // 2. Beritahu GUI untuk mulai animasi visual (Overlay)
        if (onSpinStart != null) {
            onSpinStart.run();
        }
    }

    // --- STEP 2: Terapkan Efek (Dipanggil oleh SpinOverlay setelah animasi) ---
    public void applyPendingEffect(GameEngine engine) {
        if (pendingEffect == null) return;

        System.out.println("Applying Effect: " + pendingEffect);

        switch (pendingEffect) {
            case FLASH -> activateFlash();
            case DRUNK -> activateDrunk();
            case DOUBLE_MONEY -> activateDoubleMoney();
            case HELLS_KITCHEN -> triggerHellsKitchen(engine);
            case MAGIC_SPONGE -> triggerMagicSponge(engine);
        }
        
        pendingEffect = null; // Reset setelah dipakai
    }
    
    // Getter untuk GUI (agar Overlay tahu harus berhenti di gambar mana)
    public EffectType getPendingEffect() {
        return pendingEffect;
    }

    // ==========================================
    // LOGIKA EFEK (BUFF / DEBUFF)
    // ==========================================

    // 1. THE FLASH (15 Detik)
    // Effect: Kecepatan gerak x2, Dash tanpa cooldown
    private void activateFlash() {
        if (isFlashActive) return; // Jangan tumpuk durasi
        System.out.println("⚡ EFFECT: THE FLASH! (Speed x2, No Dash Cooldown)");
        isFlashActive = true;
        
        TimerUtils.schedule(() -> {
            isFlashActive = false;
            System.out.println("⚡ Effect Ended: The Flash");
        }, 15000);
    }

    // 2. DRUNK CHEF (10 Detik)
    // Effect: Kontrol WASD/Arrow terbalik
    private void activateDrunk() {
        if (isDrunkActive) return;
        System.out.println("🥴 EFFECT: DRUNK CHEF! (Controls Inverted)");
        isDrunkActive = true;
        
        TimerUtils.schedule(() -> {
            isDrunkActive = false;
            System.out.println("🥴 Effect Ended: Drunk Chef");
        }, 10000);
    }

    // 3. DOUBLE MONEY (20 Detik)
    // Effect: Skor order jadi 2x lipat
    private void activateDoubleMoney() {
        if (isDoubleMoneyActive) return;
        System.out.println("💰 EFFECT: DOUBLE MONEY! (2x Score)");
        isDoubleMoneyActive = true;
        
        TimerUtils.schedule(() -> {
            isDoubleMoneyActive = false;
            System.out.println("💰 Effect Ended: Double Money");
        }, 20000);
    }

    // 4. HELL'S KITCHEN (Instant)
    // Effect: Salah satu masakan yang sedang dimasak langsung GOSONG
    private void triggerHellsKitchen(GameEngine engine) {
        System.out.println("🔥 EFFECT: HELL'S KITCHEN! (Random active cooking burned)");
        WorldMap map = engine.getWorld();
        boolean burned = false;

        // Cari di seluruh station
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile t = map.getTile(new utils.Position(x, y));
                if (t instanceof StationTile st) {
                    Item item = st.getStation().peek();
                    // Cek apakah itu alat masak yang sedang aktif
                    if (item instanceof CookingDevice dev && dev.isCooking()) {
                        dev.forceBurn(); // Paksa gosong
                        burned = true;
                        // Kita cukup bakar satu saja biar tidak terlalu kejam, atau hapus 'break' untuk bakar semua
                        break; 
                    }
                }
            }
            if (burned) break;
        }
        
        if (!burned) System.out.println("   (Lucky! No active cooking found to burn)");
    }

    // 5. MAGIC SPONGE (Instant)
    // Effect: Semua piring kotor di map & inventory jadi bersih
    private void triggerMagicSponge(GameEngine engine) {
        System.out.println("✨ EFFECT: MAGIC SPONGE! (All dirty plates cleaned)");
        WorldMap map = engine.getWorld();
        int count = 0;

        // A. Bersihkan di Map (Station & Lantai)
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile t = map.getTile(new utils.Position(x, y));
                
                // Cek Station
                if (t instanceof StationTile st) {
                    Station station = st.getStation();
                    if (station.peek() instanceof DirtyPlate) {
                        station.pick(); 
                        station.place(new Plate()); // Ganti baru
                        count++;
                    }
                }
                
                // Cek Lantai
                if (t instanceof WalkableTile wt && wt.getItem() instanceof DirtyPlate) {
                    wt.pick();
                    wt.setItem(new Plate());
                    count++;
                }
            }
        }
        
        // B. Bersihkan di Inventory Chef
        for (Chef c : engine.getChefs()) {
            if (c.getHeldItem() instanceof DirtyPlate) {
                c.setHeldItem(new Plate());
                count++;
            }
        }
        
        System.out.println("   Cleaned " + count + " dirty plates!");
    }

    // --- PUBLIC GETTERS (Untuk Logic di GameController/Chef/OrderManager) ---
    public boolean isFlash() { return isFlashActive; }
    public boolean isDrunk() { return isDrunkActive; }
    public boolean isDoubleMoney() { return isDoubleMoneyActive; }
}