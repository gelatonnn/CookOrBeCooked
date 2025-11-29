package items.utensils;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

import java.util.ArrayList;
import java.util.List;

public class Plate extends UtensilBase {

    private ItemState cleanliness = ItemState.CLEAN;
    private final List<Preparable> contents = new ArrayList<>();

    public Plate() {}

    public boolean isClean() {
        return cleanliness == ItemState.CLEAN;
    }

    public void makeDirty() {
        cleanliness = ItemState.DIRTY;
    }

    public void wash() {
        cleanliness = ItemState.CLEAN;
    }

    public void addIngredient(Preparable ingredient) {
        contents.add(ingredient);
    }

    public List<Preparable> getContents() {
        return contents;
    }

    @Override
    public String getName() {
        return "Plate";
    }
}
