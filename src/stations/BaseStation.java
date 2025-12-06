package stations;

import items.core.Item;

public abstract class BaseStation implements Station {
    protected Item storedItem = null;

    @Override
    public boolean isOccupied() {
        return storedItem != null;
    }

    @Override
    public Item pick() {
        Item temp = storedItem;
        storedItem = null;
        return temp;
    }

    @Override
    public Item peek() {
        return storedItem;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) return false;
        storedItem = item;
        return true;
    }

    public Item getStoredItem() {
        return storedItem;
    }

    protected void setStoredItem(Item item) {
        this.storedItem = item;
    }
}