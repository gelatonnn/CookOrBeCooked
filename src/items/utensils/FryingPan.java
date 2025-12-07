package items.utensils;

import items.core.CookingDevice;
import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

import java.util.ArrayList;
import java.util.List;

public class FryingPan extends UtensilBase implements CookingDevice {
    private final List<Preparable> contents = new ArrayList<>();
    private final int capacity = 2;
    private boolean cooking = false;

    @Override
    public String getName() { return "Frying Pan"; }

    @Override
    public boolean isPortable() { return true; }

    @Override
    public int capacity() { return capacity; }

    @Override
    public boolean canAccept(Preparable ingredient) {
        if (contents.size() >= capacity) {
            System.out.println("⚠ Frying pan is full!");
            return false;
        }

        ItemState state = ((Item) ingredient).getState();
        boolean canAccept = state == ItemState.CHOPPED;

        if (!canAccept) {
            System.out.println("❌ Frying pan only accepts CHOPPED ingredients!");
        }

        return canAccept;
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (contents.size() < capacity && canAccept(ingredient)) {
            contents.add(ingredient);
            System.out.println("✅ Added " + ((Item)ingredient).getName() + " to frying pan");
        }
    }

    @Override
    public void startCooking() {
        if (contents.isEmpty()) {
            System.out.println("❌ Frying pan is empty! Add CHOPPED ingredients first.");
            return;
        }

        if (cooking) {
            System.out.println("⚠ Already cooking!");
            return;
        }

        cooking = true;
        System.out.println("🔥 Started cooking in frying pan...");
        for (Preparable p : contents) {
            p.cook();
        }
    }

    @Override
    public void finishCooking() {
        cooking = false;
        System.out.println("✅ Cooking finished in frying pan");
    }

    @Override
    public List<Preparable> getContents() {
        return new ArrayList<>(contents);
    }

    @Override
    public void clearContents() {
        contents.clear();
        cooking = false;
    }

    @Override
    public boolean isCooking() {
        return cooking;
    }

    @Override
    public String toString() {
        return getName() + " [" + contents.size() + "/" + capacity + " items]" +
                (cooking ? " COOKING" : "");
    }
}