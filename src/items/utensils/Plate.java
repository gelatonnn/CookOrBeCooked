package items.utensils;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

import java.util.ArrayList;
import java.util.List;

public class Plate extends UtensilBase {
    private ItemState cleanliness = ItemState.CLEAN;
    private final List<Preparable> contents = new ArrayList<>();

    @Override
    public String getName() { return "Plate"; }

    public boolean isClean() {
        return cleanliness == ItemState.CLEAN;
    }

    public void wash() {
        cleanliness = ItemState.CLEAN;
        contents.clear();
    }

    public void makeDirty() {
        cleanliness = ItemState.DIRTY;
    }

    public void addIngredient(Preparable p) {
        contents.add(p);
    }

    public List<Preparable> getContents() {
        return new ArrayList<>(contents);
    }

    public void clearIngredients() {
        contents.clear();
    }

    @Override
    public String toString() {
        String status = isClean() ? "CLEAN" : "DIRTY";
        return getName() + " [" + status + "] with " + contents.size() + " items";
    }
}