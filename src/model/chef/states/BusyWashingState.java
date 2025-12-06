package model.chef.states;

import items.core.Item;
import items.utensils.Plate;
import model.chef.*;
import stations.Station;
import utils.TimerUtils;

public class BusyWashingState implements ChefState {
    private final Station station;

    public BusyWashingState(Station station) {
        this.station = station;
    }

    @Override
    public void enter(Chef chef) {
        System.out.println(chef.getName() + " mulai mencuci piring...");
        TimerUtils.scheduleSeconds(() -> finishWashing(chef), 3);
    }

    private void finishWashing(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof Plate plate) {
            plate.wash();
            System.out.println("Piring sudah bersih!");
        }
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        System.out.println("Chef sedang mencuci, tidak bisa bergerak!");
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}