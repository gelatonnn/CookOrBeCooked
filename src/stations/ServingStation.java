package stations;

import items.core.Item;
import items.dish.DishBase;
import items.dish.DishState;
import items.utensils.Plate;
import utils.TimerUtils;

public class ServingStation extends BaseStation {
    private boolean served = false;

    @Override
    public String getName() { return "Serving Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (item instanceof Plate plate) {
            return !plate.getContents().isEmpty();
        }
        return item instanceof DishBase d && d.getDishState() == DishState.COMPLETE;
    }

    @Override
    public boolean place(Item item) {
        if (!super.place(item)) {
            System.out.println("❌ Cannot serve this item!");
            return false;
        }

        served = true;

        // Return plate as dirty after 10 seconds
        TimerUtils.scheduleSeconds(() -> {
            if (item instanceof Plate plate) {
                plate.makeDirty();
                System.out.println("🍽️  Dirty plate returned to Plate Storage");
            }
            storedItem = null;
        }, 10);

        return true;
    }

    public boolean isServed() { return served; }
}
