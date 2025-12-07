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
        if (item == null) {
            System.out.println("❌ No item to pick up!");
            return;
        }
        chef.setHeldItem(item);
        chef.changeState(new CarryingState());
        System.out.println("✅ Picked up: " + item.getName());
    }

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {
        if (st == null) {
            System.out.println("❌ No station to interact with!");
            return;
        }

        String stName = st.getName().toLowerCase();

        if (stName.contains("cutting")) {
            if (!st.isOccupied()) {
                System.out.println("❌ Cutting station is empty! Place RAW ingredient first.");
                return;
            }
            chef.changeState(new BusyCuttingState(st));
            return;
        }

        if (stName.contains("cooking")) {
            if (!st.isOccupied()) {
                System.out.println("❌ No cooking device on stove! Place boiling pot or frying pan first.");
                return;
            }
            chef.changeState(new BusyCookingState(st));
            return;
        }

        if (stName.contains("washing")) {
            if (!st.isOccupied()) {
                System.out.println("❌ No plate to wash!");
                return;
            }
            chef.changeState(new BusyWashingState(st));
            return;
        }

        // Default: pick item from station
        Item i = st.pick();
        if (i != null) {
            chef.setHeldItem(i);
            chef.changeState(new CarryingState());
            System.out.println("✅ Picked up: " + i.getName() + " from " + st.getName());
        } else {
            System.out.println("❌ Nothing to pick from " + st.getName());
        }
    }
}