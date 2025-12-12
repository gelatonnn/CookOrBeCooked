package stations;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;

public class CookingStation extends BaseStation {
    @Override
    public String getName() { return "Cooking Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (storedItem == null) return item instanceof CookingDevice;
        if (storedItem instanceof CookingDevice device && item instanceof Preparable prep) {
            return device.canAccept(prep);
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        if (storedItem == null) return super.place(item);
        if (storedItem instanceof CookingDevice device && item instanceof Preparable prep) {
            if (device.canAccept(prep)) {
                device.addIngredient(prep);
                return true;
            }
        }
        return false;
    }

    @Override
    public Item pick() {
        Item item = super.pick();

        if (item instanceof CookingDevice device) {
            device.finishCooking();
        }

        return item;
    }
}