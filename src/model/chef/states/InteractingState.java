package model.chef.states;

import items.core.Item;
import model.chef.*;
import stations.Station;

public class InteractingState implements ChefState {
    @Override
    public void enter(Chef chef) {}

    @Override
    public void move(Chef chef, int dx, int dy) {}

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}