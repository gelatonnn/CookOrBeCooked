package model.chef.states;

import items.core.Item;
import model.chef.Chef;
import model.chef.ChefState;
import stations.Station;

public class BusyCookingState implements ChefState {
    
    public BusyCookingState(Station station) {
    }

    @Override
    public void enter(Chef chef) {
        System.out.println(chef.getName() + " meletakkan alat masak.");
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {}

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}