package model.chef.states;

import items.core.Item;
import model.chef.*;
import stations.Station;

public class CarryingState implements ChefState {
    @Override
    public void enter(Chef chef) {}

    @Override
    public void move(Chef chef, int dx, int dy) {
        chef.changeState(new MovingState(this));
        chef.move(dx, dy);
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {
        if (st == null || chef.getHeldItem() == null) return;

        Item held = chef.getHeldItem();
        if (!st.canPlace(held)) return;

        if (st.place(held)) {
            chef.setHeldItem(null);
            chef.changeState(new IdleState());
        }
    }

    @Override
    public void interact(Chef chef, Station st) {
        placeItem(chef, st);
    }
}