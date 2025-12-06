package items.dish;

import model.recipes.RecipeBook;
import model.recipes.DishType;

public class PastaFruttiDiMare extends DishBase {
    public PastaFruttiDiMare() {
        super(RecipeBook.getRecipe(DishType.PASTA_FRUTTI_DI_MARE));
    }

    @Override
    public String getName() {
        return "Pasta Frutti di Mare";
    }
}