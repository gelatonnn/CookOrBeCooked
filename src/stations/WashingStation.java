package stations;

import items.core.Item;
import items.utensils.DirtyPlate;
import items.utensils.Plate;

public class WashingStation extends BaseStation {
    
    @Override
    public String getName() {
        return "Washing Station"; 
    }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof DirtyPlate || item instanceof Plate;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) {
            System.out.println("❌ Washing Station: Hanya menerima piring!");
            return false;
        }
        
        String status = "Bersih";
        if (item instanceof DirtyPlate || (item instanceof Plate p && !p.isClean())) {
            status = "Kotor";
        }
        System.out.println("🍽️ Piring (" + status + ") diletakkan di Washing Station.");
        
        return super.place(item);
    }
}