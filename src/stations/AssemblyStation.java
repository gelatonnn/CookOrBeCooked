package stations;

import items.core.Item;
import items.core.Preparable;
import items.dish.DishBase;

public class AssemblyStation extends BaseStation {
    @Override
    public String getName() { return "Assembly Station"; }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof DishBase || item instanceof Preparable;
    }
}