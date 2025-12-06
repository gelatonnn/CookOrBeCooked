package model.chef.states;

import items.core.Preparable;
import model.chef.*;
import stations.Station;
import utils.TimerUtils;
import items.core.Item;

public class BusyCuttingState implements ChefState {
    private final Station station;

    public BusyCuttingState(Station st) {
        this.station = st;
    }

    @Override
    public void enter(Chef chef) {
        System.out.println(chef.getName() + " mulai memotong...");
        TimerUtils.scheduleSeconds(() -> finishCutting(chef), 3);
    }

    private void finishCutting(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof Preparable p) {
            p.chop();
            System.out.println("Pemotongan selesai!");
        }
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        System.out.println("Chef sedang memotong, tidak bisa bergerak!");
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}