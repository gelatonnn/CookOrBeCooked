package model.chef;

import items.core.Item;
import model.chef.states.*;
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
        if (dx > 0) direction = Direction.RIGHT;
        else if (dx < 0) direction = Direction.LEFT;
        else if (dy > 0) direction = Direction.DOWN;
        else if (dy < 0) direction = Direction.UP;

        state.move(this, dx, dy);
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

    // ... import items.core.CookingDevice; (Pastikan import ini ada di paling atas) ...
    // ... import items.core.Preparable; ...
    // ... import items.utensils.Plate; ...

    public void tryPickFrom(Station st) {
        // KASUS 1: Tangan Kosong -> Boleh ambil apa saja
        if (held == null) {
            Item item = st.pick(); 
            if (item != null) {
                state.pickItem(this, item); 
            }
            return;
        }

        // KASUS 2: Tangan Penuh -> Cek apakah bisa di-MERGE?
        Item itemOnStation = st.peek();
        if (itemOnStation == null) return;

        // A. Jika tangan pegang Panci/Wajan (CookingDevice), dan station ada Bahan (Preparable)
        if (held instanceof items.core.CookingDevice device && itemOnStation instanceof items.core.Preparable prep) {
            if (device.canAccept(prep)) {
                st.pick(); // SAH! Ambil item dari station
                device.addIngredient(prep); // Masukkan ke panci
                
                // FIX ERROR: Lakukan casting ke (Item) untuk mengambil nama
                String prepName = ((Item) prep).getName();
                String devName = ((Item) device).getName();
                System.out.println("Added " + prepName + " to " + devName);
            } else {
                System.out.println("Alat masak ini tidak menerima bahan tersebut!");
            }
            return;
        }

        // B. Jika tangan pegang Piring (Plate), dan station ada Bahan (Preparable)
        if (held instanceof items.utensils.Plate plate && itemOnStation instanceof items.core.Preparable prep) {
            st.pick(); // SAH!
            plate.addIngredient(prep);
            
            // Cek apakah piring jadi Dish (Resep Jadi)
            model.recipes.DishType match = model.recipes.RecipeBook.findMatch(plate.getContents());
            if (match != null) {
                if (match == model.recipes.DishType.PASTA_MARINARA) setHeldItem(new items.dish.PastaMarinara());
                else if (match == model.recipes.DishType.PASTA_BOLOGNESE) setHeldItem(new items.dish.PastaBolognese());
                else if (match == model.recipes.DishType.PASTA_FRUTTI_DI_MARE) setHeldItem(new items.dish.PastaFruttiDiMare());
                System.out.println("Plating Complete via Pick: " + match);
            }
            return;
        }

        // C. Tangan Penuh & Tidak Bisa Merge
        System.out.println("Tangan penuh! Tidak bisa mengambil " + itemOnStation.getName());
    }

    public void tryPlaceTo(Station st) {
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
}