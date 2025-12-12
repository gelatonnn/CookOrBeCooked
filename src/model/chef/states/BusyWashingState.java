package model.chef.states;

import items.core.Item;
import items.utensils.DirtyPlate;
import items.utensils.Plate;
import model.chef.Chef;
import model.chef.ChefState;
import stations.Station;
import utils.TimerUtils;
import view.gui.AssetManager; 

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
        AssetManager.getInstance().playSound("wash"); 
        washWithProgress(chef);
    }

    private void washWithProgress(Chef chef) {
        if (!(chef.getState() instanceof BusyWashingState)) return;

        if (progress >= maxProgress) {
            finishWashing(chef);
            return;
        }

        progress++;
        // System.out.println("   Washing progress: " + progress + "/" + maxProgress);
        
        if (progress < maxProgress) {
             AssetManager.getInstance().playSound("wash");
        }

        TimerUtils.schedule(() -> washWithProgress(chef), 1000);
    }

    private void finishWashing(Chef chef) {
        if (station.isOccupied()) {
            Item item = station.peek();
            
            // Skenario 1: Item adalah DirtyPlate 
            if (item instanceof DirtyPlate) {
                station.pick(); 
                station.place(new Plate()); 
                
                System.out.println("✨ Cling! DirtyPlate menjadi Plate bersih.");
                AssetManager.getInstance().playSound("pick"); 
            } 
            // Skenario 2: Item adalah Plate biasa tapi kotor 
            else if (item instanceof Plate p && !p.isClean()) {
                p.wash();
                System.out.println("✨ Cling! Plate dicuci bersih.");
                AssetManager.getInstance().playSound("pick");
            }
        }
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        System.out.println("⚠ Chef is washing, cannot move! Progress paused.");
    }

    @Override public void pickItem(Chef chef, Item item) {}
    @Override public void placeItem(Chef chef, Station st) {}
    @Override public void interact(Chef chef, Station st) {}
}