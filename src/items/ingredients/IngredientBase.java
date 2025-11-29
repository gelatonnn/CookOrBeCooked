package items.ingredients;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class IngredientBase extends Item implements Preparable {

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(10);

    private boolean isCurrentlyCooking = false;

    public IngredientBase() {
        this.state = ItemState.RAW;
        this.portable = true;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return state == ItemState.COOKED;
    }

    @Override
    public void chop() {
        if (canBeChopped() && state == ItemState.RAW) {
            state = ItemState.CHOPPED;
        }
    }

    @Override
    public void cook() {
        if (!canBeCooked()) return;
        if (isCurrentlyCooking) return; // prevent double schedule

        isCurrentlyCooking = true;
        state = ItemState.COOKING;

        // 12 detik bakal jadi COOKED
        scheduler.schedule(() -> {
            if (state == ItemState.COOKING) {
                state = ItemState.COOKED;
            }
        }, 12, TimeUnit.SECONDS);

        // 24 detik bakal jadi BURNED
        scheduler.schedule(() -> {
            if (state == ItemState.COOKED || state == ItemState.COOKING) {
                state = ItemState.BURNED;
            }
        }, 24, TimeUnit.SECONDS);
    }
}
