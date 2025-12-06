package model.recipes;

import items.ingredients.*;
import java.util.*;

public class RecipeBook {
    private static final Map<DishType, Recipe> recipes = new EnumMap<>(DishType.class);

    static {
        recipes.put(
                DishType.PASTA_MARINARA,
                new Recipe(DishType.PASTA_MARINARA, List.of(Pasta.class, Tomato.class))
        );

        recipes.put(
                DishType.PASTA_BOLOGNESE,
                new Recipe(DishType.PASTA_BOLOGNESE, List.of(Pasta.class, Meat.class))
        );

        recipes.put(
                DishType.PASTA_FRUTTI_DI_MARE,
                new Recipe(DishType.PASTA_FRUTTI_DI_MARE,
                        List.of(Pasta.class, Shrimp.class, Fish.class))
        );
    }

    public static Recipe getRecipe(DishType type) {
        return recipes.get(type);
    }

    public static DishType getRandomDish() {
        DishType[] arr = DishType.values();
        return arr[new Random().nextInt(arr.length)];
    }
}