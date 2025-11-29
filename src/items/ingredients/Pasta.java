package items.ingredients;

import items.core.ItemState;

public class Pasta extends IngredientBase {

    @Override
    public String getName() {
        return "Pasta";
    }

    // Pasta disini ga perlu dipotong
    @Override
    public boolean canBeChopped() {
        return false;
    }

    // Pasta bisa langsung dimasak saat RAW
    @Override
    public boolean canBeCooked() {
        return getState() == ItemState.RAW;
    }
}
