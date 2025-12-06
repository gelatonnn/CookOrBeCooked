package stations;

import factory.ItemFactory;
import items.core.Item;

public class IngredientStorage implements Station {
    private final String ingredientType;

    public IngredientStorage() {
        this("pasta");
    }

    public IngredientStorage(String type) {
        this.ingredientType = type;
    }

    @Override
    public String getName() { return "Ingredient Storage"; }

    @Override
    public boolean canPlace(Item item) { return false; }

    @Override
    public boolean place(Item item) { return false; }

    @Override
    public Item pick() {
        return ItemFactory.create(ingredientType);
    }

    @Override
    public Item peek() { return null; }

    @Override
    public boolean isOccupied() { return false; }
}