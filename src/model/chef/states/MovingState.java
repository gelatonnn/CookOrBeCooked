package model.chef.states;

import items.core.Item;
import model.chef.*;
import stations.Station;

public class MovingState implements ChefState {
    private final ChefState returnState;

    public MovingState(ChefState prev) {
        this.returnState = prev;
    }

    @Override
    public void enter(Chef chef) {}

    @Override
    public void move(Chef chef, int dx, int dy) {
        chef.setPos(chef.getX() + dx, chef.getY() + dy);
        chef.changeState(returnState);
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}