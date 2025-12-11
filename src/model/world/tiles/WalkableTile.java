package model.world.tiles;

import items.core.Item;
import model.world.Tile;
import utils.Position;

public class WalkableTile extends Tile {
    // Legacy field removed/deprecated.
    // Items are now handled by GameEngine.floorItems list for pixel precision.

    public WalkableTile(Position pos) {
        super(pos);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    // Deprecated methods kept for compilation compatibility if other classes reference them,
    // but logic should move to GameEngine.
    public void setItem(Item item) { }
    public Item getItem() { return null; }
    public Item pick() { return null; }
}