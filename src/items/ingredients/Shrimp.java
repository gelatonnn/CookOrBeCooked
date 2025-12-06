package items.ingredients;

import items.core.ItemState;

public class Shrimp extends IngredientBase {
    @Override
    public String getName() { return "Shrimp"; }

    @Override
    public boolean canBeChopped() { return getState() == ItemState.RAW; }

    @Override
    public boolean canBeCooked() { return getState() == ItemState.CHOPPED; }
}