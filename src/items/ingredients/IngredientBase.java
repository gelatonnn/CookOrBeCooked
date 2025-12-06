package items.ingredients;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;
import utils.TimerUtils;

public abstract class IngredientBase extends Item implements Preparable {
    private boolean cooking = false;

    public IngredientBase() {
        this.state = ItemState.RAW;
        this.portable = true;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return state == ItemState.COOKED || state == ItemState.CHOPPED;
    }

    @Override
    public void chop() {
        if (canBeChopped() && state == ItemState.RAW) {
            state = ItemState.CHOPPED;
        }
    }

    @Override
    public void cook() {
        if (!canBeCooked() || cooking) return;

        cooking = true;
        state = ItemState.COOKING;

        // After 12s → COOKED
        TimerUtils.scheduleSeconds(() -> {
            if (state == ItemState.COOKING) {
                state = ItemState.COOKED;
            }
        }, 12);

        // After 24s → BURNED
        TimerUtils.scheduleSeconds(() -> {
            if (state == ItemState.COOKING || state == ItemState.COOKED) {
                state = ItemState.BURNED;
                cooking = false;
            }
        }, 24);
    }
}
