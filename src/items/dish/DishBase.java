package items.dish;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;
import model.recipes.Recipe;

import java.util.ArrayList;
import java.util.List;

public class DishBase extends Item {
    protected final Recipe recipe;
    protected final List<Preparable> ingredients = new ArrayList<>();
    protected DishState dishState = DishState.INCOMPLETE;

    public DishBase(Recipe recipe) {
        this.recipe = recipe;
        this.state = ItemState.RAW;
        this.portable = true;
    }

    @Override
    public String getName() {
        return recipe.getType().name().replace("_", " ");
    }

    public DishState getDishState() {
        return dishState;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<Preparable> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public boolean addIngredient(Preparable p) {
        if (p == null) return false;
        ingredients.add(p);

        if (recipe.matches(ingredients)) {
            dishState = DishState.COMPLETE;
            state = ItemState.COOKED;
        } else {
            dishState = DishState.INCOMPLETE;
        }
        return true;
    }

    public void clearIngredients() {
        ingredients.clear();
        dishState = DishState.INCOMPLETE;
    }

    @Override
    public String toString() {
        return getName() + " [" + dishState + "] with " + ingredients.size() + " ingredients";
    }
}