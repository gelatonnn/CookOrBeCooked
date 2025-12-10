package model.chef;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;
import items.dish.*;
import items.utensils.Plate;
import model.chef.states.*;
import model.recipes.DishType;
import model.recipes.RecipeBook;
import stations.Station;
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

    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN_MS = 2000;

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

    public boolean canDash() {
        return System.currentTimeMillis() - lastDashTime >= DASH_COOLDOWN_MS;
    }

    public void registerDash() {
        this.lastDashTime = System.currentTimeMillis();
    }

    public void changeState(ChefState s) {
        this.state = s;
        
        // Update Action SEBELUM enter() untuk mencegah bug nested state change
        // Ini memastikan status diset dulu, baru logika dijalankan
        if (s instanceof IdleState) currentAction = ChefAction.IDLE;
        else if (s instanceof MovingState) currentAction = ChefAction.MOVING;
        else if (s instanceof CarryingState) currentAction = ChefAction.CARRYING;
        else if (s instanceof BusyCuttingState) currentAction = ChefAction.CUTTING;
        else if (s instanceof BusyCookingState) currentAction = ChefAction.COOKING;
        else if (s instanceof BusyWashingState) currentAction = ChefAction.WASHING;

        // Jalankan logika masuk state (yang mungkin memanggil changeState lagi)
        s.enter(this);
    }

    public void tryPickFrom(Station st) {
        if (held == null) {
            Item item = st.pick();
            if (item != null) {
                state.pickItem(this, item);
                view.gui.AssetManager.getInstance().playSound("pickup");
            }
            return;
        }

        Item itemOnStation = st.peek();
        if (itemOnStation == null) return;

        if (held instanceof CookingDevice device && itemOnStation instanceof Preparable prep) {
            if (device.canAccept(prep)) {
                st.pick();
                device.addIngredient(prep);
                System.out.println("Added " + ((Item)prep).getName() + " to " + ((Item)device).getName());
            } else {
                System.out.println("Alat masak ini tidak menerima bahan tersebut!");
            }
            return;
        }

        if (held instanceof items.utensils.Plate plate && itemOnStation instanceof Preparable prep) {
            st.pick();
            plate.addIngredient(prep);

            DishType match = RecipeBook.findMatch(plate.getContents());
            if (match != null) {
                if (match == DishType.PASTA_MARINARA) setHeldItem(new PastaMarinara());
                else if (match == DishType.PASTA_BOLOGNESE) setHeldItem(new PastaBolognese());
                else if (match == DishType.PASTA_FRUTTI_DI_MARE) setHeldItem(new PastaFruttiDiMare());
                System.out.println("Plating Complete via Pick: " + match);
            }
            return;
        }

        System.out.println("Tangan penuh! Tidak bisa mengambil " + itemOnStation.getName());
    }

    public void tryPlaceTo(Station st) {
        Item itemOnStation = st.peek();

        if (held instanceof CookingDevice device && itemOnStation instanceof Plate plate) {
            if (!plate.isClean()) {
                System.out.println("❌ Gagal: Piring kotor!");
                return;
            }
            if (device.getContents().isEmpty()) {
                System.out.println("❌ Gagal: Alat masak kosong!");
                return;
            }

            System.out.println("Menuang isi " + ((Item)device).getName() + " ke Piring...");
            view.gui.AssetManager.getInstance().playSound("place");

            for (Preparable ingredient : device.getContents()) {
                plate.addIngredient(ingredient);
            }

            DishType match = RecipeBook.findMatch(plate.getContents());
            if (match != null) {
                DishBase finalDish = null;
                switch (match) {
                    case PASTA_MARINARA -> finalDish = new PastaMarinara();
                    case PASTA_BOLOGNESE -> finalDish = new PastaBolognese();
                    case PASTA_FRUTTI_DI_MARE -> finalDish = new PastaFruttiDiMare();
                }

                if (finalDish != null) {
                    st.pick();
                    st.place(finalDish);
                    System.out.println("✨ Plating Berhasil: " + match);
                    view.gui.AssetManager.getInstance().playSound("serve");
                }
            } else {
                System.out.println("⚠️ Bahan dituang, tapi belum jadi menu lengkap.");
            }

            device.clearContents();
            return;
        }

        state.placeItem(this, st);
    }

    public void tryInteract(Station st) {
        state.interact(this, st);
    }

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
    
    // Helper untuk progress bar UI
    public float getActionProgress() {
        if (state instanceof BusyCuttingState cuttingState) {
            return (float) cuttingState.getProgress() / cuttingState.getMaxProgress();
        }
        return 0f;
    }
}