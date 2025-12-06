package model.world.tiles;

import model.world.Tile;
import utils.Position;

public class WalkableTile extends Tile {
    public WalkableTile(Position pos) { super(pos); }
    @Override public boolean isWalkable() { return true; }
}