package stations;

import items.core.CookingDevice;
import items.core.Item;

public class CookingStation extends BaseStation {
    @Override
    public String getName() { return "Cooking Station"; }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof CookingDevice;
    }

    @Override
    public boolean place(Item item) {
        return super.place(item);
    }
}