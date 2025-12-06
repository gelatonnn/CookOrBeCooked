package factory;

import items.ingredients.*;
import items.utensils.*;
import items.dish.*;
import model.recipes.*;

public class ItemRegistryInit {
    public static void registerAll() {
        // INGREDIENTS
        ItemFactory.register("pasta", Pasta::new);
        ItemFactory.register("tomato", Tomato::new);
        ItemFactory.register("meat", Meat::new);
        ItemFactory.register("fish", Fish::new);
        ItemFactory.register("shrimp", Shrimp::new);

        // UTENSILS
        ItemFactory.register("plate", Plate::new);
        ItemFactory.register("boiling_pot", BoilingPot::new);
        ItemFactory.register("frying_pan", FryingPan::new);

        // DISHES
        ItemFactory.register("pasta_marinara",
                () -> new DishBase(RecipeBook.getRecipe(DishType.PASTA_MARINARA)));
        ItemFactory.register("pasta_bolognese",
                () -> new DishBase(RecipeBook.getRecipe(DishType.PASTA_BOLOGNESE)));
        ItemFactory.register("pasta_frutti_di_mare",
                () -> new DishBase(RecipeBook.getRecipe(DishType.PASTA_FRUTTI_DI_MARE)));
    }
}