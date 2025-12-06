package model.recipes;

import items.core.Preparable;
import java.util.List;

public class Recipe {
    private final DishType type;
    private final List<Class<? extends Preparable>> requiredIngredients;

    public Recipe(DishType type, List<Class<? extends Preparable>> requiredIngredients) {
        this.type = type;
        this.requiredIngredients = requiredIngredients;
    }

    public DishType getType() {
        return type;
    }

    public String getName() {
        return type.name().replace("_", " ");
    }

    public List<Class<? extends Preparable>> getRequiredIngredients() {
        return requiredIngredients;
    }

    public boolean matches(List<Preparable> ingreds) {
        if (ingreds.size() != requiredIngredients.size()) return false;

        boolean[] used = new boolean[ingreds.size()];

        for (Class<? extends Preparable> req : requiredIngredients) {
            boolean found = false;

            for (int i = 0; i < ingreds.size(); i++) {
                if (!used[i] && req.isInstance(ingreds.get(i))) {
                    used[i] = true;
                    found = true;
                    break;
                }
            }

            if (!found) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return getName() + " (requires " + requiredIngredients.size() + " ingredients)";
    }
}