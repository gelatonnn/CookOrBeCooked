package items.utensils;

import items.core.Item;

public class DirtyPlate extends Item {
    public DirtyPlate() {
        this.portable = true;
    }

    @Override
    public String getName() {
        return "Plate_Dirty"; 
    }
}