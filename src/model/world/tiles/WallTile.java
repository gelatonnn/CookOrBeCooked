package model.world.tiles;

import model.world.Tile;
import utils.Position;

public class WallTile extends Tile {
    public WallTile(Position pos) { super(pos); }
    @Override public boolean isWalkable() { return false; }
}