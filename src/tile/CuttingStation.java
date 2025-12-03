package tile;

import chef.Chef;
import model.Position;

public class CuttingStation extends Station {
    public CuttingStation(Position pos) {
        super(pos, null);
    }

    @Override
    public void interact(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memotong bahan di Cutting Station.");
    }

    @Override
    public void onEnter(Chef chef) {
        // Handle chef entering the cutting station
    }
}
