package stations;

import items.core.Item;

public class TrashStation implements Station {
    @Override
    public String getName() { return "Trash Station"; }

    @Override
    public boolean canPlace(Item item) { return true; }

    @Override
    public boolean place(Item item) { return true; }

    @Override
    public Item pick() { return null; }

    @Override
    public Item peek() { return null; }

    @Override
    public boolean isOccupied() { return false; }
}