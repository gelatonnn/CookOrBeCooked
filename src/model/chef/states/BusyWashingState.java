package model.chef.states;

import items.core.Item;
import items.utensils.Plate;
import model.chef.*;
import stations.Station;
import utils.TimerUtils;
import view.gui.AssetManager; // Jangan lupa import

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

        // TRIGGER SOUND: WASH
        // Suara akan muncul setiap detik (efek menggosok: sruk... sruk... sruk...)
        AssetManager.getInstance().playSound("wash");

        TimerUtils.schedule(() -> washWithProgress(chef), 1000);
    }

    private void finishWashing(Chef chef) {
        if (station.isOccupied() && station.peek() instanceof Plate plate) {
            plate.wash();
            System.out.println("Plate is now clean!");
            
            // Opsional: Bunyi 'cling' atau 'pick' saat selesai jadi bersih
            AssetManager.getInstance().playSound("pick"); 
        }
        chef.changeState(new IdleState());
    }

    // ... (Method lain biarkan tetap sama) ...
    @Override public void move(Chef chef, int dx, int dy) { System.out.println("⚠ Chef is washing..."); }
    @Override public void pickItem(Chef chef, Item item) {}
    @Override public void placeItem(Chef chef, Station st) {}
    @Override public void interact(Chef chef, Station st) {}
}