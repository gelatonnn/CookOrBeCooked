package items.utensils;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;

import java.util.ArrayList;
import java.util.List;

public class BoilingPot extends UtensilBase implements CookingDevice {

    private final List<Preparable> contents = new ArrayList<>();
    private final int capacity = 3;

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
        String name = ((Item) ingredient).getName().toLowerCase();
        return name.contains("pasta");
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
            p.cook(); // cooking timer handled in IngredientBase
        }
    }

    @Override
    public String getName() {
        return "Boiling Pot";
    }
}
