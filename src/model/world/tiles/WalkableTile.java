package model.world.tiles;

import items.core.Item;
import model.world.Tile;
import utils.Position;

public class WalkableTile extends Tile {
    private Item itemOnFloor;

    public WalkableTile(Position pos) {
        super(pos);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    // New methods to handle thrown items
    public void setItem(Item item) {
        this.itemOnFloor = item;
    }

    public Item getItem() {
        return itemOnFloor;
    }

    public Item pick() {
        Item i = itemOnFloor;
        itemOnFloor = null;
        return i;
    }
}