package model.chef.states;

import items.core.Item;
import items.utensils.DirtyPlate; // <--- INI IMPORT YANG KURANG
import items.utensils.Plate;
import model.chef.Chef;
import model.chef.ChefState;
import stations.Station;
import utils.TimerUtils;
import view.gui.AssetManager; // Import untuk suara

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
        // Mainkan suara cuci sekali di awal atau loop (opsional)
        AssetManager.getInstance().playSound("wash"); 
        washWithProgress(chef);
    }

    private void washWithProgress(Chef chef) {
        // Cek jika state berubah (misal ditarik oleh event lain), batalkan timer
        if (!(chef.getState() instanceof BusyWashingState)) return;

        if (progress >= maxProgress) {
            finishWashing(chef);
            return;
        }

        progress++;
        // System.out.println("   Washing progress: " + progress + "/" + maxProgress);
        
        // Suara gosok setiap detik
        if (progress < maxProgress) {
             AssetManager.getInstance().playSound("wash");
        }

        TimerUtils.schedule(() -> washWithProgress(chef), 1000);
    }

    private void finishWashing(Chef chef) {
        // Cek apakah ada barang di station
        if (station.isOccupied()) {
            Item item = station.peek();
            
            // Skenario 1: Item adalah DirtyPlate (Piring Kotor dari Serving)
            if (item instanceof DirtyPlate) {
                station.pick(); // Hapus piring kotor
                station.place(new Plate()); // Ganti dengan piring bersih baru
                
                System.out.println("✨ Cling! DirtyPlate menjadi Plate bersih.");
                AssetManager.getInstance().playSound("pick"); // Suara 'cling'
            } 
            // Skenario 2: Item adalah Plate biasa tapi kotor (Legacy logic)
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