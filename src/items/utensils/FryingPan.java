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
        if (contents.size() >= capacity) return false;
        return ((Item) ingredient).getState() == ItemState.CHOPPED;
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (contents.size() < capacity && canAccept(ingredient)) {
            contents.add(ingredient);
        }
    }

    @Override
    public void startCooking() {
        if (contents.isEmpty() || cooking) return;
        cooking = true;
        for (Preparable p : contents) p.cook();
    }

    @Override
    public void finishCooking() {
        cooking = false;
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
        return getName() + " [" + contents.size() + "/" + capacity + "]" +
                (cooking ? " COOKING" : "");
    }
}