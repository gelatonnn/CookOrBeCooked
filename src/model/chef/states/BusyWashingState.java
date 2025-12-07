package model.chef.states;

import items.core.Item;
import items.utensils.Plate;
import model.chef.*;
import stations.Station;
import utils.TimerUtils;

public class BusyWashingState implements ChefState {
    private final Station station;
    private int progress = 0;
    private final int maxProgress = 3;

    public BusyWashingState(Station station) {
        this.station = station;
    }

    @Override
    public void enter(Chef chef) {
        System.out.println(chef.getName() + " started washing plates...");
        washWithProgress(chef);
    }

    private void washWithProgress(Chef chef) {
        if (progress >= maxProgress) {
            finishWashing(chef);
            return;
        }

        progress++;
        System.out.println("   Washing progress: " + progress + "/" + maxProgress);

        TimerUtils.schedule(() -> washWithProgress(chef), 1000);
    }

    private void finishWashing(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof Plate plate) {
            plate.wash();
            System.out.println("Plate is now clean!");
        }
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        System.out.println("⚠ Chef is washing, cannot move! Progress paused.");
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}
