package stations;

import items.core.Item;
import items.core.CookingDevice;

public class TrashStation implements Station {
    @Override
    public String getName() { return "Trash Station"; }

    @Override
    public boolean canPlace(Item item) {
        return true;
    }

    @Override
    public boolean place(Item item) {
        if (item instanceof CookingDevice dev) {
            dev.clearContents();
            System.out.println("🗑️  Emptied " + item.getName() + " contents into trash");
            return false; // Don't actually destroy the device
        }

        System.out.println("🗑️  Threw away " + item.getName());
        return true;
    }

    @Override
    public Item pick() {
        return null;
    }

    @Override
    public Item peek() {
        return null;
    }

    @Override
    public boolean isOccupied() {
        return false;
    }
}