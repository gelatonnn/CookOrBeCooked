package tile;

import chef.Chef;
import model.Position;

public abstract class Tile {
    protected Position pos;
    protected boolean isWalkable;

    public Tile(Position pos, boolean isWalkable) {
        this.pos = pos;
        this.isWalkable = isWalkable;
    }

    public Position getPos() {
        return pos;
    }

    public abstract boolean isWalkable(); 

    public abstract void onEnter(Chef chef);
}
