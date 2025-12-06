package stations;

import items.core.Item;
import items.core.Preparable;

public class CuttingStation extends BaseStation {
    @Override
    public String getName() { return "Cutting Station"; }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof Preparable p && p.canBeChopped();
    }

    @Override
    public boolean place(Item item) {
        return super.place(item);
    }
}