package items.dish;

import model.recipes.RecipeBook;
import model.recipes.DishType;

public class PastaBolognese extends DishBase {
    public PastaBolognese() {
        super(RecipeBook.getRecipe(DishType.PASTA_BOLOGNESE));
    }

    @Override
    public String getName() {
        return "Pasta Bolognese";
    }
}