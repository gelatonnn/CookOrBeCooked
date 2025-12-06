package model.world;

import utils.Position;

public abstract class Tile {
    protected final Position pos;

    public Tile(Position pos) {
        this.pos = pos;
    }

    public Position getPos() { return pos; }

    public abstract boolean isWalkable();
}