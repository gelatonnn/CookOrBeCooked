package tile;

import chef.Chef;
import factory.ItemFactory;
import items.core.Item;
import model.Position;

public class IngredientStorage extends Station {

    private String ingredientType;

    public IngredientStorage(Position pos, String ingredientType) {
        super(pos, StationType.INGREDIENT_STORAGE);
        this.ingredientType = ingredientType;
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (c.getHeldItem() != null) {
            return new InteractionResult(false, "Chef sudah memegang item.");
        }

        Item ingredient = ItemFactory.create(ingredientType);
        c.setHeldItem(ingredient);
        return new InteractionResult(true, "Mengambil " + ingredient.getName());
    }

    @Override
    public boolean isWalkable() {
        return true;
    }
    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Ingredient Storage.");
    }
}
