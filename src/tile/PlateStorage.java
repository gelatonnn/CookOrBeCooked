package tile;

import chef.Chef;
import model.Position;
import items.utensils.Plate;
import java.util.Stack;

public class PlateStorage extends Station {
    private Stack<Plate> plates;  

    public PlateStorage(Position pos) {
        super(pos, null);
        this.plates = new Stack<>();
    }

    @Override
    public void interact(Chef chef) {
        if (!plates.isEmpty()) {
            Plate plate = plates.pop();  // Ambil piring bersih
            chef.pickUp(plate);  // Chef mengambil piring
            System.out.println("Chef " + chef.getName() + " mengambil piring bersih.");
        } else {
            System.out.println("Tidak ada piring bersih.");
        }
    }

    public void addPlate(Plate plate) {
        plates.push(plate);  // Menambahkan piring bersih
    }

    @Override
    public void onEnter(Chef chef) {
        // Handle chef entering the plate storage
    }
}
