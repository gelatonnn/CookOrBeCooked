package model.chef.states;

import items.core.Item;
import items.core.Preparable;
import model.chef.*;
import stations.Station;
import utils.TimerUtils;

public class BusyCuttingState implements ChefState {
    private final Station station;
    private int progress = 0;
    private final int maxProgress = 3;

    public BusyCuttingState(Station st) {
        this.station = st;
    }

    @Override
    public void enter(Chef chef) {
        System.out.println("🔪 " + chef.getName() + " started cutting...");
        view.gui.AssetManager.getInstance().playSound("chop");
        cutWithProgress(chef);
    }

    private void cutWithProgress(Chef chef) {
        if (progress >= maxProgress) {
            finishCutting(chef);
            return;
        }

        progress++;
        System.out.println("   Cutting progress: " + progress + "/" + maxProgress);

        TimerUtils.schedule(() -> cutWithProgress(chef), 1000);
    }

    private void finishCutting(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof Preparable p) {
            p.chop();
            System.out.println("✅ Cutting complete! Item is now CHOPPED");
        }
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        System.out.println("⚠ Chef is cutting, cannot move! Progress paused.");
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {
    }

    public int getProgress() {
    return progress;
}

    public int getMaxProgress() {
    return maxProgress;
}
}
