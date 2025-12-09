package model.chef;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;
import items.dish.*;
import items.utensils.Plate;
import model.chef.states.*;
import model.recipes.DishType;
import model.recipes.RecipeBook;
import stations.Station; // Untuk PastaMarinara, dll
import utils.Direction;
import utils.Position;

public class Chef {
    private final String id;
    private final String name;
    private int x, y;
    private Direction direction;
    private Item held;
    private ChefState state;
    private ChefAction currentAction;

    // NEW: Dash Cooldown variables
    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN_MS = 2000; // 2 seconds cooldown

    public Chef(String id, String name, int x, int y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.direction = Direction.DOWN;
        this.state = new IdleState();
        this.currentAction = ChefAction.IDLE;
    }

    public Chef(int x, int y) {
        this("chef_" + System.currentTimeMillis(), "Chef", x, y);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Position getPos() { return new Position(x, y); }
    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    public ChefAction getCurrentAction() { return currentAction; }
    public ChefState getState() { return state; }
    public Item getHeldItem() { return held; }
    public boolean hasItem() { return held != null; }

    // Setters
    public void setDirection(Direction dir) { this.direction = dir; }
    public void setCurrentAction(ChefAction action) { this.currentAction = action; }
    public void setHeldItem(Item item) { this.held = item; }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int dx, int dy) {
        // UPDATED: Logic to handle 8 directions
        if (dx == 0 && dy < 0) direction = Direction.UP;
        else if (dx == 0 && dy > 0) direction = Direction.DOWN;
        else if (dx < 0 && dy == 0) direction = Direction.LEFT;
        else if (dx > 0 && dy == 0) direction = Direction.RIGHT;
        else if (dx < 0 && dy < 0) direction = Direction.UP_LEFT;
        else if (dx > 0 && dy < 0) direction = Direction.UP_RIGHT;
        else if (dx < 0 && dy > 0) direction = Direction.DOWN_LEFT;
        else if (dx > 0 && dy > 0) direction = Direction.DOWN_RIGHT;

        state.move(this, dx, dy);
    }

    // NEW: Dash Cooldown Logic
    public boolean canDash() {
        return System.currentTimeMillis() - lastDashTime >= DASH_COOLDOWN_MS;
    }

    public void registerDash() {
        this.lastDashTime = System.currentTimeMillis();
    }

    public void changeState(ChefState s) {
        this.state = s;
        s.enter(this);

        if (s instanceof IdleState) currentAction = ChefAction.IDLE;
        else if (s instanceof MovingState) currentAction = ChefAction.MOVING;
        else if (s instanceof CarryingState) currentAction = ChefAction.CARRYING;
        else if (s instanceof BusyCuttingState) currentAction = ChefAction.CUTTING;
        else if (s instanceof BusyCookingState) currentAction = ChefAction.COOKING;
        else if (s instanceof BusyWashingState) currentAction = ChefAction.WASHING;
    }

    public void tryPickFrom(Station st) {
        if (held == null) {
            Item item = st.pick();
            if (item != null) {
                state.pickItem(this, item);
            }
            return;
        }

        Item itemOnStation = st.peek();
        if (itemOnStation == null) return;

        if (held instanceof items.core.CookingDevice device && itemOnStation instanceof items.core.Preparable prep) {
            if (device.canAccept(prep)) {
                st.pick();
                device.addIngredient(prep);
                System.out.println("Added " + ((Item)prep).getName() + " to " + ((Item)device).getName());
            } else {
                System.out.println("Alat masak ini tidak menerima bahan tersebut!");
            }
            return;
        }

        if (held instanceof items.utensils.Plate plate && itemOnStation instanceof items.core.Preparable prep) {
            st.pick();
            plate.addIngredient(prep);

            model.recipes.DishType match = model.recipes.RecipeBook.findMatch(plate.getContents());
            if (match != null) {
                if (match == model.recipes.DishType.PASTA_MARINARA) setHeldItem(new items.dish.PastaMarinara());
                else if (match == model.recipes.DishType.PASTA_BOLOGNESE) setHeldItem(new items.dish.PastaBolognese());
                else if (match == model.recipes.DishType.PASTA_FRUTTI_DI_MARE) setHeldItem(new items.dish.PastaFruttiDiMare());
                System.out.println("Plating Complete via Pick: " + match);
            }
            return;
        }

        System.out.println("Tangan penuh! Tidak bisa mengambil " + itemOnStation.getName());
    }

    public void tryPlaceTo(Station st) {
        Item itemOnStation = st.peek();

        // --- LOGIKA BARU: Menuang isi Panci/Wajan ke Piring di Station ---
        if (held instanceof CookingDevice device && itemOnStation instanceof Plate plate) {
            
            // 1. Validasi: Piring harus bersih & Alat masak ada isinya
            if (!plate.isClean()) {
                System.out.println("❌ Gagal: Piring kotor!");
                return;
            }
            if (device.getContents().isEmpty()) {
                System.out.println("❌ Gagal: Alat masak kosong!");
                return;
            }

            // 2. Pindahkan isi alat masak ke piring
            System.out.println("Menuang isi " + ((Item)device).getName() + " ke Piring...");
            for (Preparable ingredient : device.getContents()) {
                plate.addIngredient(ingredient);
            }

            // 3. Cek apakah kombinasi bahan di piring membentuk Dish (Resep Valid)
            DishType match = RecipeBook.findMatch(plate.getContents());
            
            if (match != null) {
                // Jika resep valid, ubah item di station dari 'Plate' menjadi 'Dish'
                items.dish.DishBase finalDish = null;
                
                switch (match) {
                    case PASTA_MARINARA -> finalDish = new PastaMarinara();
                    case PASTA_BOLOGNESE -> finalDish = new PastaBolognese();
                    case PASTA_FRUTTI_DI_MARE -> finalDish = new PastaFruttiDiMare();
                }

                if (finalDish != null) {
                    st.pick(); // Ambil piring biasa dari station (hapus ref lama)
                    st.place(finalDish); // Taruh Dish jadi (yang sudah ada piringnya secara konsep)
                    System.out.println("✨ Plating Berhasil: " + match);
                }
            } else {
                System.out.println("⚠️ Bahan dituang, tapi belum jadi menu lengkap.");
            }

            // 4. Kosongkan alat masak di tangan
            device.clearContents();
            return; // Return agar tidak lanjut ke logika default placeItem
        }
        // -------------------------------------------------------------

        // Logika default (state pattern) untuk menaruh item biasa
        state.placeItem(this, st);
    }

    public void tryInteract(Station st) {
        state.interact(this, st);
    }

    // FIX (Retained): Ensure state reset after throwing
    public void throwItem(boolean[][] worldMask) {
        if (held == null) return;
        System.out.println(name + " threw " + held.getName() + "!");
        held = null;
        changeState(new IdleState());
    }

    public Position getFacingPosition() {
        return getPos().move(direction);
    }

    public boolean isBusy() {
        return currentAction == ChefAction.CUTTING ||
                currentAction == ChefAction.COOKING ||
                currentAction == ChefAction.WASHING;
    }

    @Override
    public String toString() {
        return name + " at (" + x + "," + y + ") facing " + direction +
                " [" + currentAction + "]" +
                (held != null ? " holding " + held.getName() : "");
    }
    
    public float getActionProgress() {
    if (state instanceof BusyCuttingState cuttingState) {
        return (float) cuttingState.getProgress() / cuttingState.getMaxProgress();
    }
    return 0f;
}
}