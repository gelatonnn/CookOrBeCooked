package tile;

import chef.Chef;
import items.utensils.Plate;
import model.Position;
import java.util.Stack;

public class WashingStation extends Station {
    private Stack<Plate> dirtyStack;  
    private Stack<Plate> cleanStack;  

    public WashingStation(Position pos) {
        super(pos, StationType.WASHING);
        this.dirtyStack = new Stack<>();
        this.cleanStack = new Stack<>();
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (c.getHeldItem() instanceof Plate) {
            Plate plate = (Plate) c.getHeldItem();
            if (plate.isClean()) {
                return new InteractionResult(false, "Piring sudah bersih.");
            }
            plate.wash();
            cleanStack.push(plate);
            return new InteractionResult(true, "Piring dicuci.");
        }
        return new InteractionResult(false, "Tidak ada piring kotor untuk dicuci.");
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Washing Station.");
    }
}