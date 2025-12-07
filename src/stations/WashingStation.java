package stations;

import items.core.Item;
import items.utensils.Plate;
import items.core.ItemState;

public class WashingStation extends BaseStation {
    @Override
    public String getName() { return "Washing Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (item instanceof Plate plate) {
            return !plate.isClean();
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) {
            if (item instanceof Plate plate && plate.isClean()) {
                System.out.println("❌ This plate is already clean!");
            } else {
                System.out.println("❌ Only dirty plates can be washed here!");
            }
            return false;
        }

        System.out.println("📍 Dirty plate placed in washing station");
        return super.place(item);
    }

    @Override
    public Item pick() {
        Item item = super.pick();
        if (item instanceof Plate plate && plate.isClean()) {
            System.out.println("✅ Picked up clean plate");
        }
        return item;
    }
}