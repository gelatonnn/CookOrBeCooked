package stations;

import items.core.Item;

public interface Station {
    String getName();
    boolean canPlace(Item item);
    boolean place(Item item);
    Item pick();
    Item peek();
    boolean isOccupied();
}