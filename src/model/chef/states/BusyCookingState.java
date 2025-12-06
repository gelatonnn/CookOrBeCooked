package model.chef.states;

import items.core.CookingDevice;
import items.core.Item;
import model.chef.*;
import stations.Station;
import utils.TimerUtils;

public class BusyCookingState implements ChefState {
    private final Station station;

    public BusyCookingState(Station station) {
        this.station = station;
    }

    @Override
    public void enter(Chef chef) {
        System.out.println(chef.getName() + " mulai memasak...");
        if (station.isOccupied() && station.peek() instanceof CookingDevice dev) {
            dev.startCooking();
        }
        TimerUtils.scheduleSeconds(() -> finishCooking(chef), 3);
    }

    private void finishCooking(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof CookingDevice dev) {
            dev.finishCooking();
        }
        System.out.println("Memasak selesai!");
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        System.out.println("Chef sedang memasak, tidak bisa bergerak!");
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}