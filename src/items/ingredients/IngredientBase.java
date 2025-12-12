package items.ingredients;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

public abstract class IngredientBase extends Item implements Preparable {

    public IngredientBase() {
        this.state = ItemState.RAW;
        this.portable = true;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return state == ItemState.COOKED || state == ItemState.CHOPPED || state == ItemState.BURNED;
    }

    @Override
    public void chop() {
        if (state == ItemState.RAW && canBeChopped()) {
            state = ItemState.CHOPPED;
            System.out.println("✂️ " + getName() + " is now CHOPPED");
        } else {
            System.out.println("⚠️ " + getName() + " sudah dipotong atau tidak bisa dipotong!");
        }
    }

    @Override
    public void cook() {
        if (state == ItemState.RAW || state == ItemState.CHOPPED) {
            state = ItemState.COOKED;
            System.out.println("✅ " + getName() + " is now COOKED!");
        }
    }
    
    public void burn() {
        if (state != ItemState.BURNED) {
            state = ItemState.BURNED;
            System.out.println("🔥💀 " + getName() + " is BURNED!");
        }
    }
}