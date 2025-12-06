package stations;

import items.core.Item;
import items.utensils.Plate;

public class WashingStation extends BaseStation {
    @Override
    public String getName() { return "Washing Station"; }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof Plate;
    }

    @Override
    public boolean place(Item item) {
        return super.place(item);
    }
}