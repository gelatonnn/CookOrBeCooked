package items.ingredients;

import items.core.ItemState;

public class Fish extends IngredientBase {
    @Override
    public String getName() { return "Fish"; }

    @Override
    public boolean canBeChopped() { return getState() == ItemState.RAW; }

    @Override
    public boolean canBeCooked() { return getState() == ItemState.CHOPPED; }
}