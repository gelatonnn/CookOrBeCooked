package items.ingredients;

import items.core.ItemState;

public class Meat extends IngredientBase {
    @Override
    public String getName() { return "Meat"; }

    @Override
    public boolean canBeChopped() { return getState() == ItemState.RAW; }

    @Override
    public boolean canBeCooked() { return getState() == ItemState.CHOPPED; }
}