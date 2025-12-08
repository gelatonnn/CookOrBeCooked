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
    public String getName() {
        return "Ingredient Storage [" + ingredientType.toUpperCase() + "]";
    }

    @Override
    public boolean canPlace(Item item) {
        return false;
    }

    @Override
    public boolean place(Item item) {
        System.out.println("❌ Cannot place items in ingredient storage!");
        return false;
    }

    @Override
    public Item pick() {
        try {
            Item item = ItemFactory.create(ingredientType);
            System.out.println("📦 Got " + item.getName() + " from storage");
            return item;
        } catch (Exception e) {
            System.out.println("❌ Ingredient not available: " + ingredientType);
            return null;
        }
    }

    @Override
    public Item peek() {
        return null;
    }

    @Override
    public boolean isOccupied() {
        return false;
    }
}