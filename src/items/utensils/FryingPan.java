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

    @Override
    public boolean isPortable() {
        return true;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean canAccept(Preparable ingredient) {
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
        for (Preparable p : contents) {
            p.cook();
        }
    }

    @Override
    public String getName() {
        return "Frying Pan";
    }
}
