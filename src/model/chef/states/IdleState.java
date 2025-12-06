package model.chef.states;

import items.core.Item;
import model.chef.*;
import stations.Station;

public class IdleState implements ChefState {
    @Override
    public void enter(Chef chef) {}

    @Override
    public void move(Chef chef, int dx, int dy) {
        chef.changeState(new MovingState(this));
        chef.move(dx, dy);
    }

    @Override
    public void pickItem(Chef chef, Item item) {
        if (item == null) return;
        chef.setHeldItem(item);
        chef.changeState(new CarryingState());
    }

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {
        if (st == null) return;

        String stName = st.getName().toLowerCase();

        if (stName.contains("cutting")) {
            chef.changeState(new BusyCuttingState(st));
            return;
        }

        if (stName.contains("cooking") || stName.contains("stove")) {
            chef.changeState(new BusyCookingState(st));
            return;
        }

        if (stName.contains("washing") || stName.contains("sink")) {
            chef.changeState(new BusyWashingState(st));
            return;
        }

        Item i = st.pick();
        if (i != null) {
            chef.setHeldItem(i);
            chef.changeState(new CarryingState());
        }
    }
}