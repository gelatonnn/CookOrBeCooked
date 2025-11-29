package items.ingredients;

import items.core.ItemState;

public class Tomato extends IngredientBase {

    @Override
    public String getName() {
        return "Tomato";
    }

    @Override
    public boolean canBeChopped() {
        return getState() == ItemState.RAW;
    }

    @Override
    public boolean canBeCooked() {
        return getState() == ItemState.CHOPPED;
    }
}
