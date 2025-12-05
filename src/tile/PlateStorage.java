package tile;

import chef.Chef;
import model.Position;
import items.utensils.Plate;
import java.util.Stack;

public class PlateStorage extends Station {
    private Stack<Plate> plates = new Stack<>();  

    public PlateStorage(Position pos) {
        super(pos, StationType.PLATE_STORAGE);
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (plates.isEmpty()) {
            return new InteractionResult(false, "Tidak ada piring bersih.");
        }

        Plate plate = plates.pop();
        c.setHeldItem(plate);
        return new InteractionResult(true, "Piring berhasil diambil.");
    }

    public void addPlate(Plate plate) {
        plates.push(plate);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Plate Storage.");
    }
}
