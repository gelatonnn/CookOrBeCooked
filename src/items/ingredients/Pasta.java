package items.ingredients;

import items.core.ItemState;

public class Pasta extends IngredientBase {
    @Override
    public String getName() { return "Pasta"; }

    @Override
    public boolean canBeChopped() { return false; }

    @Override
    public boolean canBeCooked() {
        return getState() == ItemState.RAW;
    }
}