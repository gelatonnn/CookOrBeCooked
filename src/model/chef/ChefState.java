package model.chef;

import items.core.Item;
import stations.Station;

public interface ChefState {
    void enter(Chef chef);
    void move(Chef chef, int dx, int dy);
    void pickItem(Chef chef, Item item);
    void placeItem(Chef chef, Station station);
    void interact(Chef chef, Station station);
}