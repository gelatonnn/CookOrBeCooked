package stations;

import items.core.Item;
// import items.utensils.DirtyPlate; // Tidak lagi dibutuhkan

public class ServingStation extends BaseStation {
    @Override
    public String getName() { return "Serving Station"; }

    @Override
    public boolean canPlace(Item item) {
        // Serving Station menolak placement manual
        return false;
    }

    @Override
    public boolean place(Item item) {
        return false;
    }

    @Override
    public Item pick() {
        return super.pick();
    }
}