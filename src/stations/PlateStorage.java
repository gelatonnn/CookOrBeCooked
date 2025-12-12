package stations;

import java.util.Stack;

import items.core.Item;
import items.utensils.DirtyPlate;
import items.utensils.Plate;

public class PlateStorage implements Station {
    private final Stack<Item> plates = new Stack<>();

    public PlateStorage() {
        for (int i = 0; i < 4; i++) {
            plates.push(new Plate());
        }
    }

    @Override
    public String getName() {
        int cleanCount = 0;
        for (Item item : plates) {
            if (item instanceof Plate p && p.isClean()) {
                cleanCount++;
            }
        }
        return "Plate Storage [" + cleanCount + " clean]";
    }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof Plate || item instanceof DirtyPlate;
    }

    @Override
    public boolean place(Item item) {
        if (canPlace(item)) {
            plates.push(item);
            String type = (item instanceof DirtyPlate || (item instanceof Plate p && !p.isClean())) 
                          ? "Dirty" : "Clean";
            System.out.println("🍽️ " + type + " plate returned to storage");
            return true;
        }
        return false;
    }

    @Override
    public Item pick() {
        if (!plates.isEmpty()) {
            Item item = plates.pop();
            String type = (item instanceof DirtyPlate || (item instanceof Plate p && !p.isClean())) 
                          ? "dirty" : "clean";
            System.out.println("🍽️ Picked " + type + " plate (" + plates.size() + " remaining)");
            return item;
        } else {
            System.out.println("❌ No plates in storage!");
            return null;
        }
    }

    @Override
    public Item peek() {
        return !plates.isEmpty() ? plates.peek() : null;
    }

    @Override
    public boolean isOccupied() {
        return false; 
    }

    public void returnCleanPlate() {
        place(new Plate());
    }
}