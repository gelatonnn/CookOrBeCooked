package model.chef.states;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;
import model.chef.Chef;
import model.chef.ChefState;
import stations.Station;
import utils.TimerUtils;
import view.gui.AssetManager;

public class BusyCuttingState implements ChefState {
    private final Station station;
    private int progress = 0;
    private final int maxProgress = 3; 

    public BusyCuttingState(Station station) {
        this.station = station;
    }

    @Override
    public void enter(Chef chef) {
        if (station.isOccupied()) {
            Item item = station.peek();
            
            if (!(item instanceof Preparable)) {
                cancelAction(chef, "Item tidak bisa dipotong!");
                return;
            }
            
            if (item.getState() != ItemState.RAW) {
                cancelAction(chef, "Bahan sudah dipotong!");
                return;
            }
        } else {
            cancelAction(chef, "Station kosong!");
            return;
        }

        System.out.println("🔪 " + chef.getName() + " started cutting...");
        AssetManager.getInstance().playSound("chop");
        cutWithProgress(chef);
    }
    
    private void cancelAction(Chef chef, String reason) {
        System.out.println("⚠️ " + reason);
        chef.changeState(new IdleState());
    }

    private void cutWithProgress(Chef chef) {
        if (!(chef.getState() instanceof BusyCuttingState)) return;

        if (progress >= maxProgress) {
            finishCutting(chef);
            return;
        }

        progress++;
        // System.out.println("   Cutting progress: " + progress + "/" + maxProgress);
        
        if (progress < maxProgress) {
             AssetManager.getInstance().playSound("chop");
        }

        TimerUtils.schedule(() -> cutWithProgress(chef), 1000);
    }

    private void finishCutting(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof Preparable p) {
            p.chop();
            System.out.println("✅ Cutting complete!");
        }
        chef.changeState(new IdleState());
    }

    @Override public void move(Chef chef, int dx, int dy) { System.out.println("⚠ Chef is busy cutting..."); }
    @Override public void pickItem(Chef chef, Item item) {}
    @Override public void placeItem(Chef chef, Station st) {}
    @Override public void interact(Chef chef, Station st) {}

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }
}