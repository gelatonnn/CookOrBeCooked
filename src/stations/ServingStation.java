package stations;

import items.core.Item;
import items.utensils.DirtyPlate;

public class ServingStation extends BaseStation {
    @Override
    public String getName() { return "Serving Station"; }

    // --- FIX ERROR: Implementasi Method Wajib ---

    @Override
    public boolean canPlace(Item item) {
        // Serving Station secara default menolak placement manual biasa.
        // (Serving dish dilakukan via logic khusus di GameEngine, 
        //  dan Dirty Plate muncul otomatis via receiveDirtyPlate)
        return false;
    }

    @Override
    public boolean place(Item item) {
        // Tidak ada logika place manual di sini
        return false;
    }

    // --- Method Khusus Logic Game (Dipanggil GameEngine) ---

    // Method untuk memunculkan piring kotor setelah serving berhasil
    public void receiveDirtyPlate() {
        this.storedItem = new DirtyPlate();
    }
    
    // Player harus bisa mengambil Dirty Plate untuk dicuci
    @Override
    public Item pick() {
        return super.pick();
    }
}