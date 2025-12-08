package stations;

import items.core.Item;
import items.core.Preparable;
import items.core.ItemState;

public class CuttingStation extends BaseStation {
    @Override
    public String getName() { return "Cutting Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (item instanceof Preparable p) {
            Item i = (Item) p;
            return i.getState() == ItemState.RAW && p.canBeChopped();
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) {
            System.out.println("❌ This item cannot be chopped here!");
            return false;
        }
        System.out.println("📍 Placed " + item.getName() + " on Cutting Station");
        return super.place(item);
    }
}