package tile;

import chef.Chef;
import model.Position;

public class Walkable extends Tile {
    public Walkable(Position pos) {
        super(pos, true);
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki tile di posisi " + pos);
    }
}
