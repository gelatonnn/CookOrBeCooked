package items.dish;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

import java.util.ArrayList;
import java.util.List;

public abstract class DishBase extends Item {

    protected List<Preparable> ingredients = new ArrayList<>();
    protected DishState dishState = DishState.INCOMPLETE;

    public DishBase() {
        this.portable = true;
        this.state = ItemState.CLEAN; // dish is not food, so CLEAN state
    }

    @Override
    public boolean isPortable() {
        return true;
    }

    public abstract String getName();

    public abstract boolean validateRecipe(List<Preparable> ingredients);

    public DishState getDishState() {
        return dishState;
    }

    public List<Preparable> getIngredients() {
        return ingredients;
    }

    public boolean addIngredient(Preparable ingredient) {
        if (ingredient == null) return false;
        if (ingredient.canBePlacedOnPlate()) {
            ingredients.add(ingredient);
            updateState();
            return true;
        }
        return false;
    }

    private void updateState() {
        if (validateRecipe(ingredients)) {
            dishState = DishState.COMPLETE;
        } else {
            dishState = DishState.INCOMPLETE;
        }
    }
}
