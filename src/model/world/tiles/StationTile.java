package model.world.tiles;

import model.world.Tile;
import stations.Station;
import utils.Position;

public class StationTile extends Tile {
    private final Station st;

    public StationTile(Position pos, Station st) {
        super(pos);
        this.st = st;
    }

    public Station getStation() { return st; }

    @Override
    public boolean isWalkable() { return false; }
}