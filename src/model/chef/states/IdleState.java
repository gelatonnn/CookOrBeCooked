package model.chef.states;

import items.core.Item;
import model.chef.*;
import stations.Station;

public class IdleState implements ChefState {
    @Override
    public void enter(Chef chef) {}

    @Override
    public void move(Chef chef, int dx, int dy) {
        chef.changeState(new MovingState(this));
        chef.move(dx, dy);
    }

    @Override
    public void pickItem(Chef chef, Item item) {
        if (item == null) return;
        chef.setHeldItem(item);
        chef.changeState(new CarryingState());
    }

    @Override
    public void placeItem(Chef chef, Station st) {}

    @Override
    public void interact(Chef chef, Station st) {
        if (st == null) return;

        String stName = st.getName().toLowerCase();

        // 1. Interaksi Cutting Station (Potong) -> Masuk BusyCuttingState
        if (stName.contains("cutting")) {
            chef.changeState(new BusyCuttingState(st));
            return;
        }

        // 2. Interaksi Washing Station (Cuci) -> Masuk BusyWashingState
        if (stName.contains("washing") || stName.contains("sink")) {
            chef.changeState(new BusyWashingState(st));
            return;
        }

        // --- PERBAIKAN DI SINI ---
        // HAPUS blok kode yang mengecek "cooking" atau "stove".
        // Karena masak sudah otomatis, menekan E di kompor tidak boleh bikin Chef busy.
        // Kita cukup return saja agar tidak terjadi apa-apa.
        if (stName.contains("cooking") || stName.contains("stove")) {
            // Do nothing (Cooking is automatic)
            return;
        }
        // -------------------------

        // Default: Jika interaksi bukan action khusus, coba ambil item (opsional)
        // (Atau bisa dikosongkan jika E murni hanya untuk aksi, bukan ambil barang)
        Item i = st.pick();
        if (i != null) {
            chef.setHeldItem(i);
            chef.changeState(new CarryingState());
        }
    }
}