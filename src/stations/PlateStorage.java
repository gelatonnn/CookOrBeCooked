package stations;

import items.core.Item;
import items.utensils.Plate;

public class PlateStorage implements Station {
    private int cleanPlates = 4;  // Start with 4 clean plates

    @Override
    public String getName() {
        return "Plate Storage [" + cleanPlates + " clean]";
    }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof Plate plate && !plate.isClean();
    }

    @Override
    public boolean place(Item item) {
        if (item instanceof Plate plate && !plate.isClean()) {
            // Dirty plate returned
            System.out.println("🍽️  Dirty plate returned to storage");
            return true;
        }
        return false;
    }

    @Override
    public Item pick() {
        if (cleanPlates > 0) {
            cleanPlates--;
            System.out.println("🍽️  Got clean plate (" + cleanPlates + " remaining)");
            return new Plate();
        } else {
            System.out.println("❌ No clean plates! Wash dirty plates first.");
            return null;
        }
    }

    @Override
    public Item peek() {
        return cleanPlates > 0 ? new Plate() : null;
    }

    @Override
    public boolean isOccupied() {
        return false;
    }

    public void returnCleanPlate() {
        cleanPlates++;
        System.out.println("✨ Clean plate returned to storage");
    }
}