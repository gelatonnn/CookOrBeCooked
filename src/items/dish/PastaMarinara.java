package items.dish;

import model.recipes.RecipeBook;
import model.recipes.DishType;

public class PastaMarinara extends DishBase {
    public PastaMarinara() {
        super(RecipeBook.getRecipe(DishType.PASTA_MARINARA));
    }

    @Override
    public String getName() {
        return "Pasta Marinara";
    }
}