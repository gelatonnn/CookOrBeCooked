package tile;

import chef.Chef;
import model.Position;

public class Wall extends Tile {
    public Wall(Position pos) {
        super(pos, false);
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef tidak bisa memasuki tile ini karena dinding.");
    }

    @Override
    public boolean isWalkable() {
        return false;
    }
}
