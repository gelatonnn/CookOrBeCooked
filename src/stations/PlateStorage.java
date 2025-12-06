package stations;

import items.core.Item;
import items.utensils.Plate;

public class PlateStorage implements Station {
    @Override
    public String getName() { return "Plate Storage"; }

    @Override
    public boolean canPlace(Item item) { return false; }

    @Override
    public boolean place(Item item) { return false; }

    @Override
    public Item pick() {
        return new Plate();
    }

    @Override
    public Item peek() { return null; }

    @Override
    public boolean isOccupied() { return false; }
}