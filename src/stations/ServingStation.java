package stations;

import items.core.Item;
import items.dish.DishBase;
import items.dish.DishState;

public class ServingStation extends BaseStation {
    private boolean served = false;

    @Override
    public String getName() { return "Serving Station"; }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof DishBase d && d.getDishState() == DishState.COMPLETE;
    }

    @Override
    public boolean place(Item item) {
        if (!super.place(item)) return false;
        served = true;
        return true;
    }

    public boolean isServed() { return served; }
}