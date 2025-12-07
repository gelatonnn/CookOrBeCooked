package model.chef.states;

import items.core.Item;
import model.chef.*;
import stations.Station;

public class BusyCookingState implements ChefState {
    // State ini sebenarnya tidak lagi menahan Chef selama 3 detik
    // Karena proses memasak terjadi otomatis di CookingDevice (Pot/Pan)
    
    public BusyCookingState(Station station) {
        // Konstruktor dipertahankan untuk kompatibilitas
    }

    @Override
    public void enter(Chef chef) {
        // Langsung kembalikan ke Idle karena Chef tidak perlu nungguin kompor
        // Kompor akan masak sendiri (Concurrency di BoilingPot/FryingPan)
        System.out.println(chef.getName() + " meletakkan alat masak.");
        chef.changeState(new IdleState());
    }

    @Override
    public void move(Chef chef, int dx, int dy) {
        // Tidak terpakai karena langsung pindah ke Idle
    }

    @Override
    public void pickItem(Chef chef, Item item) {}

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {}
}