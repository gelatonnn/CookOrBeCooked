package tile;

import chef.Chef;
import items.utensils.Plate;
import model.Position;
import java.util.Stack;

public class WashingStation extends Station {
    private Stack<Plate> dirtyStack;  
    private Stack<Plate> cleanStack;  

    public WashingStation(Position pos) {
        super(pos, null);
        this.dirtyStack = new Stack<>();
        this.cleanStack = new Stack<>();
    }

    @Override
    public void interact(Chef chef) {
        if (!dirtyStack.isEmpty()) {
            Plate plate = dirtyStack.pop();
            plate.wash(); 
            cleanStack.push(plate);  
            System.out.println("Chef " + chef.getName() + " mencuci piring.");
        } else {
            System.out.println("Tidak ada piring kotor untuk dicuci.");
        }
    }

    public void addDirtyPlate(Plate plate) {
        dirtyStack.push(plate);
    }

    @Override
    public void onEnter(Chef chef) {
        // Handle chef entering the washing station
    }

    public Plate getCleanPlate() {
        if (!cleanStack.isEmpty()) {
            return cleanStack.pop();
        }
        return null;
    }
}
